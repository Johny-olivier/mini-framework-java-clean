package testframework.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;
import framework.annotation.ResponseBody;
import framework.web.ModelAndView;
import testframework.modeles.Employe;

@Controller
public class EmployeController {

    @Mapping(url = "/employe", method = "GET")
    public ModelAndView formulaire() {
        return new ModelAndView("employe-form");
    }

    @Mapping(url = "/employe", method = "POST")
    public ModelAndView enregistrer(Employe employe) {
        ModelAndView vue = new ModelAndView("employe-resultat");
        vue.addAttribute("employe", employe);
        return vue;
    }

    @Mapping(url = "/api/employe", method = "POST")
    @ResponseBody
    public Employe apiEnregistrer(Employe employe) {
        return employe;
    }
}
