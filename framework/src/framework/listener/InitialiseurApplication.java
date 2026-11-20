package framework.listener;

import framework.scanner.ControllerScanner;
import framework.utils.ConfigurationFramework;
import framework.utils.RegistreRoutes;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.List;

/**
 * Sprint 4 : initialisation du framework au démarrage de l'application web.
 *
 * Le listener scanne les contrôleurs, construit la table des routes et la
 * publie dans le ServletContext avant la première requête. Le servlet ne fait
 * plus que lire cette table.
 */
public class InitialiseurApplication implements ServletContextListener {

    public static final String ATTRIBUT_ROUTES = "framework.routes";
    public static final String ATTRIBUT_CONTROLEURS = "framework.controleurs";
    public static final String ATTRIBUT_INSTANCES = "framework.controleurs.instances";
    public static final String ATTRIBUT_ROLE_SESSION = "framework.authRoleSessionAttribute";

    @Override
    public void contextInitialized(ServletContextEvent evenement) {
        initialiser(evenement.getServletContext());
    }

    /**
     * Construit le registre et le publie dans le contexte.
     * Utilisé par le listener, et par le servlet en repli si le listener
     * n'est pas déclaré dans web.xml.
     */
    public static RegistreRoutes initialiser(ServletContext contexte) {
        String packages = ConfigurationFramework.lire(contexte, ConfigurationFramework.PACKAGE_CONTROLEURS);
        if (packages == null || packages.isBlank()) {
            throw new IllegalStateException(
                    "Paramètre '" + ConfigurationFramework.PACKAGE_CONTROLEURS + "' manquant dans web.xml.");
        }

        String cheminClasses = contexte.getRealPath("/WEB-INF/classes");
        if (cheminClasses == null) {
            throw new IllegalStateException(
                    "Impossible de localiser /WEB-INF/classes. Déployez l'application en répertoire (exploded).");
        }

        List<Class<?>> controleurs = ControllerScanner.scan(cheminClasses, packages);
        RegistreRoutes registre = new RegistreRoutes();
        registre.enregistrerTout(controleurs);

        java.util.Map<Class<?>, Object> instances = new java.util.concurrent.ConcurrentHashMap<>();
        for (Class<?> controleur : controleurs) {
            try {
                var constructeur = controleur.getDeclaredConstructor();
                constructeur.setAccessible(true);
                instances.put(controleur, constructeur.newInstance());
            } catch (java.lang.reflect.InvocationTargetException e) {
                java.lang.Throwable cause = e.getCause() == null ? e : e.getCause();
                throw new IllegalStateException(
                        "Impossible d'instancier le contrôleur " + controleur.getName() + " au démarrage.", cause);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Impossible d'instancier le contrôleur " + controleur.getName()
                        + " au démarrage. Un constructeur sans argument est requis.", e);
            }
        }
        contexte.setAttribute(ATTRIBUT_INSTANCES, instances);

        // Sprint 5bis : Spring reste optionnel. Si un ContextLoaderListener a
        // déjà publié son contexte racine, on le republie sous un nom stable
        // pour les contrôleurs. Sinon l'attribut vaut null et le framework
        // continue de fonctionner sans Spring.
        contexte.setAttribute(ConfigurationFramework.CONTEXTE_SPRING,
                contexte.getAttribute(ConfigurationFramework.RACINE_SPRING));

        String attributRole = contexte.getInitParameter("authRoleSessionAttribute");
        if (attributRole == null || attributRole.trim().isEmpty()) {
            attributRole = "role";
        }
        contexte.setAttribute(ATTRIBUT_ROLE_SESSION, attributRole.trim());

        contexte.setAttribute(ATTRIBUT_CONTROLEURS, controleurs);
        contexte.setAttribute(ATTRIBUT_ROUTES, registre);
        contexte.log("Framework : " + controleurs.size() + " contrôleur(s) et "
                + registre.taille() + " route(s) initialisés au démarrage."
                + (contexte.getAttribute(ConfigurationFramework.CONTEXTE_SPRING) == null
                        ? " Aucun contexte Spring détecté."
                        : " Contexte Spring publié sous '" + ConfigurationFramework.CONTEXTE_SPRING + "'."));
        return registre;
    }
}
