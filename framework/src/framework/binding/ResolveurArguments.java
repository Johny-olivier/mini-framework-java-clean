package framework.binding;

import framework.annotation.Param;
import jakarta.servlet.http.HttpServletRequest;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Construit les arguments à passer à une méthode de contrôleur à partir des
 * paramètres de la requête.
 *
 * Sprint 7 : types simples, avec @Param facultatif.
 * Sprint 7bis : si le paramètre est un objet, le framework l'instancie et
 * remplit ses propriétés avec les setters. Les objets imbriqués utilisent la
 * notation "adresse.rue". Un même formulaire peut alimenter plusieurs objets ;
 * le préfixe "nomParametre.propriete" est alors prioritaire sur la forme nue.
 */
public class ResolveurArguments {

    private static final int PROFONDEUR_MAX = 5;

    public Object[] resoudre(Method methode, HttpServletRequest requete) {
        Parameter[] parametres = methode.getParameters();
        Object[] arguments = new Object[parametres.length];
        Set<String> noms = nomsDesParametres(requete);
        boolean prefixeObligatoire = compterObjets(parametres) > 1;
        for (int i = 0; i < parametres.length; i++) {
            arguments[i] = resoudre(parametres[i], requete, noms, prefixeObligatoire);
        }
        return arguments;
    }

    private Object resoudre(Parameter parametre, HttpServletRequest requete, Set<String> noms,
            boolean prefixeObligatoire) {
        Param annotation = parametre.getAnnotation(Param.class);
        String nom = annotation != null ? annotation.value() : parametre.getName();
        Class<?> type = parametre.getType();

        if (type.isArray() || Iterable.class.isAssignableFrom(type)) {
            throw new IllegalStateException(
                    "Type de paramètre non pris en charge pour " + nom + " : " + type.getName());
        }
        if (Convertisseur.estSimple(type)) {
            return Convertisseur.convertir(requete.getParameter(nom), type, nom);
        }
        return lierObjet(type, nom, requete, noms, prefixeObligatoire);
    }

    private Object lierObjet(Class<?> type, String nom, HttpServletRequest requete, Set<String> noms,
            boolean prefixeObligatoire) {
        Object objet = instancier(type);
        // Forme nue : nom, prenom, adresse.rue...
        // Avec plusieurs objets en paramètre, seule la forme préfixée est acceptée
        // pour lever toute ambiguïté entre les objets.
        if (!prefixeObligatoire) {
            remplir(objet, type, requete, noms, "", true, 1);
        }
        // Forme préfixée par le nom du paramètre, prioritaire : employe.nom, employe.adresse.rue...
        remplir(objet, type, requete, noms, nom, false, 1);
        return objet;
    }

    private int compterObjets(Parameter[] parametres) {
        int total = 0;
        for (Parameter parametre : parametres) {
            if (!Convertisseur.estSimple(parametre.getType())) {
                total++;
            }
        }
        return total;
    }

    private Object instancier(Class<?> type) {
        try {
            var constructeur = type.getDeclaredConstructor();
            constructeur.setAccessible(true);
            return constructeur.newInstance();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalStateException("Le constructeur de " + type.getName() + " a échoué.", cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Impossible d'instancier " + type.getName()
                    + ". Un constructeur sans argument est requis pour le binding objet.", e);
        }
    }

    private void remplir(Object objet, Class<?> type, HttpServletRequest requete, Set<String> noms,
            String chemin, boolean racine, int profondeur) {
        if (profondeur > PROFONDEUR_MAX) {
            throw new MauvaisParametreException(
                    "Objet trop profondément imbriqué (maximum " + PROFONDEUR_MAX + " niveaux).");
        }

        for (PropertyDescriptor propriete : proprietes(type)) {
            Method setter = propriete.getWriteMethod();
            if (setter == null) {
                continue;
            }
            String nomPropriete = propriete.getName();
            String cheminPropriete = chemin.isEmpty() ? nomPropriete : chemin + "." + nomPropriete;
            Class<?> typePropriete = propriete.getPropertyType();

            if (Convertisseur.estSimple(typePropriete)) {
                String valeur = valeur(requete, noms, cheminPropriete, nomPropriete, racine);
                if (valeur != null) {
                    invoquerSetter(objet, setter, Convertisseur.convertir(valeur, typePropriete, cheminPropriete));
                }
            } else if (aDesParametresSous(noms, cheminPropriete)) {
                Object valeurNested = lireOuCreer(objet, propriete, typePropriete);
                remplir(valeurNested, typePropriete, requete, noms, cheminPropriete, false, profondeur + 1);
                invoquerSetter(objet, setter, valeurNested);
            }
        }
    }

    private String valeur(HttpServletRequest requete, Set<String> noms, String cheminPropriete,
            String nomPropriete, boolean racine) {
        if (noms.contains(cheminPropriete)) {
            return requete.getParameter(cheminPropriete);
        }
        if (racine && noms.contains(nomPropriete)) {
            return requete.getParameter(nomPropriete);
        }
        return null;
    }

    private Object lireOuCreer(Object objet, PropertyDescriptor propriete, Class<?> typePropriete) {
        Method getter = propriete.getReadMethod();
        if (getter != null) {
            try {
                Object existant = getter.invoke(objet);
                if (existant != null) {
                    return existant;
                }
            } catch (ReflectiveOperationException e) {
                // On crée une nouvelle instance ci-dessous.
            }
        }
        return instancier(typePropriete);
    }

    private void invoquerSetter(Object objet, Method setter, Object valeur) {
        try {
            setter.invoke(objet, valeur);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new MauvaisParametreException(
                    "Valeur refusée par le setter " + setter.getName() + " : " + cause.getMessage());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Impossible d'appeler " + setter.getName(), e);
        }
    }

    private PropertyDescriptor[] proprietes(Class<?> type) {
        try {
            BeanInfo info = Introspector.getBeanInfo(type, Object.class);
            return info.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Impossible d'analyser " + type.getName(), e);
        }
    }

    private boolean aDesParametresSous(Set<String> noms, String prefixe) {
        String debut = prefixe + ".";
        for (String nom : noms) {
            if (nom.startsWith(debut)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> nomsDesParametres(HttpServletRequest requete) {
        Enumeration<String> enumeration = requete.getParameterNames();
        if (enumeration == null) {
            return Collections.emptySet();
        }
        Set<String> noms = new HashSet<>();
        while (enumeration.hasMoreElements()) {
            noms.add(enumeration.nextElement());
        }
        return noms;
    }
}
