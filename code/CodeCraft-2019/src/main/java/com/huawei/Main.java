package com.huawei;

import org.apache.log4j.Logger;

import java.net.Inet4Address;
import java.util.*;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        if (args.length != 4) {
            logger.error("please input args: inputFilePath, resultFilePath");
            return;
        }

        logger.info("Start...");

        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " and answerPath = " + answerPath);

        // TODO:read input files
        logger.info("start read input files");
        ArrayList<Car> carList = IO.loadCarData(carPath);
        ArrayList<Road> roadList = IO.loadRoadData(roadPath);
        ArrayList<Cross> crossList = IO.loadCrossData(crossPath);
        Vars vars = new Vars(carList, roadList, crossList);
        // 规划路径列表
        ArrayList<Route> routeList = new ArrayList<>();
        for (int i = 0; i < carList.size(); i++) {
            Car car = carList.get(i);
            routeList.add(new Route(car.id, 0, null));
        }

        // TODO: calc
        int K = 10;
        Dijkstra dijkstra = new Dijkstra(vars, K);

        // TODO: test dispatcher
        Dispatcher dispatcher = new Dispatcher(vars);
        int base = vars.carList.size();  // 问题规模参考值
        int res = -1;
        int tryTime = 1;
        int nps = 50;      // 每秒发车数，从大到小搜索，找到可行的初始解
        Random random = new Random(1234);
        do {
            logger.info("--------------- Try Time: " + tryTime + "----------------");
            /****  ****/
            int dt = 1;
            for (int i = 0; i < routeList.size(); i++) {
                Car car;
//
                car = vars.carList.get(i);

                int randRouteId = random.nextInt(K);
                int fromIdx = vars.crossIdxMap.get(car.from);
                int toIdx = vars.crossIdxMap.get(car.to);
                routeList.get(i).reset();
                routeList.get(i).carId = car.id;
                routeList.get(i).startTime = car.planTime + dt;
                routeList.get(i).roadIds = dijkstra.pathLists[fromIdx][toIdx].getPath(randRouteId);
                if (i % nps == 0) {
                    dt+= 1;
                }
            }
            vars.setRoutes(routeList);
            res = dispatcher.dispatch();
            vars.reset();
            tryTime++;
            if (nps > 23) {
                nps--;
            }
        } while (res == -1);

        /** 模拟退火启发，舍友说这不是模拟退火 **/
        ArrayList<Integer> history = new ArrayList<>();
        ArrayList<Integer> historyCarId = new ArrayList<>();
        history.add(res);

        ArrayList<Route> bestAns = routeList;
        int minTime = res;
        logger.info("minTime: " + minTime);
        long loopcount = 0;

        do {
            ArrayList<Route> copy = Utils.copyRoutes(bestAns);   // 拷贝结果
            do {
                loopcount++;
                Collections.sort(copy, Comparator.comparingInt(o -> o.reachTime));
                Route lastRoute = copy.get(copy.size()-1);  // 将最后到达的车的出发时间提前 (planTime - startTime)
                Car car = vars.carMap.get(lastRoute.carId);
                historyCarId.add(car.id);
                if (lastRoute.startTime - car.planTime == 0) lastRoute.startTime = 1200; // 调bug
                int newStartTime = random.nextInt(lastRoute.startTime - car.planTime) + car.planTime;
                lastRoute.startTime = newStartTime;
                Utils.resetRoutes(copy);
                vars.setRoutes(copy);
                res = dispatcher.dispatch();
                vars.reset();
            } while ((res == -1 || res >= minTime) && System.currentTimeMillis() - startTime < 700000 && loopcount < 550);  // 超时跳出
            logger.info(res);
            if (res != -1 && res < minTime) {       // 排除超时跳出
                history.add(res);
                minTime = res;
                bestAns = copy;
            }
        } while (System.currentTimeMillis() - startTime < 700000  && loopcount < 550);  // 超时跳出



        // TODO: write answer.txt
        logger.info("Start write output file");
        IO.writeAnswer(answerPath, bestAns);
        long endTime = System.currentTimeMillis();
        logger.info("程序运行时间：" + (endTime - startTime) + "ms");    // 程序运行时间

        logger.info("history");
        for (Integer h: history) {
            logger.info(h);
        }

        logger.info("history car id");
        for (Integer h: historyCarId) {
            logger.info(h);
        }

//        long totalTime = 0;
//        for (Car car: carList) {
//            totalTime += (car.reachTime - car.planTime);
//        }
//        logger.info("车辆总调度时间：" + totalTime);

        logger.info("End...");
    }
}