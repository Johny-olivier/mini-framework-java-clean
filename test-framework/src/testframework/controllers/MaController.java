package testframework.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;
import framework.annotation.Param;
import framework.annotation.ResponseBody;
import framework.web.ModelAndView;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MaController {

    @Mapping(url = "/users")
    public String listUsers() {
        return "Liste des users";
    }

    @Mapping(url = "/login")
    public String login() {
        return "Page login";
    }

    @Mapping(url = "/login", method = "POST")
    public String checkLogin() {
        return "Connexion ok";
    }

    @Mapping(url = "/bienvenue")
    public ModelAndView bienvenue() {
        ModelAndView mv = new ModelAndView("bienvenue");
        mv.addAttribute("nom", "Rakoto");
        return mv;
    }

    @Mapping(url = "/api/users")
    @ResponseBody
    public Map<String, Object> apiUsers() {
        Map<String, Object> user = new HashMap<>();
        user.put("nom", "Rakoto");
        user.put("age", 20);
        return user;
    }

    @Mapping(url = "/cherche")
    public String cherche(@Param("q") String q, @Param("page") int page) {
        return "Recherche " + q + " page " + page;
    }

}
