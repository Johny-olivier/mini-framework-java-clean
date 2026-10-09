package framework.servlet;

import framework.scanner.ControllerScanner;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> controllers;

    @Override
    public void init() {
        String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
        String controllerPackage = getServletConfig().getInitParameter("controller-package");
        controllers = ControllerScanner.scan(classesPath, controllerPackage);
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
        writer.println("URL : " + request.getRequestURI());
        writer.println("Méthode : " + request.getMethod());
        writer.println("Controllers : " + controllers.toString());
    }
}