package framework.tests;

import framework.session.FrameworkSession;
import java.util.HashMap;
import java.util.Map;

public final class FrameworkSessionTest {

    private FrameworkSessionTest() {
    }

    public static void executer() {
        Map<String, Object> attributs = new HashMap<>();
        attributs.put("compteur", 1);
        attributs.put("nom", "Johny");
        framework.session.FrameworkSession session = new framework.session.FrameworkSession(
                HttpSessionFactice.creer(attributs));

        Verificateur.egal(1, session.getAttribute("compteur"), "getAttribute simple");
        Verificateur.egal("Johny", session.getAttribute("nom", String.class), "getAttribute typé");
        Verificateur.egal("SESSION-TEST", session.getId(), "getId délégué");

        Map<String, Object> copie = session.getAttributes();
        Verificateur.egal(2, copie.size(), "getAttributes copie les attributs");
        copie.put("extra", "x");
        Verificateur.verifier(!session.getAttributes().containsKey("extra"),
                "modifier la copie ne modifie pas la session");

        session.setAttribute("compteur", 2);
        Verificateur.egal(2, session.getAttribute("compteur"), "setAttribute délégué");

        Verificateur.echoueAvec(IllegalStateException.class,
                () -> session.getAttribute("compteur", String.class),
                "mauvais type rejeté");

        session.removeAttribute("nom");
        Verificateur.egal(null, session.getAttribute("nom"), "removeAttribute délégué");

        session.clear();
        Verificateur.egal(0, session.getAttributes().size(), "clear supprime tout");

        Verificateur.echoueAvec(NullPointerException.class,
                () -> new framework.session.FrameworkSession(null),
                "session null rejetée");
        Verificateur.echoueAvec(NullPointerException.class,
                () -> session.setAttribute(null, "x"),
                "clé null rejetée");
    }
}
