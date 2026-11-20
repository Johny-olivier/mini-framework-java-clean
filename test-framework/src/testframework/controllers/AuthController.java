package testframework.controllers;

import framework.annotation.Auth;
import framework.annotation.Controller;
import framework.annotation.Mapping;
import framework.annotation.ResponseBody;
import framework.session.FrameworkSession;
import framework.web.ModelAndView;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Mapping(url = "/public")
    public String publicPage() {
        return "Page publique";
    }

    @Mapping(url = "/prive")
    @Auth
    public String prive() {
        return "Page privée";
    }

    @Mapping(url = "/api/prive")
    @Auth
    @ResponseBody
    public Map<String, Object> apiPrive() {
        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("message", "accès autorisé");
        return resultat;
    }

    @Mapping(url = "/admin")
    @Auth(role = "admin")
    public String admin() {
        return "Page admin";
    }

    @Mapping(url = "/auth/login")
    public String login(FrameworkSession session, String role) {
        if (role == null || role.isBlank()) {
            role = "user";
        }
        session.setAttribute("role", role);
        return "Connecté en tant que " + role;
    }

    @Mapping(url = "/auth/logout")
    public String logout(FrameworkSession session) {
        session.clear();
        return "Déconnecté";
    }
}
