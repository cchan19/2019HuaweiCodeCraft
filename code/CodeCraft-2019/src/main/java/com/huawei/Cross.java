package com.huawei;

import java.util.*;
import org.apache.log4j.Logger;


public class Cross implements Comparable<Cross> {
    
    private static final Logger logger = Logger.getLogger(Cross.class);

    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final int STRAIGHT = 2;   // 顺便定义优先级
    public static final int LEFT = 1;
    public static final int RIGHT = 0;


    // 基本属性
    int id;
    int[] roadIds;

    // 附加属性
    ArrayList<Integer> roadIdList;     // 去掉不存在的路，并排序
    HashMap<Integer, Integer> roadDirMap;   // 映射路口id到方向

    public Cross(int id, int n, int e, int s, int w) {
        this.id = id;
        this.roadIds = new int[]{n, e, s, w};
        this.roadIdList = new ArrayList<>();
        this.roadDirMap = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            if (roadIds[i] != -1) {
                roadIdList.add(roadIds[i]);
                roadDirMap.put(roadIds[i], i);
            }
        }
        Collections.sort(roadIdList);
    }

    // 根据当前和接下来的道路id获取方向（直行，左转，右转）
    public int getDirection(int fromId, int toId) {
        int from = roadDirMap.get(fromId);
        int to = roadDirMap.get(toId);
        if ((from + 2) % 4 == to) {
            return STRAIGHT;
        } else if ((from + 1) % 4 == to) {
            return LEFT;
        } else if ((from + 3) % 4 == to) {
            return RIGHT;
        } else {
            logger.error("Error roadId");
            return -1;
        }
    }

    // 根据道路id获取其他不同方向的道路id
    public int getRightRoadId(int rId) {
        if (roadDirMap.get(rId) == null) return -1;
        int dir = roadDirMap.get(rId);
        dir = (dir + 3) % 4;
        return roadIds[dir];
    }
    public int getLeftRoadId(int rId) {
        if (roadDirMap.get(rId) == null) return -1;
        int dir = roadDirMap.get(rId);
        dir = (dir + 1) % 4;
        return roadIds[dir];
    }
    public int getOppoRoadId(int rId) {
        if (roadDirMap.get(rId) == null) return -1;
        int dir = roadDirMap.get(rId);
        dir = (dir + 2) % 4;
        return roadIds[dir];
    }

    @Override
    public int compareTo(Cross o) {
        return id - o.id;
    }
}