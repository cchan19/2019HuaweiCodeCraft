package com.huawei;

import org.apache.log4j.Logger;

import java.util.*;

// 代码参考 https://www.jianshu.com/p/ea0e6894259b

public class KShortestPathYen {

    Logger logger = Logger.getLogger(KShortestPathYen.class);

    Vars vars;
    PathList[][] pathLists;

    public KShortestPathYen(Vars vars, int K) {
        this.vars = vars;
        // 使用Yen找出每对路口间的k条最短路径 List[][] (哎，泛型不能建数组，又得建个类包一下)
        int len = vars.trafficGraph.length;
        pathLists = new PathList[len][len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                logger.info("KSP Process: " + i + "->" + j);
                PathList pathList = new PathList(KSP_Yen(vars, i, j, K));
                pathLists[i][j] = pathList;
            }
        }

    }

    // 封装路径列表
    public class PathList {
        public ArrayList<ArrayList<Integer>> list;
        public PathList(ArrayList<ArrayList<Integer>> list) {
            this.list = list;
        }
        public ArrayList<Integer> getPath(int i) {
            return list.get(i);
        }
    }

    public static class MyPath {
        // 路径上的各个节点对应的数组下标（从起点到终点）
        public List<Integer> path;
        // 路径总权值
        public double weight;
        // 路径上节点个数：通过path.size()得到


        public MyPath() {
        }

        public MyPath(List<Integer> path, double weight) {
            this.path = path;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "MyPath{" +
                    "path=" + path +
                    ", weight=" + weight +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyPath path1 = (MyPath) o;
            return path != null ? path.equals(path1.path) : path1.path == null;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = path != null ? path.hashCode() : 0;
            temp = Double.doubleToLongBits(weight);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    /**
     * 用Yen's KSP算法从图中找出从startIndex到endIndex的K条最短路径
     *
     * @param vars
     * @param startIndex:起始节点的数组下标
     * @param endIndex：终止节点的数组下标
     * @param K：要求的最短路径数目
     * @return
     */
    public ArrayList<ArrayList<Integer>> KSP_Yen(Vars vars, int startIndex, int endIndex, int K) {
        if (startIndex == endIndex) return null;
        Road[][] graph = vars.trafficGraph;
        // 结果列表
        List<MyPath> result = new ArrayList<>();
        // 候选路径列表
        Set<MyPath> candidatePaths = new HashSet<>();
        // 候选路径列表中权值最小的路径，及其对应的节点个数
        // 第一条最短路径
        MyPath p1 = getSingleShortestPath_dijkstra(vars, startIndex, endIndex, null, null);
        if (p1 == null) return null;
        result.add(p1);
        int k = 1;
        List<Integer> pk = p1.path;
        while (k < K) {
            /*
            求第k+1条最短路径
             */
            // 遍历每一个偏离点
            for (int i = 0; i <= pk.size() - 2; i++) {
                // 1，pk路径中起点到偏离点Vi的路径权值
                double w1 = 0;
                for (int j = 0; j <= i - 1; j++) {
//                    w1 += NavigationUtil.getEdgeWight(g, pk.get(j), pk.get(j + 1));
                    w1 += graph[pk.get(j)][pk.get(j+1)].weight;
                }
                // 2,偏离点到终点的最短路径
//                MyPath viToDestinationSP = getSingleShortestPath_dijkstra(g,
//                        pk.get(i), endIndex, pk.subList(0, i), result);
                MyPath viToDestinationSP = getSingleShortestPath_dijkstra(vars,
                        pk.get(i), endIndex, pk.subList(0, i), result);
                if (viToDestinationSP != null) {
                    // 说明从这个偏离点可以到达终点
                    MyPath temp = new MyPath();
                    List<Integer> tempPath = new ArrayList<>(pk.subList(0, i));
                    tempPath.addAll(viToDestinationSP.path);
                    temp.path = tempPath;
                    temp.weight = w1 + viToDestinationSP.weight;
                    // 加入候选列表
                    if (!candidatePaths.contains(temp)) {
                        candidatePaths.add(temp);
                    }
                }
            }
            if (candidatePaths == null || candidatePaths.size() == 0) {
                // 没有候选路径，则无需再继续向下求解
                break;
            } else {
                // 从候选路径中选出最合适的一条，移除并加入到结果列表
                MyPath fitPath = getFitPathFromCandidate(candidatePaths);
                candidatePaths.remove(fitPath);
                result.add(fitPath);
                k++;
                pk = fitPath.path;
            }
        }
        ArrayList<ArrayList<Integer>> roadIdsList = parseMyPathList(vars, result);
        return roadIdsList;
    }

    /**
     * 从候选列表中得到一条路径作为pk+1
     * 要求：1）该路径的权值和最小；2）路径经过节点数最少
     *
     * @param candidatePaths：候选列表
     * @return
     */
    private MyPath getFitPathFromCandidate(Set<MyPath> candidatePaths) {
        MyPath result = new MyPath(null, Double.MAX_VALUE);
        for (MyPath p : candidatePaths) {
            // 对于每一条路径
            if (p.weight < result.weight) {
                result = p;
            }
            if (p.weight == result.weight && p.path.size() < result.path.size()) {
                result = p;
            }
        }
        return result;
    }

    /**
     * 用Dijkstra算法得到从startIndex到endIndex的一条最短路径
     *
     * @param vars
     * @param startIndex                               起始节点的数组下标
     * @param endIndex                                 终止节点的数组下标
     * @param unavailableNodeIndexs：求最短路径时不可用的节点（数组下标）
     * @param unavailableEdges：求最短路径时不可用的边
     * @return
     */
    public MyPath getSingleShortestPath_dijkstra(Vars vars, int startIndex, int endIndex,
                                                 List<Integer> unavailableNodeIndexs, List<MyPath> unavailableEdges) {
        //
        Road[][] graph = vars.trafficGraph;

        if (startIndex == -1) {
            //            throw new Exception("getSingleShortestPath_dijkstra()起始点编号输入错误");
        }
        if (endIndex == -1) {
            //            throw new Exception("getSingleShortestPath_dijkstra()终止点编号输入错误");
        }
//        int[] set = new int[g.n]; // 是否已并入集合，该点是否已找到最短路径
        int[] set = new int[graph.length];
        // s到i的最短路径长度
//        double[] dist = new double[g.n];
        double[] dist = new double[graph.length];
        // s到i的最短路径上i的前一个节点编号
//        int[] path = new int[g.n];
        int[] path = new int[graph.length];
        // 初始化数组
        set[startIndex] = 1;
//        for (int i = 0; i < g.n; i++) {
        for (int i = 0; i < graph.length; i++) {
            if (i == startIndex) { // 源点
                dist[i] = 0;
                path[i] = -1;
            } else {
//                if (NavigationUtil.isConnected(g, startIndex, i)) {
                if (graph[startIndex][i] != null) {
//                    dist[i] = NavigationUtil.getEdgeWight(g, startIndex, i);
                    dist[i] = graph[startIndex][i].weight;
                    path[i] = startIndex;
                } else {
                    dist[i] = Double.MAX_VALUE;
                    path[i] = -1;
                }
            }
        }

        // 不能走的边
        if (unavailableEdges != null && unavailableEdges.size() != 0) {
            for (MyPath p : unavailableEdges) {
                int index = p.path.indexOf(startIndex);
                if (index >= 0 && (index + 1) >= 0) {
                    dist[p.path.get(index + 1)] = Double.MAX_VALUE;
                    path[p.path.get(index + 1)] = -1;
                }
            }
        }

        // 不能走的点
        if (unavailableNodeIndexs != null && unavailableNodeIndexs.size() != 0) {
            for (Integer point : unavailableNodeIndexs) {
                set[point] = 1;
            }
        }

        // 需进行n-2轮循环
//        for (int i = 0; i < g.n - 2; i++) {
        for (int i = 0; i < graph.length - 2; i++) {
            int k = -1;
            double min = Double.MAX_VALUE;
            // 找出dist[]中最小的（太贪心了）
//            for (int j = 0; j < g.n; j++) {
            for (int j = 0; j < graph.length; j++) {
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
            for (int j = 0; j < graph.length; j++) {
                if (set[j] == 1) {
                    continue;
                }
//                if (NavigationUtil.isConnected(g, k, j)) {
                if (graph[k][j] != null) {
//                    double temp = dist[k] + NavigationUtil.getEdgeWight(g, k, j);
                    double temp = dist[k] + graph[k][j].weight;
                    if (temp < dist[j]) {
                        dist[j] = temp;
                        path[j] = k;
                    }
                }
            }
        }

//        System.out.println("运行Dijkstra算法后的数组情况为：");
//        System.out.print("set[]:");
//        for (int i = 0; i < g.n; i++) {
//        for (int i = 0; i < tMap.length; i++) {
//            System.out.print(set[i] + "\t");
//        }
//        System.out.println();
//        System.out.print("dist[]:");
////        for (int i = 0; i < g.n; i++) {
//        for (int i = 0; i < tMap.length; i++) {
//            System.out.print(dist[i] + "\t");
//        }
//        System.out.println();
//        System.out.print("path[]:");
////        for (int i = 0; i < g.n; i++) {
//        for (int i = 0; i < tMap.length; i++) {
//            System.out.print(path[i] + "\t");
//        }
//        System.out.println();
        // 输出
        if (dist[endIndex] == Double.MAX_VALUE) {
            // 说明没有最短路径，两点不连通
//            System.out.println("两点之间不连通");
            return null;
        } else {
//            System.out.println("节点" + g.nodes[startIndex].nodeId + "到节点" +
//                    g.nodes[endIndex].nodeId + "的最短路径长度为：" + dist[endIndex] + "，具体路径是：");
//            System.out.println("节点" + startIndex + "到节点" +
//                    endIndex + "的最短路径长度为：" + dist[endIndex] + "，具体路径是：");
            MyPath result = new MyPath();
            result.path = getMinimumPath(vars, startIndex, endIndex, path);
            result.weight = dist[endIndex];
            return result;
        }
    }

    /**
     * 输出从节点S到节点T的最短路径
     *
     * @param sIndex：起始节点在数组中下标
     * @param tIndex：终止节点在数组中下标
     */
    private List<Integer> getMinimumPath(Vars vars, int sIndex, int tIndex, int[] path) {
        List<Integer> result = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();
        stack.push(tIndex);
        int i = path[tIndex];
        while (i != -1) {
            stack.push(i);
            i = path[i];
        }
        while (!stack.isEmpty()) {
//            System.out.print(g.nodes[stack.peek()].nodeId + ":" + g.nodes[stack.peek()].nodeName + "-->");
//            result.add(NavigationUtil.getIndex(g, g.nodes[stack.pop()].nodeId));
//            System.out.print(stack.peek() + ":" + stack.peek() + "-->");
            result.add(stack.pop());
        }
//        System.out.println();
        return result;
    }

    // List<MyPath>转成ArrayList<ArrayList<Integer>>，其中存roadid
    private ArrayList<ArrayList<Integer>> parseMyPathList(Vars vars, List<MyPath> myPathList) {
        ArrayList<ArrayList<Integer>> roadIdsList = new ArrayList<>();
        for (MyPath myPath: myPathList) {
            List<Integer> path = myPath.path;
            ArrayList<Integer> roadIds = new ArrayList<>();
            for (int i = 0; i < path.size() - 1; i++) {
                int from = path.get(i);
                int to = path.get(i+1);
                int roadId = vars.trafficGraph[from][to].id;
                roadIds.add(roadId);
            }
            roadIdsList.add(roadIds);
        }
        return roadIdsList;
    }
}
