package framework.utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

/**
 * Lecture des paramètres de configuration du framework.
 *
 * Les paramètres sont déclarés sur le servlet dans web.xml. Au démarrage,
 * le listener n'a pas encore accès à la configuration du servlet : on passe
 * donc par l'enregistrement du servlet, avec repli sur les context-param.
 */
public final class ConfigurationFramework {

    public static final String PACKAGE_CONTROLEURS = "controller-package";
    public static final String PREFIXE_VUES = "view-prefix";
    public static final String SUFFIXE_VUES = "view-suffix";
    public static final String NOM_SERVLET = "FrontController";

    /** Attribut publié par le framework pour les contrôleurs. */
    public static final String CONTEXTE_SPRING = "springContext";

    /** Clé sous laquelle Spring publie son contexte racine. */
    public static final String RACINE_SPRING = "org.springframework.web.context.WebApplicationContext.ROOT";

    private ConfigurationFramework() {
    }

    public static String lire(ServletContext contexte, String nom) {
        String valeur = lireSurServlet(contexte, nom);
        if (valeur == null || valeur.isBlank()) {
            valeur = contexte.getInitParameter(nom);
        }
        return valeur == null ? null : valeur.trim();
    }

    public static String lire(ServletContext contexte, String nom, String valeurParDefaut) {
        String valeur = lire(contexte, nom);
        return valeur == null || valeur.isEmpty() ? valeurParDefaut : valeur;
    }

    private static String lireSurServlet(ServletContext contexte, String nom) {
        try {
            ServletRegistration servlet = contexte.getServletRegistration(NOM_SERVLET);
            return servlet == null ? null : servlet.getInitParameter(nom);
        } catch (IllegalStateException e) {
            // Le contexte n'est pas encore initialisé : les context-param prendront le relais.
            return null;
        }
    }
}
