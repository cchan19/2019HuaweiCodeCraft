package com.huawei;

import java.util.*;

import com.huawei.Road.CarPort;
import org.apache.log4j.Logger;


public class Dispatcher {

    private static final Logger logger = Logger.getLogger(Dispatcher.class);

    // 所有变量
    Vars vars;

    public Dispatcher(Vars vars) {
        this.vars = vars;
    }

    public int dispatch() throws Exception {
        int countAllCar = vars.carList.size();
        int countReachedCar = 0;
        int time = 1;
        while (countReachedCar < countAllCar) {
            // 死锁结束
            int reachedCar = step(time);
            if (reachedCar == -1) {
                return -1;
            }
            countReachedCar += reachedCar;
            // 测试
//            int tmp = 0;
//            for (Car car:vars.carList) {
//                if (car.state == Car.REACH) tmp++;
//            }
//            if (countReachedCar != tmp) {
//                throw new Exception();
//            }
//            logger.info("Time: " + time + ", Reached Car: " + countReachedCar);

//            IO.writeDetail("D:/Huawei/info.txt", time, vars.roadList);
            time++;
        }
        return time-1;
    }

    // 每一步调度，返回这次调度到达的车辆数
    private int step(int time) throws Exception {
//        logger.info("Time: " + time);
        int countReachedCar = 0;
        // 第一步, 遍历所有路口并更新
        vars.countWaitCar = 0;
        for (Road road: vars.roadList) {
            for (int i = 0; i <= road.isDuplex; i++) {
                for (int j = 0; j < road.size; j++) {
                    LinkedList<Car> chl = road.channels.get(i).get(j);
                    if (chl.isEmpty()) continue;
                    // vars.countWaitCar += updateCarState(chl, road, false, false);
                    vars.countWaitCar += updateCarStateAtFirstStep(chl, road);
                }
                // 更新单向道假想车位

                // 找前排最靠前的车
                int maxPos = Integer.MIN_VALUE;
                Car car = null;
                int chlIdx = 0;
                for (int j = 0; j < road.size; j++) {
                    LinkedList<Car> tmpChl = road.channels.get(i).get(j);
                    if (tmpChl.isEmpty()) continue;
                    Car tmpCar = tmpChl.getFirst();
                    if (tmpCar.state == Car.STOP) continue;
                    if (tmpCar.posOnRoad > maxPos) {
                        maxPos = tmpCar.posOnRoad;
                        car = tmpCar;
                        chlIdx = i;
                    }
                }
                Road.CarPort carPort = road.carPorts.get(i);
                if (car == null) {
                    carPort.reset();
                } else {
                    carPort.set(car.id, chlIdx);
                }
            }
        }

//        logger.info("Waited Car After Step 1:" + vars.countWaitCar);

        // 第二步， 遍历路口-遍历道路-调度车辆，直到路上没有等待的车辆
        // 记录每次遍历的等待车辆数，判断死锁
        int lastCount;
        while (vars.countWaitCar > 0) {
            lastCount = vars.countWaitCar;
//            logger.error("waitCount:" + vars.countWaitCar);
//            for (Car car: vars.carList) {
//                logger.info(car.toString() + "[roadId:" + vars.routeCarMap.get(car.id).getCurrRoadId() + "]");
//            }
            for (Cross cross: vars.crossList) {
//                logger.info("current cross id:" + cross.id);

                for (int roadId: cross.roadIdList) {

                    Road road = vars.roadMap.get(roadId);
//                    logger.info("current road id:" + roadId);
                    ArrayList<LinkedList<Car>> oneWay = road.getOutChl(cross.id);
                    if (oneWay == null) {   // 没有入口
                        continue;
                    }
                    while (true) {
                        // 找前排最靠前的车
                        int maxPos = Integer.MIN_VALUE;
                        Car car = null;
                        LinkedList<Car> chl = null;
                        int chlIdx = 0;
                        for (int i = 0; i < road.size; i++) {
                            LinkedList<Car> tmpChl = oneWay.get(i);
                            if (tmpChl.isEmpty()) continue;
                            Car tmpCar = tmpChl.getFirst();
                            if (tmpCar.state == Car.STOP) continue;
                            if (tmpCar.posOnRoad > maxPos) {
                                maxPos = tmpCar.posOnRoad;
                                car = tmpCar;
                                chl = tmpChl;
                                chlIdx = i;
                            }
                        }
                        Road.CarPort carPort = road.getCarPort(cross.id);
                        if (car == null) {
                            carPort.reset();
                            break;     // 没有需要调度的车辆
                        } else {
                            carPort.set(car.id, chlIdx);
                        }
                        int thisDir;
                        // 1.1 获取车辆路线
                        Route thisRoute = vars.routeCarMap.get(car.id);
                        // 1.2 获取车辆转向，如果到达，先当作直行，放到后面处理
                        if (thisRoute.isEndRoad()) {
                            thisDir = Cross.STRAIGHT;
                        } else {
                            int fromId = thisRoute.getCurrRoadId();     // 理论上等于roadId
                            int toId = thisRoute.getNextRoadId();
                            thisDir = cross.getDirection(fromId, toId);
                        }
                        boolean isConflict = false;
                        // 如果左转，查看右边的路上优先车是否直行
                        if (thisDir == Cross.LEFT) {
                            int rightRoadId = cross.getRightRoadId(roadId);
                            if (rightRoadId != -1) {
                                Road rightRoad = vars.roadMap.get(rightRoadId);
                                Road.CarPort rightCarPort = rightRoad.getCarPort(cross.id);
                                if (rightCarPort != null && !rightCarPort.isEmpty()) {
                                    Route thatRoute = vars.routeCarMap.get(rightCarPort.carId);
                                    // TODO
                                    if (thatRoute.isEndRoad()) {
                                        isConflict = true;
                                    } else {
                                        int fromId = thatRoute.getCurrRoadId();
                                        int toId = thatRoute.getNextRoadId();
                                        int thatDir = cross.getDirection(fromId, toId);
                                        if (thatDir == Cross.STRAIGHT) {
                                            isConflict = true;
                                        }
                                    }
                                }
                            }
                        } else if (thisDir == Cross.RIGHT) {
                            // 如果右转，查看左边是否直行，对面是否左转
                            int leftRoadId = cross.getLeftRoadId(roadId);
                            if (leftRoadId != -1) {
                                Road leftRoad = vars.roadMap.get(leftRoadId);
                                Road.CarPort leftCarPort = leftRoad.getCarPort(cross.id);
                                if (leftCarPort != null && !leftCarPort.isEmpty()) {
                                    Route thatRoute = vars.routeCarMap.get(leftCarPort.carId);
                                    if (thatRoute.isEndRoad()) {
                                        isConflict = true;
                                    } else {
                                        int fromId = thatRoute.getCurrRoadId();
                                        int toId = thatRoute.getNextRoadId();
                                        int thatDir = cross.getDirection(fromId, toId);
                                        if (thatDir == Cross.STRAIGHT) {
                                            isConflict = true;
                                        }
                                    }
                                }
                            }
                            int oppoRoadId = cross.getOppoRoadId(roadId);
                            if (oppoRoadId != -1) {
                                Road oppoRoad = vars.roadMap.get(oppoRoadId);
                                Road.CarPort oppoCarPort = oppoRoad.getCarPort(cross.id);
                                if (oppoCarPort != null && !oppoCarPort.isEmpty()) {
                                    Route thatRoute = vars.routeCarMap.get(oppoCarPort.carId);
                                    // TODO
                                    if (!thatRoute.isEndRoad()) {
                                        int fromId = thatRoute.getCurrRoadId();
                                        int toId = thatRoute.getNextRoadId();
                                        int thatDir = cross.getDirection(fromId, toId);
                                        if (thatDir == Cross.LEFT) {
                                            isConflict = true;
                                        }
                                    }
                                }
                            }
                        }
                        // 如果冲突，结束这条路
                        if (isConflict) {
//                            logger.info("Conflict happen: " + "carId:" + car.id + " roadId:" + roadId);
                            break;
                        }
                        // 可以开始过马路啦（1. 到达， 2. 过马路）
                        // 1. 到达
                        if (thisRoute.isEndRoad()) {
                            countReachedCar++;
                            car.state = Car.REACH;
                            thisRoute.reachTime = time;
                            vars.countWaitCar--;
                            chl.removeFirst();
//                            logger.info("Time " + time + " car " + car.id + " reached");
                            // 更新后面车辆状态（和前面一样，那就写成函数吧）
                            // updateCarState(chl, road, false, true);
                            vars.countWaitCar -= updateCarStateAtSecondStep(chl, road, false);
                            continue;
                        }
                        // 2. 过马路，规则10，根据当前限速和目标道路限速判断
                        // car, thisRoute, road
                        int toId = thisRoute.getNextRoadId();
                        Road thatRoad = vars.roadMap.get(toId);
                        int V1 = Math.min(car.speed, road.speed);
                        int V2 = Math.min(car.speed, thatRoad.speed);
                        int S1 = road.length - car.posOnRoad;
                        if (S1 > V1) {
                            logger.error("V1 S1");
                        }
                        int S2 = V2 - S1;   // 下一段路可行驶距离
                        if (S2 <= 0) {  // 过不了，移动到最前面，终止，同时更新车道
                            car.posOnRoad = road.length;
                            car.state = Car.STOP;
                            vars.countWaitCar--;
                            // updateCarState(chl, road, true, true);
                            vars.countWaitCar -= updateCarStateAtSecondStep(chl, road, true);
                            continue;

                        }

                        // 先看哪个车道可以进，再看那个车道的末尾的车是否是等待
                        LinkedList<Car> thatChl = null;
                        ArrayList<LinkedList<Car>> thatOneWay = thatRoad.getInChl(cross.id);
//                        // TODO 找空位
//                        for (int i = 0; i < thatRoad.size; i++) {
//                            LinkedList<Car> tmpChl = thatOneWay.get(i);
//                            if (tmpChl.isEmpty()
//                            || tmpChl.getLast().posOnRoad > 1) {
//                                thatChl = tmpChl;
//                                break;
//                            }
//                        }
//                        if (thatChl == null) {
//                            // TODO
//                            boolean allStop = true;
//                            for (int i = 0; i <thatRoad.size; i++) {
//                                LinkedList<Car> tmpChl = thatOneWay.get(i);
//                                if (tmpChl.getLast().state == Car.WAIT) {
//                                    allStop = false;
//                                    break;
//                                }
//                            }
//                            if (allStop) {
//                                car.posOnRoad = road.length;
//                                car.state = Car.STOP;
//                                vars.countWaitCar--;
//                                // updateCarState(chl, road, true, true);
//                                vars.countWaitCar -= updateCarStateAtSecondStep(chl, road, true);
//                                continue;
//                            } else {
//                                break;
//                            }
//                        } else {
//                            // TODO 1. 等待的车挡路
//                            if (!thatChl.isEmpty()
//                                    && thatChl.getLast().state == Car.WAIT
//                                    && thatChl.getLast().posOnRoad <= S2) {
//                                break;      //
//                            }
//                            chl.removeFirst();  // 出车道
//                            // updateCarState(chl, road, false, true);
//                            vars.countWaitCar -= updateCarStateAtSecondStep(chl, road, false);
//                            if (thatChl.isEmpty() || thatChl.getLast().posOnRoad > S2) {
//                                car.posOnRoad = S2;
//                            } else {
//                                car.posOnRoad = thatChl.getLast().posOnRoad - 1;
//                            }
//                            car.state = Car.STOP;
//                            thisRoute.idx++;    // 记得更新Route
//                            vars.countWaitCar--;
//                            thatChl.addLast(car);   // 进车道
//                            continue;
//                        }

                        for (int i = 0; i < thatRoad.size; i++) {
                            LinkedList<Car> tmpChl = thatOneWay.get(i);
                            if (tmpChl.isEmpty()
                                    || tmpChl.getLast().state == Car.WAIT
                                    || tmpChl.getLast().posOnRoad > 1) {
                                thatChl = tmpChl;  // 找到车道入口
                                break;
                            }
                        }
                        if (thatChl == null) {  // 没有足够的入口，行驶到前方终止
//                            break; 继续等待？
                            car.posOnRoad = road.length;
                            car.state = Car.STOP;
                            vars.countWaitCar--;
                            // updateCarState(chl, road, true, true);
                            vars.countWaitCar -= updateCarStateAtSecondStep(chl, road, true);
                            continue;

                        } else {
                            if (thatChl.isEmpty()
                                    || thatChl.getLast().posOnRoad > S2
                                    || thatChl.getLast().state == Car.STOP) { // 入口为空，或者没有阻挡，或者阻挡为终止车辆，进去，终止
                                chl.removeFirst();  // 出车道
                                // updateCarState(chl, road, false, true);
                                vars.countWaitCar -= updateCarStateAtSecondStep(chl, road, false);
                                if (thatChl.isEmpty() || thatChl.getLast().posOnRoad > S2) {
                                    car.posOnRoad = S2;
                                } else {
                                    car.posOnRoad = thatChl.getLast().posOnRoad - 1;
                                }
                                car.state = Car.STOP;
                                thisRoute.idx++;    // 记得更新Route
                                vars.countWaitCar--;
                                thatChl.addLast(car);   // 进车道
                                continue;
                            } else {    // 阻挡车辆为等待车辆，继续等待
                                break;
                            }
                        }
                    }
                }
            }
            // 如果遍历完一遍，等待车辆没有减少，则为死锁
            if (lastCount == vars.countWaitCar) {
                logger.error("Lock!!!");
                logger.error("lastCount:" + lastCount);
                logger.error("Lock Time:" + time);
                return -1;
            }
        }

        // TODO 变道策略


        /**
         * 派车出门
         * 从routeTimeMap找出出发时刻为time的车辆
         * 按id升序安排上路，如果无法出发，则推迟到下一个时刻，需要更新route和routeTimeMap
         *
         *  */
        // TODO: 请保证routelist升序
        ArrayList<Route> delayRoutes = new ArrayList<>();   // 记录推迟的车
        ArrayList<Route> currRoutes = vars.routeTimeMap.get(time);
        if (currRoutes == null) {
            return countReachedCar;     // 当前时刻没有等待出发的车辆，结束
        }
        for (int i = 0; i < currRoutes.size(); i++) {
            Route route = currRoutes.get(i);
            int carId = route.carId;
            int roadId = route.getCurrRoadId();
            Car car = vars.carMap.get(carId);
            Road road = vars.roadMap.get(roadId);
            ArrayList<LinkedList<Car>> oneWay = road.getInChl(car.from);
            if (oneWay == null) logger.error("Error route: car id " + carId);
            // 找入口
            LinkedList<Car> chl = null;
            for (int j = 0; j < road.size; j++) {
                LinkedList<Car> tmpChl = oneWay.get(j);
                if (tmpChl.isEmpty() || tmpChl.getLast().posOnRoad > 1) {
                    chl = tmpChl;
                    break;
                }
                if (tmpChl.getLast().state == Car.WAIT) logger.error("There are some errors in step 1 or step 2"); // 按理说现在所有车都终止了
            }
            if (chl == null) {  // 找不到入口，推迟到下一刻
//                route.startTime++;
                delayRoutes.add(route);
                // currRoutes.remove(i); 似乎没必要移除，而且遍历时删除元素容易出错
//                ArrayList<Route> nextRoutes = vars.routeTimeMap.get(time+1);
//                if (nextRoutes == null) nextRoutes = new ArrayList<>();
                /**
                 *  论坛解释，这里的推迟不应该参与排序
                 *
                 */
//                nextRoutes.add(route);
//                Collections.sort(nextRoutes);
//                nextRoutes.add(0, route);
//                vars.routeTimeMap.put(time+1, nextRoutes);
            } else {
                // 汽车进入车道
                car.state = Car.STOP;
                int vec = Math.min(car.speed, road.speed);
                if (chl.isEmpty() || chl.getLast().posOnRoad > vec) {
                    car.posOnRoad = vec;
                } else if (chl.getLast().posOnRoad <= vec) {
                    car.posOnRoad = chl.getLast().posOnRoad - 1;
                } else {
                    logger.error("Impossible situation");   // 老觉得会写错，多打点日志
                }
                chl.addLast(car);
//                logger.info("car start, car id:" + car.id + " road: " + road.id + " pos: " + car.posOnRoad);
            }
        }
        // 将推迟的车插入到下一时刻
        ArrayList<Route> nextRoutes = vars.routeTimeMap.get(time+1);
        if (nextRoutes == null) {
            nextRoutes = new ArrayList<>();
        }
        nextRoutes.addAll(0, delayRoutes);
//        nextRoutes.addAll(delayRoutes);
//        Collections.sort(nextRoutes);
        vars.routeTimeMap.put(time+1, nextRoutes);

        return countReachedCar;
    }


