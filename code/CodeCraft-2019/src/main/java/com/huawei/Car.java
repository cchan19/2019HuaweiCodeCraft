package com.huawei;

public class Car implements Comparable<Car> {
    
    // 常量
    public static final int INIT = 0;
    public static final int WAIT = 1;
    public static final int STOP = 2;
    public static final int REACH = 3;

    // 基本属性
    int id;
    int from;
    int to;
    int speed;
    int planTime;

    // 附加属性
    int state;      // 状态
    int posOnRoad;      // 在车道的位置

    public Car(int id, int from, int to, int speed, int planTime) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.speed = speed;
        this.planTime = planTime;
        this.state = INIT;
        this.posOnRoad = 0;
    }

    public void reset() {
        state = INIT;
        posOnRoad = 0;
    }

    @Override
    public String toString() {
        return "[carId:" + id + ",posOnRoad:" + posOnRoad + ",state:" +state + "]";
    }

    @Override
    public int compareTo(Car o) {
        return id - o.id;
    }
}