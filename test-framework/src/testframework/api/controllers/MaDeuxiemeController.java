package testframework.api.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;

@Controller
public class MaDeuxiemeController {

    @Mapping(url = "/")
    public String index() {
        return "Accueil";
    }

    @Mapping(url = "/about")
    public String about() {
        return "Page about";
    }

}