    // 更新一条单行道上的汽车状态，返回等待车辆的数目
    private int updateCarStateAtFirstStep(LinkedList<Car> chl, Road road){
        int count = 0;
        if (chl.isEmpty()) return count;
        // 遍历车道上的车辆
        Car frontCar = null;
        for (Car car: chl) {
            int vec = Math.min(car.speed, road.speed);     // 最大速度
            if (frontCar == null) {     // 第一辆车
                if (car.posOnRoad + vec > road.length) {   // 超出车道
                    car.state = Car.WAIT;
                    count++;
                } else {
                    car.posOnRoad  += vec;
                    car.state = Car.STOP;
                }
            } else {    // 后续车辆
                if (car.posOnRoad + vec >= frontCar.posOnRoad) {        // 前车阻挡
                    if (frontCar.state == Car.STOP) {       // 阻挡车辆为终止状态
                        car.posOnRoad = frontCar.posOnRoad - 1;
                        car.state = Car.STOP;
                    } else {
                        car.state = Car.WAIT;
                        count++;
                    }
                } else {        // 没有阻挡
                    car.posOnRoad += vec;
                    car.state = Car.STOP;
                }
            }
            frontCar = car;
        }
        return count;
    }

    // 更新一条单行道上的等待汽车的状态，返回由等待变为终止的车辆
    private int updateCarStateAtSecondStep(LinkedList<Car> chl, Road road, boolean ignoreFirst){
        int count = 0;
        if (chl.isEmpty()) return count;
        // 遍历车道上的车辆
        Car frontCar = null;
        for (Car car: chl) {
            int vec = Math.min(car.speed, road.speed);     // 最大速度
            if (frontCar == null) {     // 第一辆车
                if (!ignoreFirst) {
                    if (car.state == Car.STOP) break;   // 遇到终止车辆就结束,或者等待车辆继续等待
                    if (car.posOnRoad + vec > road.length) {   // 超出车道
                        break;
                    } else {
                        car.posOnRoad += vec;
                        car.state = Car.STOP;
                        count++;
                    }
                }
            } else {    // 后续车辆
                if (car.state == Car.STOP) break;   // 遇到终止车辆就结束,或者等待车辆继续等待
                if (car.posOnRoad + vec >= frontCar.posOnRoad) {        // 前车阻挡
                    car.posOnRoad = frontCar.posOnRoad - 1;
                } else {        // 没有阻挡
                    car.posOnRoad += vec;
                }
                car.state = Car.STOP;
                count++;
            }
            frontCar = car;
        }
        return count;
    }


