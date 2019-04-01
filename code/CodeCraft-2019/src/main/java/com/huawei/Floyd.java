package com.huawei;

import java.util.ArrayList;

public class Floyd {

    Vars vars;
    double[][] values; // 最短路径长度
    int[][] paths; // 最短路径中的第一个节点id
    Path[][] pathList; // 最短路径列表(存roadId)


    public Floyd(Vars vars) {
        this.vars = vars;
        Road[][] graph = vars.trafficGraph;
        int len = graph.length;
        values = new double[len][len];
        paths = new int[len][len];
        pathList = new Path[len][len];
        // 初始化values和paths
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (i == j) {
                    values[i][j] = 0;
                } else if (graph[i][j] == null) {
                    values[i][j] = Double.MAX_VALUE;
                } else {
                    values[i][j] = graph[i][j].weight;
                }
                paths[i][j] = j;    // 初值则为各个边的终点顶点
            }
        }
        for (int u = 0; u < len; u++) {     // 复杂度O(n^3)
            for (int v = 0; v < len; v++) {
                for (int w = 0; w < len; w++) {
                    if (values[v][u] + values[u][w] < values[v][w]) {               // 比较两种方案，取更小的
                        values[v][w] = values[v][u] + values[u][w];
                        paths[v][w] = paths[v][u];                                  // 更新Path矩阵
                    }
                }
            }
        }
        // 根据paths获取完整的路径
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (values[i][j] == Double.MAX_VALUE || i == j) continue;    // 无效路径
                Path p = new Path();
                int pre = i;
                int next = paths[i][j];
                p.add(graph[pre][next].id);
                while (next != j) {
                    pre = next;
                    next = paths[next][j];
                    p.add(graph[pre][next].id);
                }
                pathList[i][j] = p;
            }
        }
    }

    class Path {
        ArrayList<Integer> list = new ArrayList<>();
        public int get(int i) {
            return list.get(i);
        }
        public void add(int e) {
            list.add(e);
        }
    }
}
