package testframework.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;

@Controller
public class SingletonController {

    private int compteur;

    public SingletonController() {
        // constructeur sans argument : une seule instance créée au démarrage
    }

    @Mapping(url = "/singleton")
    public String compter() {
        compteur++;
        return "Singleton : " + compteur;
    }
}
