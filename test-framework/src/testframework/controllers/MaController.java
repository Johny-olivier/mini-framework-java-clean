package testframework.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;

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

}
