package framework.utils;

import java.lang.reflect.Method;

public class RouteInfo {

    private Class<?> controller;
    private Method method;

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
