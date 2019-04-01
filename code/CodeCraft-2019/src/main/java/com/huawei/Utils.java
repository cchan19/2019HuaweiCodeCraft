package com.huawei;


import java.util.ArrayList;

public class Utils {

    public static ArrayList<Route> copyRoutes(ArrayList<Route> old) {
        ArrayList<Route> newRL = new ArrayList<>();
        for (int i = 0; i < old.size(); i++) {
            Route route = old.get(i);
            Route newR = new Route(0, 0, null);
            newR.carId = route.carId;
            newR.startTime = route.startTime;
            newR.reachTime = route.reachTime;
            ArrayList<Integer> newRoadId = new ArrayList<>(route.roadIds);
            newR.roadIds = newRoadId;
            newRL.add(newR);
        }
        return newRL;
    }

    public static void resetRoutes(ArrayList<Route> routes) {
        for (Route r: routes) {
            r.reset();
        }
    }
}
