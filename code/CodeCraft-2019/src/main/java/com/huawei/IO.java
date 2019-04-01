package com.huawei;

import java.util.*;
import java.io.*;
import java.util.regex.*;
import org.apache.log4j.Logger;

/**
 * 读取数据，保存为数组列表
 */

public class IO {

    private static final Logger logger = Logger.getLogger(IO.class);

    // 读Car
    public static ArrayList<Car> loadCarData(String path) {
        ArrayList<Car> carList = new ArrayList<>();
        try (FileReader reader = new FileReader(path); BufferedReader br = new BufferedReader(reader)) {
            String line;
            Pattern p = Pattern.compile("\\((-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+)\\)");
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '#') continue;
                Matcher m = p.matcher(line); 
                if (m.find()) {
                    carList.add(new Car(
                        Integer.valueOf(m.group(1)),
                        Integer.valueOf(m.group(2)),
                        Integer.valueOf(m.group(3)),
                        Integer.valueOf(m.group(4)),
                        Integer.valueOf(m.group(5))
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("carList size : " + carList.size());
        Collections.sort(carList);
        return carList;
    }
    // 读Road
    public static ArrayList<Road> loadRoadData(String path) {
        ArrayList<Road> roadList = new ArrayList<>();
        try (FileReader reader = new FileReader(path); BufferedReader br = new BufferedReader(reader)) {
            String line;
            Pattern p = Pattern.compile("\\((-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+)\\)");
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '#') continue;
                Matcher m = p.matcher(line); 
                if (m.find()) {
                    roadList.add(new Road(
                        Integer.valueOf(m.group(1)),
                        Integer.valueOf(m.group(2)),
                        Integer.valueOf(m.group(3)),
                        Integer.valueOf(m.group(4)),
                        Integer.valueOf(m.group(5)),
                        Integer.valueOf(m.group(6)),
                        Integer.valueOf(m.group(7))
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("roadList size : " + roadList.size());
        Collections.sort(roadList);
        return roadList;
    }
    // 读Cross
    public static ArrayList<Cross> loadCrossData(String path) {
        ArrayList<Cross> crossList = new ArrayList<>();
        try (FileReader reader = new FileReader(path); BufferedReader br = new BufferedReader(reader)) {
            String line;
            Pattern p = Pattern.compile("\\((-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+)\\)");
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '#') continue;
                Matcher m = p.matcher(line); 
                if (m.find()) {
                    crossList.add(new Cross(
                        Integer.valueOf(m.group(1)),
                        Integer.valueOf(m.group(2)),
                        Integer.valueOf(m.group(3)),
                        Integer.valueOf(m.group(4)),
                        Integer.valueOf(m.group(5))
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("crossList size: " + crossList.size());
        Collections.sort(crossList);
        return crossList;
    }

    // 读Answer，测试c++
    public static ArrayList<Route> loadRouteData(String path) {
        ArrayList<Route> routeList = new ArrayList<>();
        try (FileReader reader = new FileReader(path); BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("(", "");
                line = line.replaceAll(" ", "");
                line = line.replace(")", "");
                String[] ss = line.split(",");
                ArrayList<Integer> roadIds = new ArrayList<>();
                for (int i = 2; i < ss.length; i++) {
                    roadIds.add(Integer.parseInt(ss[i]));
                }
                Route route = new Route(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), roadIds);
                routeList.add(route);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("routeList size: " + routeList.size());
        Collections.sort(routeList);
        return routeList;
    }


    // 写answer
    public static void writeAnswer(String path, ArrayList<Route> routeList) {
        try (FileWriter fileWritter = new FileWriter(path)) {
            for (Route route: routeList) {
                StringBuffer sb = new StringBuffer();
                sb.append("(" + route.carId + ", " + route.startTime);
                ArrayList<Integer> roadIds = route.roadIds;
                for (int rId: roadIds) {
                    sb.append(", " + rId);
                }
                sb.append(")\n");
                fileWritter.write(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 记录详细信息
    public static void writeDetail(String path, int time, ArrayList<Road> roadList) {
        try (FileWriter fileWritter = new FileWriter(path, true)) {
            StringBuffer sb = new StringBuffer();
            sb.append("time:" + time + "\n");
            for (Road road: roadList) {
                for (int i = 0; i <= road.isDuplex; i++) {
                    sb.append("(" + road.id + ",");
                    String dir = (i == 0) ? "forward" : "backward";
                    sb.append(dir + ",[");
                    ArrayList<LinkedList<Car>> oneWay = road.channels.get(i);
                    for (int j = 0; j < oneWay.size(); j++) {
                        LinkedList<Car> chl = oneWay.get(j);
                        int[] chlArr = new int[road.length];
                        for (int k = 0; k < chlArr.length; k++) chlArr[k] = -1;
                        Iterator<Car> it = chl.iterator();
                        while (it.hasNext()) {
                            Car car = it.next();
                            int pos = car.posOnRoad;
                            chlArr[road.length - pos] = car.id;
                        }
                        sb.append("[");
                        for (int k = 0; k < chlArr.length; k++) {
                            sb.append(chlArr[k]);
                            if (k < chlArr.length-1) sb.append(",");
                        }
                        sb.append("]");
                        if (j < oneWay.size()-1) sb.append(",");
                    }
                    sb.append("])\n");
                }
            }
            fileWritter.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 直接输出答案，测试用
    public static void writeAnswer(String path, String answer) {
        try (FileWriter fileWritter = new FileWriter(path);
             FileReader reader = new FileReader(answer);
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '#') continue;
                fileWritter.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}