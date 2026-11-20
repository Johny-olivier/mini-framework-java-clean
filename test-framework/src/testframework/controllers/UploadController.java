package testframework.controllers;

import framework.annotation.Controller;
import framework.annotation.Mapping;
import framework.annotation.ResponseBody;
import framework.upload.Fichier;
import framework.web.ModelAndView;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import testframework.modeles.Employe;
import testframework.upload.StockageFichiers;

@Controller
public class UploadController {

    @Mapping(url = "/upload", method = "GET")
    public ModelAndView formulaire() {
        return new ModelAndView("upload-form");
    }

    @Mapping(url = "/upload", method = "POST")
    public ModelAndView envoyer(Employe employe, Fichier fichier, ServletContext contexte) throws IOException {
        Path enregistre = enregistrer(fichier, contexte);
        ModelAndView vue = new ModelAndView("upload-resultat");
        vue.addAttribute("employe", employe);
        vue.addAttribute("fichier", fichier);
        vue.addAttribute("chemin", enregistre == null ? "aucun fichier" : enregistre.toString());
        return vue;
    }

    @Mapping(url = "/api/upload", method = "POST")
    @ResponseBody
    public Map<String, Object> api(Employe employe, Fichier fichier, ServletContext contexte) throws IOException {
        Path enregistre = enregistrer(fichier, contexte);

        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("nom", employe.getNom());
        resultat.put("poste", employe.getPoste());

        if (fichier == null) {
            resultat.put("fichier", null);
        } else {
            Map<String, Object> metadonnees = new LinkedHashMap<>();
            metadonnees.put("nom", fichier.getNom());
            metadonnees.put("typeContenu", fichier.getTypeContenu());
            metadonnees.put("taille", fichier.getTaille());
            metadonnees.put("chemin", enregistre == null ? null : enregistre.toString());
            resultat.put("fichier", metadonnees);
        }
        return resultat;
    }

    private Path enregistrer(Fichier fichier, ServletContext contexte) throws IOException {
        if (fichier == null) {
            return null;
        }
        return new StockageFichiers(StockageFichiers.lireDossier(contexte)).enregistrer(fichier);
    }
}
