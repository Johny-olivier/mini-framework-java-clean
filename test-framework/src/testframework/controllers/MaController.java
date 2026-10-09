package testframework.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;
import framework.web.ModelAndView;

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

    @Mapping(url = "/bienvenue")
    public ModelAndView bienvenue() {
        ModelAndView mv = new ModelAndView("bienvenue");
        mv.addAttribute("nom", "Rakoto");
        return mv;
    }

}
