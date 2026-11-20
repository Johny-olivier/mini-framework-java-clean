package framework.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Normalisation des chemins demandés.
 */
public final class Chemins {

    private Chemins() {
    }

    public static String depuisRequete(HttpServletRequest requete) {
        String uri = requete.getRequestURI();
        String contexte = requete.getContextPath();
        if (contexte != null && !contexte.isEmpty() && uri.startsWith(contexte)) {
            uri = uri.substring(contexte.length());
        }
        return normaliser(uri);
    }

    /**
     * Garantit un chemin commençant par / et sans slash final superflu.
     */
    public static String normaliser(String chemin) {
        if (chemin == null || chemin.isEmpty()) {
            return "/";
        }
        String resultat = (chemin.startsWith("/") ? chemin : "/" + chemin).replaceAll("/{2,}", "/");
        while (resultat.length() > 1 && resultat.endsWith("/")) {
            resultat = resultat.substring(0, resultat.length() - 1);
        }
        return resultat;
    }
}
