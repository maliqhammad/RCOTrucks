package com.rco.rcotrucks.utils.route;

import java.util.List;

public interface RoutingListener {
    void onRoutingFailure(RouteException e);

    void onRoutingStart();

    void onRoutingSuccess(List<Route> route, int shortestRouteIndex, String routeName);

    void onRoutingCancelled();
}
