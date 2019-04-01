package com.huawei;

import java.util.*;
import org.apache.log4j.Logger;


// 一辆车的规划路线
public class Route implements Comparable<Route> {
    
    private static final Logger logger = Logger.getLogger(Route.class);


    //  基本属性
    int carId;
    int startTime;
    int reachTime;
    ArrayList<Integer> roadIds;

    // 附加属性
    int idx;        // 表示当前所在的道路

    public Route(int carId, int startTime, ArrayList<Integer> roadIds) {
        this.carId = carId;
        this.startTime = startTime;
        this.idx = 0;
        this.roadIds = roadIds;
        this.reachTime = -1;
    }

    public void reset() {
        idx = 0;
        reachTime = -1;
    }

    // 判断该车是否在最后的路上
    public boolean isEndRoad() {
        return (idx >= roadIds.size()-1);
    }

    // 获取当前所在道路id
    public int getCurrRoadId() {
        return roadIds.get(idx);
    }

    // 获取下一条道路所在id
    public int getNextRoadId() {
        if (isEndRoad()) {
            logger.error("Is end!");
            return -1;
        }
        return roadIds.get(idx+1);
    }

    @Override
    public int compareTo(Route o) {
        return carId - o.carId;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Route carId:" + carId + " ");
        for (int id: roadIds) {
            sb.append(id + "->");
        }
        return sb.toString();
    }
}