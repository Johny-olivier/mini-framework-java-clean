package framework.servlet;

import framework.annotation.Mapping;
import framework.annotation.Param;
import framework.annotation.ResponseBody;
import framework.scanner.ControllerScanner;
import framework.utils.FrameworkUtils;
import framework.utils.RouteInfo;
import framework.web.ModelAndView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> controllers;
    private Map<String, RouteInfo> routes;
    private String viewPrefix = "";
    private String viewSuffix = "";

    @Override
    public void init() {
        String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
        String controllerPackage = getServletConfig().getInitParameter("controller-package");
        controllers = ControllerScanner.scan(classesPath, controllerPackage);
        String prefix = getServletConfig().getInitParameter("view-prefix");
        String suffix = getServletConfig().getInitParameter("view-suffix");
        if (prefix != null) {
            viewPrefix = prefix;
        }
        if (suffix != null) {
            viewSuffix = suffix;
        }
        routes = new HashMap<>();
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Mapping.class)) {
                    Mapping mapping = method.getAnnotation(Mapping.class);
                    routes.put(mapping.method() + ":" + mapping.url(), new RouteInfo(controller, method));
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        processController(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        processController(request, response);
    }

    private void processController(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/plain;charset=UTF-8");
        var writer = response.getWriter();

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());

        Class<?> matchedController = null;
        Method matchedMethod = null;

        RouteInfo route = routes.get(request.getMethod() + ":" + path);
        if (route != null) {
            matchedController = route.getController();
            matchedMethod = route.getMethod();
        }

        if (matchedMethod != null) {
            try {
                Object instance = matchedController.getDeclaredConstructor().newInstance();
                Object result = matchedMethod.invoke(instance, buildArgs(matchedMethod, request));
                if (matchedMethod.isAnnotationPresent(ResponseBody.class)) {
                    response.setContentType("application/json;charset=UTF-8");
                    writer.println(FrameworkUtils.toJson(result));
                    return;
                }
                if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;
                    for (Map.Entry<String, Object> entry : mv.getAttributes().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                    request.getRequestDispatcher(viewPrefix + mv.getUrl() + viewSuffix).forward(request, response);
                    return;
                }
                if (result instanceof String) {
                    writer.println((String) result);
                } else if (result != null) {
                    writer.println(result.toString());
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur appel " + matchedMethod.getName());
                return;
            }
        } else {
            boolean urlExiste = false;
            for (String key : routes.keySet()) {
                if (key.endsWith(":" + path)) {
                    urlExiste = true;
                    break;
                }
            }
            if (urlExiste) {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                writer.println("Verbe HTTP non accepte pour " + path + ".");
                writer.println("Utilisez un autre verbe.");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.println("Aucune méthode ne correspond.");
            writer.println();
            writer.println("Controllers détectés :");
            writer.println();

            for (Map.Entry<String, RouteInfo> entry : routes.entrySet()) {
                RouteInfo info = entry.getValue();
                writer.println(info.getController().getSimpleName());
                writer.println("    - " + entry.getKey() + "  -> " + info.getMethod().getName() + "()");
            }
        }
    }

    private Object[] buildArgs(Method method, HttpServletRequest request) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Param param = params[i].getAnnotation(Param.class);
            String name = params[i].getName();
            if (param != null) {
                name = param.value();
            }
            args[i] = convert(request.getParameter(name), params[i].getType());
        }
        return args;
    }

    private Object convert(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        }
        if (type == int.class || type == Integer.class) {
            return value == null ? 0 : Integer.parseInt(value);
        }
        if (type == long.class || type == Long.class) {
            return value == null ? 0L : Long.parseLong(value);
        }
        if (type == double.class || type == Double.class) {
            return value == null ? 0.0 : Double.parseDouble(value);
        }
        if (type == boolean.class || type == Boolean.class) {
            return value != null && Boolean.parseBoolean(value);
        }
        return value;
    }
}
