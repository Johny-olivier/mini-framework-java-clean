package framework.utils;

import framework.annotation.Mapping;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Table des routes du framework, construite une seule fois au démarrage.
 *
 * Une route est identifiée par le couple verbe HTTP + URL. Deux contrôleurs
 * qui déclarent la même combinaison provoquent une erreur immédiate : le
 * conflit est signalé au déploiement, pas au moment où un utilisateur appelle
 * l'URL.
 */
public class RegistreRoutes {

    private final Map<String, RouteInfo> routes = new LinkedHashMap<>();

    public void enregistrerTout(List<Class<?>> controleurs) {
        for (Class<?> controleur : controleurs) {
            enregistrer(controleur);
        }
    }

    public void enregistrer(Class<?> controleur) {
        for (Method methode : controleur.getDeclaredMethods()) {
            Mapping mapping = methode.getAnnotation(Mapping.class);
            if (mapping == null) {
                continue;
            }
            String cle = cle(mapping.method(), mapping.url());
            RouteInfo existante = routes.get(cle);
            if (existante != null) {
                throw new IllegalStateException(
                        "Route en double : " + cle
                        + " déclarée par " + decrire(existante)
                        + " et " + controleur.getName() + "#" + methode.getName());
            }
            routes.put(cle, new RouteInfo(controleur, methode));
        }
    }

    public RouteInfo trouver(String methodeHttp, String chemin) {
        return routes.get(cle(methodeHttp, chemin));
    }

    public boolean urlExiste(String chemin) {
        for (String cle : routes.keySet()) {
            if (cle.endsWith(":" + chemin)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> methodesAutorisees(String chemin) {
        Set<String> methodes = new LinkedHashSet<>();
        for (String cle : routes.keySet()) {
            int separateur = cle.indexOf(':');
            if (cle.substring(separateur + 1).equals(chemin)) {
                methodes.add(cle.substring(0, separateur));
            }
        }
        return methodes;
    }

    public Map<String, RouteInfo> getRoutes() {
        return Collections.unmodifiableMap(routes);
    }

    public int taille() {
        return routes.size();
    }

    /**
     * Liste lisible des routes, groupée par contrôleur. Sert au message 404.
     */
    public String decrire() {
        Map<String, List<Map.Entry<String, RouteInfo>>> parControleur = new TreeMap<>();
        for (Map.Entry<String, RouteInfo> entree : routes.entrySet()) {
            String nom = entree.getValue().getController().getSimpleName();
            parControleur.computeIfAbsent(nom, ignore -> new ArrayList<>()).add(entree);
        }

        StringBuilder texte = new StringBuilder("Controllers détectés :\n\n");
        for (Map.Entry<String, List<Map.Entry<String, RouteInfo>>> controleur : parControleur.entrySet()) {
            texte.append(controleur.getKey()).append('\n');
            for (Map.Entry<String, RouteInfo> route : controleur.getValue()) {
                texte.append("    - ").append(route.getKey())
                        .append("  -> ").append(route.getValue().getMethod().getName()).append("()\n");
            }
        }
        return texte.toString();
    }

    private String cle(String methodeHttp, String url) {
        return methodeHttp.toUpperCase() + ":" + url;
    }

    private String decrire(RouteInfo route) {
        return route.getController().getName() + "#" + route.getMethod().getName();
    }
}
