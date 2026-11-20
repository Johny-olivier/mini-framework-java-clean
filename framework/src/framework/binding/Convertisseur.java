package framework.binding;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Conversion des valeurs texte de la requête vers les types Java.
 * Une valeur invalide produit une MauvaisParametreException (réponse 400).
 */
public final class Convertisseur {

    private Convertisseur() {
    }

    public static boolean estSimple(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || type == Object.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Character.class
                || type.isEnum();
    }

    public static Object convertir(String valeur, Class<?> type, String nom) {
        if (type == String.class || type == Object.class) {
            return valeur;
        }
        if (valeur == null || valeur.isBlank()) {
            return valeurParDefaut(type);
        }

        String texte = valeur.trim();
        try {
            if (type == int.class || type == Integer.class) {
                return Integer.valueOf(texte);
            }
            if (type == long.class || type == Long.class) {
                return Long.valueOf(texte);
            }
            if (type == double.class || type == Double.class) {
                return Double.valueOf(texte);
            }
            if (type == float.class || type == Float.class) {
                return Float.valueOf(texte);
            }
            if (type == short.class || type == Short.class) {
                return Short.valueOf(texte);
            }
            if (type == byte.class || type == Byte.class) {
                return Byte.valueOf(texte);
            }
            if (type == boolean.class || type == Boolean.class) {
                return Boolean.valueOf(texte);
            }
            if (type == char.class || type == Character.class) {
                return texte.charAt(0);
            }
            if (type == BigDecimal.class) {
                return new BigDecimal(texte);
            }
            if (type == BigInteger.class) {
                return new BigInteger(texte);
            }
            if (type.isEnum()) {
                return convertirEnEnum(texte, type);
            }
        } catch (IllegalArgumentException e) {
            throw new MauvaisParametreException(
                    "Paramètre invalide : " + nom + "=" + valeur + ".");
        }

        throw new IllegalStateException("Type de paramètre non pris en charge : " + type.getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convertirEnEnum(String valeur, Class<?> type) {
        return Enum.valueOf((Class<? extends Enum>) type, valeur);
    }

    private static Object valeurParDefaut(Class<?> type) {
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
