package framework.session;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulation de la session HTTP du conteneur, pour que les contrôleurs
 * manipulent la session sans toucher aux API Jakarta.
 *
 * La session réelle est toujours consultée à chaque appel : aucune copie
 * locale des attributs n'est conservée.
 */
public class FrameworkSession {

    private final HttpSession session;

    public FrameworkSession(HttpSession session) {
        this.session = Objects.requireNonNull(session, "session ne doit pas être null");
    }

    public void setAttribute(String cle, Object valeur) {
        session.setAttribute(Objects.requireNonNull(cle, "cle ne doit pas être null"), valeur);
    }

    public Object getAttribute(String cle) {
        return session.getAttribute(cle);
    }

    public <T> T getAttribute(String cle, Class<T> type) {
        Objects.requireNonNull(type, "type ne doit pas être null");
        Object valeur = session.getAttribute(cle);
        if (valeur == null) {
            return null;
        }
        if (!type.isInstance(valeur)) {
            throw new IllegalStateException("L'attribut '" + cle + "' est de type "
                    + valeur.getClass().getName() + ", type attendu : " + type.getName());
        }
        return type.cast(valeur);
    }

    public void removeAttribute(String cle) {
        session.removeAttribute(cle);
    }

    /** Copie des attributs présents : modifier la map retournée ne modifie pas la session. */
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributs = new HashMap<>();
        Enumeration<String> noms = session.getAttributeNames();
        while (noms.hasMoreElements()) {
            String nom = noms.nextElement();
            attributs.put(nom, session.getAttribute(nom));
        }
        return attributs;
    }

    public void clear() {
        List<String> aSupprimer = new ArrayList<>();
        Enumeration<String> noms = session.getAttributeNames();
        while (noms.hasMoreElements()) {
            aSupprimer.add(noms.nextElement());
        }
        for (String nom : aSupprimer) {
            session.removeAttribute(nom);
        }
    }

    public String getId() {
        return session.getId();
    }
}
