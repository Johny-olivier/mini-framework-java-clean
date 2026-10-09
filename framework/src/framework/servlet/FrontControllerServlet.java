package framework.servlet;

import framework.annotation.Mapping;
import framework.scanner.ControllerScanner;
import framework.utils.RouteInfo;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> controllers;
    private Map<String, RouteInfo> routes;

    @Override
    public void init() {
        String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
        String controllerPackage = getServletConfig().getInitParameter("controller-package");
        controllers = ControllerScanner.scan(classesPath, controllerPackage);
        routes = new HashMap<>();
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Mapping.class)) {
                    String url = method.getAnnotation(Mapping.class).url();
                    routes.put(url, new RouteInfo(controller, method));
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processController(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processController(request, response);
    }

    private void processController(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        var writer = response.getWriter();

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());

        Class<?> matchedController = null;
        Method matchedMethod = null;

        RouteInfo route = routes.get(path);
        if (route != null) {
            matchedController = route.getController();
            matchedMethod = route.getMethod();
        }

        if (matchedMethod != null) {
            try {
                Object instance = matchedController.getDeclaredConstructor().newInstance();
                Object result = matchedMethod.invoke(instance);
                if (result instanceof String) {
                    writer.println((String) result);
                } else if (result != null) {
                    writer.println(result.toString());
                }
            } catch (Exception e) {
                throw new RuntimeException("Erreur appel " + matchedMethod.getName(), e);
            }
        } else {
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
}
