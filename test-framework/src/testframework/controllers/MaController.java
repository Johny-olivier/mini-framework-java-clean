package testframework.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;

@Controller
public class MaController {

    @Mapping(url = "/users")
    public void listUsers() {

    }

    @Mapping(url = "/login")
    public void login() {

    }

}
