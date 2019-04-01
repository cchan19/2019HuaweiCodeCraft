package com.huawei;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Stack;

public class Dijkstra {

    Logger logger = Logger.getLogger(Dijkstra.class);

    Vars vars;
    PathList[][] pathLists;

    // 封装路径列表
    public class PathList {
        public ArrayList<ArrayList<Integer>> list;
        public PathList(ArrayList<ArrayList<Integer>> list) {
            this.list = list;
        }
        public void addPath(ArrayList<Integer> path) {
            list.add(path);
        }
        public ArrayList<Integer> getPath(int i) {
            return list.get(i);
        }
    }

    public Dijkstra(Vars vars, int k) {
        long startTime = System.currentTimeMillis();
        this.vars = vars;
        Road[][] graph = vars.trafficGraph;
        pathLists = new PathList[graph.length][graph.length];
        for (int fromIdx = 0; fromIdx < graph.length; fromIdx++) {
            for (int toIdx = 0; toIdx < graph.length; toIdx++) {
                logger.info("Dijkstra:" + fromIdx + "->" + toIdx);
                // 创建权重矩阵
                double[][] weights = new double[graph.length][graph.length];
                for (int i = 0; i < graph.length; i++) {
                    for (int j = 0; j < graph.length; j++) {
                        if (graph[i][j] != null) {
                            weights[i][j] = graph[i][j].weight;
                        } else {
                            weights[i][j] = Double.MAX_VALUE;
                        }
                    }
                }
                pathLists[fromIdx][toIdx] = new PathList(new ArrayList<>());
                for (int i = 0; i < k; i++) {
                    ArrayList<Integer> sp = getSingleShortestPath_dijkstra(weights, fromIdx, toIdx);
//                    pathLists[fromIdx][toIdx].addPath(sp);
                    // 更新权重矩阵，将找到的路径上的权重翻倍 && cross转road
                    ArrayList<Integer> sr = new ArrayList<>();
                    for (int j = 0; j < sp.size()-1; j++) {
                        weights[sp.get(j)][sp.get(j+1)] *= 2;
                        sr.add(vars.trafficGraph[sp.get(j)][sp.get(j+1)].id);
                    }
                    pathLists[fromIdx][toIdx].addPath(sr);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        logger.info("Dijkstra Time:" + (endTime - startTime) + "ms");
    }


    /**
     * 用Dijkstra算法得到从startIndex到endIndex的一条最短路径
     *
     * @param weights
     * @param startIndex                               起始节点的数组下标
     * @param endIndex                                 终止节点的数组下标
     * @return
     */
    public ArrayList<Integer> getSingleShortestPath_dijkstra(double[][] weights, int startIndex, int endIndex) {
        //

        if (startIndex == -1) {
            //            throw new Exception("getSingleShortestPath_dijkstra()起始点编号输入错误");
        }
        if (endIndex == -1) {
            //            throw new Exception("getSingleShortestPath_dijkstra()终止点编号输入错误");
        }
//        int[] set = new int[g.n]; // 是否已并入集合，该点是否已找到最短路径
        int[] set = new int[weights.length];
        // s到i的最短路径长度
//        double[] dist = new double[g.n];
        double[] dist = new double[weights.length];
        // s到i的最短路径上i的前一个节点编号
//        int[] path = new int[g.n];
        int[] path = new int[weights.length];
        // 初始化数组
        set[startIndex] = 1;
//        for (int i = 0; i < g.n; i++) {
        for (int i = 0; i < weights.length; i++) {
            if (i == startIndex) { // 源点
                dist[i] = 0;
                path[i] = -1;
            } else {
//                if (NavigationUtil.isConnected(g, startIndex, i)) {
                if (weights[startIndex][i] != Double.MAX_VALUE) {
//                    dist[i] = NavigationUtil.getEdgeWight(g, startIndex, i);
                    dist[i] = weights[startIndex][i];
                    path[i] = startIndex;
                } else {
                    dist[i] = Double.MAX_VALUE;
                    path[i] = -1;
                }
            }
        }

        // 需进行n-2轮循环
//        for (int i = 0; i < g.n - 2; i++) {
        for (int i = 0; i < weights.length - 2; i++) {
            int k = -1;
            double min = Double.MAX_VALUE;
            // 找出dist[]中最小的（太贪心了）
//            for (int j = 0; j < g.n; j++) {
            for (int j = 0; j < weights.length; j++) {
                if (set[j] == 1) {
                    continue;
                }
                if (dist[j] < min) {
                    min = dist[j];
                    k = j;
                }
            }
            if (k == -1) {
                // 说明从源点出发与其余节点不连通，无法再向下进行扩展
                break;
            }
            set[k] = 1; // 把节点k并入
            // 修改dist[]、path[]
//            for (int j = 0; j < g.n; j++) {
            for (int j = 0; j < weights.length; j++) {
                if (set[j] == 1) {
                    continue;
                }
//                if (NavigationUtil.isConnected(g, k, j)) {
                if (weights[k][j] != Double.MAX_VALUE) {
//                    double temp = dist[k] + NavigationUtil.getEdgeWight(g, k, j);
                    double temp = dist[k] + weights[k][j];
                    if (temp < dist[j]) {
                        dist[j] = temp;
                        path[j] = k;
                    }
                }
            }
        }
        // 输出
        if (dist[endIndex] == Double.MAX_VALUE) {
            // 说明没有最短路径，两点不连通
            return null;
        } else {
            ArrayList<Integer> result = getMinimumPath(startIndex, endIndex, path);
            return result;
        }
    }

    /**
     * 输出从节点S到节点T的最短路径
     *
     * @param sIndex：起始节点在数组中下标
     * @param tIndex：终止节点在数组中下标
     */
    private ArrayList<Integer> getMinimumPath(int sIndex, int tIndex, int[] path) {
        ArrayList<Integer> result = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();
        stack.push(tIndex);
        int i = path[tIndex];
        while (i != -1) {
            stack.push(i);
            i = path[i];
        }
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }
        return result;
    }

}
