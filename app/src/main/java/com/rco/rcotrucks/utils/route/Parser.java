package com.rco.rcotrucks.utils.route;

import java.util.List;


public interface Parser {
    List<Route> parse() throws RouteException;
}