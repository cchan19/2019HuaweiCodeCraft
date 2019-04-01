package com.huawei;

import org.apache.log4j.Logger;

import java.util.ArrayList;

public class Statistics {

    private static final Logger logger = Logger.getLogger(Statistics.class);

    Vars vars;

    public Statistics(Vars vars) {
        this.vars = vars;
    }

    // 按from和to大小分组
    public double group(ArrayList<Car> one, ArrayList<Car> two) {
        for (Car car: vars.carList) {
            if (car.from >= car.to) {
                one.add(car);
            } else {
                two.add(car);
            }
        }
        logger.info("First group size: " + one.size());
        logger.info("Second group size: " + two.size());
        return 1.0 * one.size() / vars.carList.size();
    }

}
