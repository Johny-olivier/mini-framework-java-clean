package testframework.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;
import framework.annotation.ResponseBody;
import framework.session.FrameworkSession;
import framework.web.ModelAndView;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class SessionController {

    @Mapping(url = "/compteur")
    public String compteur(FrameworkSession session) {
        return incrementer(session);
    }

    @Mapping(url = "/session")
    public String sessionCompteur(FrameworkSession session) {
        return incrementer(session);
    }

    @Mapping(url = "/counter")
    public String counter(FrameworkSession session) {
        return incrementer(session);
    }

    private String incrementer(FrameworkSession session) {
        Integer valeur = (Integer) session.getAttribute("compteur");
        if (valeur == null) {
            valeur = 0;
        }
        valeur++;
        session.setAttribute("compteur", valeur);
        return "Compteur : " + valeur;
    }

    @Mapping(url = "/reset")
    public String reset(FrameworkSession session) {
        session.clear();
        return "OK";
    }

    @Mapping(url = "/session/reset")
    public String sessionReset(FrameworkSession session) {
        session.clear();
        return "OK";
    }

    @Mapping(url = "/api/session/reset")
    public String apiSessionReset(FrameworkSession session) {
        session.clear();
        return "OK";
    }

    @Mapping(url = "/api/session")
    @ResponseBody
    public Map<String, Object> attributs(FrameworkSession session) {
        return new LinkedHashMap<>(session.getAttributes());
    }

    @Mapping(url = "/session/attributs")
    @ResponseBody
    public Map<String, Object> attributsAlias(FrameworkSession session) {
        return new LinkedHashMap<>(session.getAttributes());
    }

    @Mapping(url = "/session/demo")
    public ModelAndView demo(FrameworkSession session, String nom) {
        session.setAttribute("nom", nom);
        ModelAndView vue = new ModelAndView("bienvenue");
        vue.addAttribute("nom", nom == null ? "visiteur" : nom);
        return vue;
    }
}
