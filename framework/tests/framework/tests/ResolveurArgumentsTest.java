package framework.tests;

import framework.annotation.Param;
import framework.binding.MauvaisParametreException;
import framework.binding.ResolveurArguments;
import java.lang.reflect.Method;
import java.util.Map;

public final class ResolveurArgumentsTest {

    public enum Couleur {
        ROUGE, VERT
    }

    public static class Adresse {

        private String rue;
        private String ville;

        public String getRue() {
            return rue;
        }

        public void setRue(String rue) {
            this.rue = rue;
        }

        public String getVille() {
            return ville;
        }

        public void setVille(String ville) {
            this.ville = ville;
        }
    }

    public static class Employe {

        private String nom;
        private String poste;
        private double salaire;
        private boolean actif;
        private Adresse adresse;

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getPoste() {
            return poste;
        }

        public void setPoste(String poste) {
            this.poste = poste;
        }

        public double getSalaire() {
            return salaire;
        }

        public void setSalaire(double salaire) {
            this.salaire = salaire;
        }

        public boolean isActif() {
            return actif;
        }

        public void setActif(boolean actif) {
            this.actif = actif;
        }

        public Adresse getAdresse() {
            return adresse;
        }

        public void setAdresse(Adresse adresse) {
            this.adresse = adresse;
        }
    }

    public static class Poste {

        private String libelle;
        private int niveau;

        public String getLibelle() {
            return libelle;
        }

        public void setLibelle(String libelle) {
            this.libelle = libelle;
        }

        public int getNiveau() {
            return niveau;
        }

        public void setNiveau(int niveau) {
            this.niveau = niveau;
        }
    }

    public static class SansConstructeur {

        private final String valeur;

        public SansConstructeur(String valeur) {
            this.valeur = valeur;
        }

        public String getValeur() {
            return valeur;
        }
    }

    public static class Controleur {

        public void simple(String q, int page) {
        }

        public void avecAnnotation(@Param("recherche") String texte) {
        }

        public void objet(Employe employe) {
        }

        public void deuxObjets(Employe employe, Poste poste) {
        }

        public void avecEnum(Couleur couleur) {
        }
    }

    public static class ControleurInvalide {

        public void impossible(SansConstructeur valeur) {
        }
    }

    private static final ResolveurArguments RESOLVEUR = new ResolveurArguments();

    private ResolveurArgumentsTest() {
    }

    public static void executer() throws Exception {
        testerTypesSimples();
        testerObjetSimple();
        testerObjetImbrique();
        testerFormePrefixee();
        testerDeuxObjets();
        testerValeursInvalides();
        testerConstructeurAbsent();
        testerEnum();
    }

    private static void testerTypesSimples() throws Exception {
        Method methode = Controleur.class.getDeclaredMethod("simple", String.class, int.class);
        Object[] arguments = RESOLVEUR.resoudre(methode,
                RequeteFactice.creer(Map.of("q", "livre", "page", "2")));

        Verificateur.egal("livre", arguments[0], "binding String");
        Verificateur.egal(2, arguments[1], "binding int");

        Object[] manquants = RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of()));
        Verificateur.egal(null, manquants[0], "String absent reste null");
        Verificateur.egal(0, manquants[1], "int absent vaut 0");

        Method annotation = Controleur.class.getDeclaredMethod("avecAnnotation", String.class);
        Object[] avec = RESOLVEUR.resoudre(annotation, RequeteFactice.creer(Map.of("recherche", "velo")));
        Verificateur.egal("velo", avec[0], "@Param impose le nom du champ");
    }

    private static void testerObjetSimple() throws Exception {
        Method methode = Controleur.class.getDeclaredMethod("objet", Employe.class);
        Object[] arguments = RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of(
                "nom", "Johny",
                "poste", "Developpeur",
                "salaire", "1234.5",
                "actif", "true")));

        Employe employe = (Employe) arguments[0];
        Verificateur.egal("Johny", employe.getNom(), "objet : nom");
        Verificateur.egal("Developpeur", employe.getPoste(), "objet : poste");
        Verificateur.egal(1234.5, employe.getSalaire(), "objet : double");
        Verificateur.verifier(employe.isActif(), "objet : boolean");
        Verificateur.egal(null, employe.getAdresse(), "objet : adresse absente reste null");
    }

    private static void testerObjetImbrique() throws Exception {
        Method methode = Controleur.class.getDeclaredMethod("objet", Employe.class);
        Object[] arguments = RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of(
                "nom", "Johny",
                "adresse.rue", "Lalana",
                "adresse.ville", "Antananarivo")));

        Employe employe = (Employe) arguments[0];
        Verificateur.verifier(employe.getAdresse() != null, "objet imbriqué créé");
        Verificateur.egal("Lalana", employe.getAdresse().getRue(), "objet imbriqué : rue");
        Verificateur.egal("Antananarivo", employe.getAdresse().getVille(), "objet imbriqué : ville");
    }

    private static void testerFormePrefixee() throws Exception {
        Method methode = Controleur.class.getDeclaredMethod("objet", Employe.class);
        Object[] arguments = RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of(
                "employe.nom", "Prefixe",
                "employe.adresse.ville", "Toamasina")));

        Employe employe = (Employe) arguments[0];
        Verificateur.egal("Prefixe", employe.getNom(), "objet : préfixe employe.nom");
        Verificateur.egal("Toamasina", employe.getAdresse().getVille(), "objet : préfixe imbriqué");
    }

    private static void testerDeuxObjets() throws Exception {
        Method methode = Controleur.class.getDeclaredMethod("deuxObjets", Employe.class, Poste.class);
        Object[] arguments = RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of(
                "employe.nom", "Johny",
                "poste.libelle", "Developpeur",
                "poste.niveau", "3")));

        Employe employe = (Employe) arguments[0];
        Poste poste = (Poste) arguments[1];
        Verificateur.egal("Johny", employe.getNom(), "deux objets : premier objet");
        Verificateur.egal("Developpeur", poste.getLibelle(), "deux objets : second objet");
        Verificateur.egal(3, poste.getNiveau(), "deux objets : second objet int");

        Object[] ambigus = RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of(
                "nom", "SansPrefixe",
                "libelle", "SansPrefixe")));
        Verificateur.egal(null, ((Employe) ambigus[0]).getNom(),
                "deux objets : forme nue ignorée pour lever l'ambiguïté");
        Verificateur.egal(null, ((Poste) ambigus[1]).getLibelle(),
                "deux objets : second objet non pollué");
    }

    private static void testerValeursInvalides() throws Exception {
        Method methode = Controleur.class.getDeclaredMethod("objet", Employe.class);
        Verificateur.echoueAvec(MauvaisParametreException.class,
                () -> RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of("salaire", "abc"))),
                "nombre invalide rejeté");
    }

    private static void testerConstructeurAbsent() throws Exception {
        Method methode = ControleurInvalide.class.getDeclaredMethod("impossible", SansConstructeur.class);
        Verificateur.echoueAvec(IllegalStateException.class,
                () -> RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of())),
                "objet sans constructeur sans argument rejeté");
    }

    private static void testerEnum() throws Exception {
        Method methode = Controleur.class.getDeclaredMethod("avecEnum", Couleur.class);
        Object[] arguments = RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of("couleur", "VERT")));
        Verificateur.egal(Couleur.VERT, arguments[0], "binding enum");

        Verificateur.echoueAvec(MauvaisParametreException.class,
                () -> RESOLVEUR.resoudre(methode, RequeteFactice.creer(Map.of("couleur", "BLEU"))),
                "valeur d'enum inconnue rejetée");
    }
}
