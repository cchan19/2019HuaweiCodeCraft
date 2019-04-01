package com.huawei;

import java.util.*;
import org.apache.log4j.Logger;

public class Road implements Comparable<Road> {

    private static final Logger logger = Logger.getLogger(Road.class);
    
    // 常量
    public static final int FORWARD = 0;        // from -> to
    public static final int BACKWARD = 1;       // to -> from

    // 基本属性
    int id;
    int length;
    int speed;
    int size;       // 单向道车道数
    int from;
    int to;
    int isDuplex;
    
    // 附加属性
    double weight;     // 定义权值为length/speed/size
    ArrayList<ArrayList<LinkedList<Car>>> channels;       // 保存单行道 [方向][车道], [0]表示from到to方向
    ArrayList<CarPort> carPorts;    // 假想车位，每个单向道一个，保存第一优先等待车辆

    public Road(int id, int length, int speed, int size, int from, int to, int isDuplex) {
        this.id = id;
        this.length = length;
        this.speed = speed;
        this.weight = 1.0 * length / speed / size;
        this.size = size;
        this.from = from;
        this.to = to;
        this.isDuplex = isDuplex;
        this.channels = new ArrayList<>();
        this.carPorts = new ArrayList<>();
        // 创建车道
        for (int i = 0; i <= isDuplex; i++) {
            ArrayList<LinkedList<Car>> oneWay = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                oneWay.add(new LinkedList<>());
            }
            channels.add(oneWay);
            carPorts.add(new CarPort());
        }
    }

    public void reset() {
        for (int i = 0; i <= isDuplex; i++) {
            ArrayList<LinkedList<Car>> oneWay = channels.get(i);
            for (int j = 0; j < size; j++) {
                LinkedList<Car> chl = oneWay.get(j);
                chl.clear();
            }
            carPorts.get(i).reset();
        }
    }

    // 根据路口id获取单向道
    ArrayList<LinkedList<Car>> getOutChl(int crossId) {
        if (crossId == to) {
            return channels.get(FORWARD);
        }
        else if (crossId == from) {
            if (isDuplex == 0) {
                return null;    // 单向道
            }
            return channels.get(BACKWARD);
        }
        else {
            logger.error("Error crossId: " + crossId + ", from: " + from + " to: " + to);
            return null;
        }
    }

    ArrayList<LinkedList<Car>> getInChl(int crossId) {
        if (crossId == from) return channels.get(FORWARD);
        else if (crossId == to) return channels.get(BACKWARD);
        else {
            logger.error("Error crossId: " + crossId + ", from: " + from + " to: " + to);
            return null;
        }
    }

    // 根据路口id获取假想车位
    CarPort getCarPort(int crossId) {
        if (crossId == to) {
            return carPorts.get(FORWARD);
        }
        else if (crossId == from) {
            if (isDuplex == 0) {
                return null;    // 单向道
            }
            return carPorts.get(BACKWARD);
        }
        else {
            logger.error("Error crossId: " + crossId + ", from: " + from + " to: " + to);
            return null;
        }
    }

    @Override
    public int compareTo(Road o) {
        return id - o.id;
    }

    // 假想车位
    class CarPort {
        int carId = -1;  // 车辆id
        int chlId = -1;  // 单行道id

        boolean isEmpty() {
            return (carId == -1);
        }

        void reset() {
            carId = -1;
            chlId = -1;
        }

        void set(int carId, int chlId) {
            this.carId = carId;
            this.chlId = chlId;
        }
    }
}