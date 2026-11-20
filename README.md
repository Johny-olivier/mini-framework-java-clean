# mini-framework-java

Mini framework Java web inspiré de Spring MVC, construit avec des servlets,
des JSP et Tomcat. Il gere la detection des controleurs, le routage par URL
et verbe HTTP, les vues JSP et les reponses JSON.

## Prerequis

- JDK 17 ou plus
- Tomcat 10 (Jakarta EE 9+)
- `CATALINA_HOME` pointe vers Tomcat (par defaut
  `/opt/tomcat/apache-tomcat-10.0.16`)

## Construction

```bash
./framework/compile.sh
```

Le script produit `framework/framework.jar` et compile avec `-parameters`
(necessaire pour utiliser le nom des parametres Java dans le binding).

## Tests

```bash
./test.sh
```

- `framework/test.sh` : tests unitaires du registre de routes et des chemins.
- `test-framework/test-integration.sh` : construit l'application, la deploie
  dans un Tomcat isole (port 18080 par defaut) et verifie les routes avec curl.
  Le Tomcat du systeme n'est pas modifie.

## Deploiement

```bash
./test-framework/deploy.sh
```

L'application de test est copiee dans `$CATALINA_HOME/webapps/test-framework`.
`TOMCAT_WEBAPPS` et `CONTEXTE` permettent de changer la destination.

## Configuration (`WEB-INF/web.xml`)

| Parametre | Role |
|---|---|
| `controller-package` | packages scannes au demarrage (separes par des virgules) |
| `view-prefix` | prefixe des vues, par exemple `/views/` |
| `view-suffix` | suffixe des vues, par exemple `.jsp` |

Le listener `framework.listener.InitialiseurApplication` scanne les
controleurs et construit la table des routes avant la premiere requete. Deux
methodes qui declarent la meme URL avec le meme verbe provoquent une erreur au
demarrage.

Le servlet est mappe sur `/` et non `/*` : avec `/*`, il intercepte aussi les
JSP et les vues renvoient 404.

## Binding des parametres

Une methode de controleur recoit les champs du formulaire par leur nom :

```java
@Mapping(url = "/employe", method = "POST")
public ModelAndView enregistrer(Employe employe) {
    return new ModelAndView("employe-resultat");
}
```

- Types simples : `String`, nombres, `boolean`, `char`, enums. `@Param("nom")`
  impose le nom du champ quand il differe du nom Java.
- Objet : le framework appelle le constructeur sans argument puis les setters.
  Les champs `nom`, `poste`, `salaire` remplissent directement l'objet.
- Objet imbrique : notation `adresse.rue`, `adresse.ville`.
- Plusieurs objets dans la meme methode : utiliser le prefixe
  `nomParametre.propriete` (`employe.nom`, `poste.libelle`) pour lever
  l'ambiguite.
- Une valeur numerique invalide renvoie HTTP 400.

La compilation doit conserver les noms de parametres (`-parameters`), sinon
`@Param` devient obligatoire.

## Sprints

| Sprint | Fonctionnalite | Etat |
|---|---|---|
| 0 | Front controller unique | fait |
| 1 | `@Controller` et scan des controleurs | fait |
| 2 | `@Mapping(url)` et liste des routes | fait |
| 3 | GET et POST sur une meme URL | fait |
| 3bis | Execution de la methode appelee | fait |
| 4 | Initialisation des routes au demarrage | fait |
| 5 | `ModelAndView` et prefixe/suffixe des vues | fait |
| 5bis | Conteneur Spring | a venir |
| 6 | `@ResponseBody` et reponse JSON | fait |
| 7 | Binding des parametres simples | fait |
| 7bis | Binding d'un objet | fait |
| 8 | Upload de fichier | a venir |
| 9 | Session HTTP | a venir |
| 10 | Authentification `@Auth` | a venir |
| 10bis | Autorisation `@RoleRequired` | a venir |
| 11 | Controleurs singleton | a venir |
