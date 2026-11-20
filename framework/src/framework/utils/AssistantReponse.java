package framework.utils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Écriture centralisée des réponses du framework : texte, JSON et erreurs
 * HTTP. Les sprints suivants (authentification, rôles) réutiliseront ces
 * méthodes pour rester cohérents.
 */
public final class AssistantReponse {

    private AssistantReponse() {
    }

    public static void ecrireTexte(HttpServletResponse reponse, String contenu) throws IOException {
        reponse.setContentType("text/plain;charset=UTF-8");
        PrintWriter writer = reponse.getWriter();
        writer.println(contenu);
    }

    public static void ecrireJson(HttpServletResponse reponse, Object valeur) throws IOException {
        reponse.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = reponse.getWriter();
        writer.println(EcritureJson.ecrire(valeur));
    }

    public static void ecrireNonTrouve(HttpServletResponse reponse, RegistreRoutes registre, String chemin)
            throws IOException {
        reponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        ecrireTexte(reponse, "Aucune méthode ne correspond à " + chemin + ".\n\n" + registre.decrire());
    }

    public static void ecrireMethodeNonAutorisee(HttpServletResponse reponse, String chemin, Set<String> methodes)
            throws IOException {
        reponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        reponse.setHeader("Allow", String.join(", ", methodes));
        ecrireTexte(reponse, "Verbe HTTP non accepté pour " + chemin + ".\n"
                + "Verbes autorisés : " + String.join(", ", methodes) + ".");
    }

    public static void ecrireRequeteInvalide(HttpServletResponse reponse, String message) throws IOException {
        reponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        ecrireTexte(reponse, message);
    }

    public static void ecrireErreurInterne(HttpServletResponse reponse) throws IOException {
        reponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ecrireTexte(reponse, "Erreur interne lors du traitement de la requête.");
    }
}
