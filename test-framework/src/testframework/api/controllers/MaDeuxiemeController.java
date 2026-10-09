package testframework.api.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;

@Controller
public class MaDeuxiemeController {

    @Mapping(url = "/")
    public void index() {

    }

    @Mapping(url = "/about")
    public void about() {

    }

}
