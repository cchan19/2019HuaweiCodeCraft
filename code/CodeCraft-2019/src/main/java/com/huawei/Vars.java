package com.huawei;

import java.util.*;
import org.apache.log4j.Logger;

/**
 * 创建必要的变量来保存模型、地图等信息
 * 同时提供部分查询功能
 */

public class Vars {
    
    private static final Logger logger = Logger.getLogger(Vars.class);

    // 模型数组
    ArrayList<Car> carList;
    ArrayList<Road> roadList;
    ArrayList<Cross> crossList;

    HashMap<Integer, Car> carMap;       // 映射汽车id到实体
    HashMap<Integer, Road> roadMap;     // 映射道路id到实体
    HashMap<Integer, Cross> crossMap;    // 映射路口id到实体
    HashMap<Integer, Integer> crossIdxMap;  // 映射路口id到路口索引
    // 重构图
    Road[][] trafficGraph;      // 邻接矩阵，用crossList序号作为索引

    // 规划好的路径
    ArrayList<Route> routeList;
    HashMap<Integer, ArrayList<Route>> routeTimeMap;       // 映射时间到路线（一对多）
    HashMap<Integer, Route> routeCarMap;        // 映射汽车id到路线

    // 正在等待的车辆计数
    int countWaitCar;

    // 构造函数
    public Vars(ArrayList<Car> carList, ArrayList<Road> roadList, ArrayList<Cross> crossList) {
        this.carList = carList;
        this.roadList = roadList;
        this.crossList = crossList;
        carMap = new HashMap<>();
        roadMap = new HashMap<>();
        crossMap = new HashMap<>();
        crossIdxMap = new HashMap<>();
        // 建立映射
        for (Car c: carList) {
            carMap.put(c.id, c);
        }
        logger.info("carMap size : " + carMap.size());
        for (Road r: roadList) {
            roadMap.put(r.id, r);
        }
        logger.info("roadMap size : " + roadMap.size());
        for (Cross c: crossList) {
            crossMap.put(c.id, c);
        }
        logger.info("crossMap size : " + crossMap.size());
        for (int i = 0; i < crossList.size(); i++) {
            crossIdxMap.put(crossList.get(i).id, i);
        }
        // 遍历道路，建立邻接矩阵
        trafficGraph = new Road[crossList.size()][crossList.size()];
        for (Road r: roadList) {
            int fromIdx = crossIdxMap.get(r.from);
            int toIdx = crossIdxMap.get(r.to);
            trafficGraph[fromIdx][toIdx] = r;
            if (r.isDuplex == 1) {
                trafficGraph[toIdx][fromIdx] = r;
            }
        }
    }

    // 设置规划路径
    public void setRoutes(ArrayList<Route> routeList) {
        this.routeList = routeList;
        Collections.sort(routeList);
        this.routeCarMap = new HashMap<>();
        this.routeTimeMap = new HashMap<>();
        for (Route route: routeList) {
            // 设置时间映射
            ArrayList<Route> rl = routeTimeMap.get(route.startTime);
            if (rl == null) {
                rl = new ArrayList<>();
            }
            rl.add(route);
            routeTimeMap.put(route.startTime, rl);
            // 设置汽车id映射
            routeCarMap.put(route.carId, route);
        }
    }

    // Reset
    public void reset() {
        for (Car car: carList) {
            car.reset();
        }
        for (Road road: roadList) {
            road.reset();
        }
        this.routeList = null;
    }
}