package framework.utils;

import java.lang.reflect.Method;

public class RouteInfo {

    private final Class<?> controller;
    private final Method method;

    public RouteInfo(Class<?> controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    public Class<?> getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }
}
