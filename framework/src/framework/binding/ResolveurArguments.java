package framework.binding;

import framework.annotation.Param;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Construit les arguments à passer à une méthode de contrôleur à partir des
 * paramètres de la requête.
 *
 * Sprint 7 : types simples, avec @Param facultatif. Le nom du paramètre Java
 * est utilisé par défaut, ce qui suppose une compilation avec -parameters.
 * Sprint 7bis étendra ce resolveur aux objets et aux fichiers.
 */
public class ResolveurArguments {

    public Object[] resoudre(Method methode, HttpServletRequest requete) {
        Parameter[] parametres = methode.getParameters();
        Object[] arguments = new Object[parametres.length];
        for (int i = 0; i < parametres.length; i++) {
            arguments[i] = resoudre(parametres[i], requete);
        }
        return arguments;
    }

    private Object resoudre(Parameter parametre, HttpServletRequest requete) {
        Param annotation = parametre.getAnnotation(Param.class);
        String nom = annotation != null ? annotation.value() : parametre.getName();
        return convertir(requete.getParameter(nom), parametre.getType(), nom);
    }

    private Object convertir(String valeur, Class<?> type, String nom) {
        if (type == String.class || type == Object.class) {
            return valeur;
        }
        if (valeur == null || valeur.isBlank()) {
            return valeurParDefaut(type);
        }
        try {
            if (type == int.class || type == Integer.class) {
                return Integer.valueOf(valeur.trim());
            }
            if (type == long.class || type == Long.class) {
                return Long.valueOf(valeur.trim());
            }
            if (type == double.class || type == Double.class) {
                return Double.valueOf(valeur.trim());
            }
            if (type == float.class || type == Float.class) {
                return Float.valueOf(valeur.trim());
            }
            if (type == boolean.class || type == Boolean.class) {
                return Boolean.valueOf(valeur.trim());
            }
            if (type == short.class || type == Short.class) {
                return Short.valueOf(valeur.trim());
            }
            if (type == byte.class || type == Byte.class) {
                return Byte.valueOf(valeur.trim());
            }
            if (type == char.class || type == Character.class) {
                return valeur.charAt(0);
            }
        } catch (NumberFormatException e) {
            throw new MauvaisParametreException(
                    "Paramètre invalide : " + nom + "=" + valeur + " (valeur numérique attendue).");
        }
        return valeur;
    }

    private Object valeurParDefaut(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == double.class) {
            return 0.0;
        }
        if (type == float.class) {
            return 0.0f;
        }
        if (type == short.class) {
            return (short) 0;
        }
        return (byte) 0;
    }
}
