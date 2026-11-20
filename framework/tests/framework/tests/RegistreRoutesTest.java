package framework.tests;

import framework.annotation.Controller;
import framework.annotation.Mapping;
import framework.utils.RegistreRoutes;
import java.util.List;
import java.util.Set;

public final class RegistreRoutesTest {

    @Controller
    public static class ControleurAccueil {

        @Mapping(url = "/")
        public String index() {
            return "";
        }

        @Mapping(url = "/login")
        public String pageLogin() {
            return "";
        }

        @Mapping(url = "/login", method = "POST")
        public String connexion() {
            return "";
        }

        public String sansMapping() {
            return "";
        }
    }

    @Controller
    public static class ControleurDoublon {

        @Mapping(url = "/login")
        public String autreLogin() {
            return "";
        }
    }

    @Controller
    public static class ControleurAutreVerbe {

        @Mapping(url = "/login", method = "PUT")
        public String autreVerbe() {
            return "";
        }
    }

    private RegistreRoutesTest() {
    }

    public static void executer() {
        RegistreRoutes registre = new RegistreRoutes();
        registre.enregistrerTout(List.of(ControleurAccueil.class));

        Verificateur.egal(3, registre.taille(), "trois routes enregistrées, méthode non annotée ignorée");
        Verificateur.egal("pageLogin", registre.trouver("GET", "/login").getMethod().getName(), "GET /login");
        Verificateur.egal("connexion", registre.trouver("POST", "/login").getMethod().getName(), "POST /login");
        Verificateur.egal(null, registre.trouver("PUT", "/login"), "PUT /login non déclaré");
        Verificateur.verifier(registre.urlExiste("/login"), "urlExiste reconnaît /login");
        Verificateur.verifier(!registre.urlExiste("/inconnu"), "urlExiste rejette /inconnu");
        Verificateur.egal(Set.of("GET", "POST"), registre.methodesAutorisees("/login"), "verbes autorisés sur /login");
        Verificateur.egal(Set.of(), registre.methodesAutorisees("/inconnu"), "aucun verbe sur une URL inconnue");

        RegistreRoutes avecAutreVerbe = new RegistreRoutes();
        avecAutreVerbe.enregistrerTout(List.of(ControleurAccueil.class, ControleurAutreVerbe.class));
        Verificateur.egal(4, avecAutreVerbe.taille(), "même URL avec un verbe différent acceptée");

        Verificateur.echoueAvec(IllegalStateException.class,
                () -> new RegistreRoutes().enregistrerTout(List.of(ControleurAccueil.class, ControleurDoublon.class)),
                "doublon URL+verbe rejeté au démarrage");

        String description = registre.decrire();
        Verificateur.verifier(description.contains("ControleurAccueil"), "la liste 404 contient le contrôleur");
        Verificateur.verifier(description.contains("GET:/login"), "la liste 404 contient la route");
    }
}