    // // 更新一条单行道上的汽车状态，返回等待车辆的数目
    // private int updateCarState(LinkedList<Car> chl, Road road, boolean ignoreFirst, boolean ignoreStop){
    //     int count = 0;
    //     if (chl.isEmpty()) return count;
    //     // 遍历车道上的车辆
    //     Car frontCar = null;
    //     for (Car car: chl) {
    //         if (ignoreStop && car.state == Car.STOP) {
    //             break;
    //         }
    //         int vec = Math.min(car.speed, road.speed);     // 最大速度
    //         if (frontCar == null) {     // 第一辆车
    //             if (!ignoreFirst) {     // 是否忽略（考虑到规则10）
    //                 if (car.posOnRoad + vec > road.length) {   // 超出车道
    //                     car.state = Car.WAIT;
    //                     count++;
    //                 } else {
    //                     car.posOnRoad = car.speed + vec;
    //                     car.state = Car.STOP;
    //                 }
    //             }
    //         } else {    // 后续车辆
    //             if (car.posOnRoad + vec >= frontCar.posOnRoad) {        // 前车阻挡
    //                 if (frontCar.state == Car.STOP) {       // 阻挡车辆为终止状态
    //                     car.posOnRoad = frontCar.posOnRoad - 1;
    //                     car.state = Car.STOP;
    //                 } else {
    //                     car.state = Car.WAIT;
    //                     count++;
    //                 }
    //             } else {        // 没有阻挡
    //                 car.posOnRoad += vec;
    //                 car.state = Car.STOP;
    //             }
    //         }
    //         frontCar = car;
    //     }
    //     return count;
    // }

}