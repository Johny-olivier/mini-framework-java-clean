package framework.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Écriture JSON minimale, sans dépendance externe.
 *
 * Gère null, nombres, booléens, chaînes (avec échappement), énumérations,
 * tableaux, Iterable, Map et objets JavaBean via leurs getters. Les cycles
 * sont coupés et la profondeur est bornée.
 */
public final class EcritureJson {

    private static final int PROFONDEUR_MAX = 10;

    private EcritureJson() {
    }

    public static String ecrire(Object valeur) {
        StringBuilder texte = new StringBuilder();
        ecrireValeur(valeur, texte, new IdentityHashMap<>(), 0);
        return texte.toString();
    }

    private static void ecrireValeur(Object valeur, StringBuilder texte,
            IdentityHashMap<Object, Boolean> pile, int profondeur) {
        if (valeur == null) {
            texte.append("null");
            return;
        }
        if (valeur instanceof Number || valeur instanceof Boolean) {
            texte.append(valeur);
            return;
        }
        if (valeur instanceof Character || valeur instanceof CharSequence || valeur instanceof Enum) {
            ecrireChaine(valeur.toString(), texte);
            return;
        }
        if (profondeur >= PROFONDEUR_MAX || pile.containsKey(valeur)) {
            texte.append("null");
            return;
        }

        pile.put(valeur, Boolean.TRUE);
        try {
            if (valeur instanceof Map) {
                ecrireMap((Map<?, ?>) valeur, texte, pile, profondeur);
            } else if (valeur instanceof Iterable) {
                ecrireIterable((Iterable<?>) valeur, texte, pile, profondeur);
            } else if (valeur.getClass().isArray()) {
                ecrireTableau(valeur, texte, pile, profondeur);
            } else {
                ecrireBean(valeur, texte, pile, profondeur);
            }
        } finally {
            pile.remove(valeur);
        }
    }

    private static void ecrireMap(Map<?, ?> map, StringBuilder texte,
            IdentityHashMap<Object, Boolean> pile, int profondeur) {
        texte.append('{');
        boolean premier = true;
        for (Map.Entry<?, ?> entree : map.entrySet()) {
            if (!premier) {
                texte.append(',');
            }
            ecrireChaine(String.valueOf(entree.getKey()), texte);
            texte.append(':');
            ecrireValeur(entree.getValue(), texte, pile, profondeur + 1);
            premier = false;
        }
        texte.append('}');
    }

    private static void ecrireIterable(Iterable<?> valeurs, StringBuilder texte,
            IdentityHashMap<Object, Boolean> pile, int profondeur) {
        texte.append('[');
        boolean premier = true;
        for (Object valeur : valeurs) {
            if (!premier) {
                texte.append(',');
            }
            ecrireValeur(valeur, texte, pile, profondeur + 1);
            premier = false;
        }
        texte.append(']');
    }

    private static void ecrireTableau(Object tableau, StringBuilder texte,
            IdentityHashMap<Object, Boolean> pile, int profondeur) {
        texte.append('[');
        int taille = Array.getLength(tableau);
        for (int i = 0; i < taille; i++) {
            if (i > 0) {
                texte.append(',');
            }
            ecrireValeur(Array.get(tableau, i), texte, pile, profondeur + 1);
        }
        texte.append(']');
    }

    private static void ecrireBean(Object bean, StringBuilder texte,
            IdentityHashMap<Object, Boolean> pile, int profondeur) {
        texte.append('{');
        boolean premier = true;
        for (PropertyDescriptor propriete : proprietes(bean.getClass())) {
            Method getter = propriete.getReadMethod();
            if (getter == null) {
                continue;
            }
            Object valeur;
            try {
                valeur = getter.invoke(bean);
            } catch (ReflectiveOperationException e) {
                continue;
            }
            if (!premier) {
                texte.append(',');
            }
            ecrireChaine(propriete.getName(), texte);
            texte.append(':');
            ecrireValeur(valeur, texte, pile, profondeur + 1);
            premier = false;
        }
        texte.append('}');
    }

    private static PropertyDescriptor[] proprietes(Class<?> type) {
        try {
            return Introspector.getBeanInfo(type, Object.class).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            return new PropertyDescriptor[0];
        }
    }

    private static void ecrireChaine(String valeur, StringBuilder texte) {
        texte.append('"');
        for (int i = 0; i < valeur.length(); i++) {
            char caractere = valeur.charAt(i);
            if (caractere == '"') {
                texte.append("\\\"");
            } else if (caractere == '\\') {
                texte.append("\\\\");
            } else if (caractere == '\n') {
                texte.append("\\n");
            } else if (caractere == '\r') {
                texte.append("\\r");
            } else if (caractere == '\t') {
                texte.append("\\t");
            } else if (caractere == '\b') {
                texte.append("\\b");
            } else if (caractere == '\f') {
                texte.append("\\f");
            } else if (caractere < 0x20) {
                texte.append(String.format("\\u%04x", (int) caractere));
            } else {
                texte.append(caractere);
            }
        }
        texte.append('"');
    }
}
