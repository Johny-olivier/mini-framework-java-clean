package framework.servlet;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControllerServlet extends HttpServlet  {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processController(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processController(request, response);
    }

    protected void processController(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = "url : " + request.getRequestURI() + "\n";
        String method = "method : " + request.getMethod() + "\n";
        String tongasoa = "Tonga soa !" ;

        response.getWriter().write(url);
        response.getWriter().write(method);
        response.getWriter().write(tongasoa);
    }
}
