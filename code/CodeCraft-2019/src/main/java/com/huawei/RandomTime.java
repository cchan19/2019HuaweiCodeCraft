package com.huawei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class RandomTime {

    // 生成随机时间，按车速排序
    public static int[][] randomTimeBySpeed(Random random, int range, ArrayList<Car> carList) {
        // {time, carId}
        int[][] result = new int[carList.size()][2];
        int[] times = new int[carList.size()];
        // {speed, carId}
        int[][] speeds = new int[carList.size()][2];
        for (int i = 0; i < carList.size(); i++) {
            times[i] = random.nextInt(range);
            speeds[i][0] = carList.get(i).speed;
            speeds[i][1] = carList.get(i).id;
        }
        Arrays.sort(times);
        Arrays.sort(speeds, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o2[0] - o1[0];   // 降序
            }
        });
        // merge
        for (int i = 0; i < carList.size(); i++) {
            result[i][0] = times[i];
            result[i][1] = speeds[i][1];
        }
        return result;
    }
}

