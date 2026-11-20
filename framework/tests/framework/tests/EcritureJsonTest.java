package framework.tests;

import framework.utils.EcritureJson;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EcritureJsonTest {

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
        private double salaire;
        private Adresse adresse;

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public double getSalaire() {
            return salaire;
        }

        public void setSalaire(double salaire) {
            this.salaire = salaire;
        }

        public Adresse getAdresse() {
            return adresse;
        }

        public void setAdresse(Adresse adresse) {
            this.adresse = adresse;
        }
    }

    public static class Noeud {

        private String nom;
        private Noeud parent;

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public Noeud getParent() {
            return parent;
        }

        public void setParent(Noeud parent) {
            this.parent = parent;
        }
    }

    private EcritureJsonTest() {
    }

    public static void executer() {
        Verificateur.egal("null", EcritureJson.ecrire(null), "null");
        Verificateur.egal("42", EcritureJson.ecrire(42), "nombre");
        Verificateur.egal("true", EcritureJson.ecrire(true), "booléen");
        Verificateur.egal("\"a\\\"b\\nc\"", EcritureJson.ecrire("a\"b\nc"),
                "chaîne : guillemet et retour à la ligne échappés");

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("nom", "Johny");
        map.put("age", 20);
        Verificateur.egal("{\"nom\":\"Johny\",\"age\":20}", EcritureJson.ecrire(map), "map");

        Verificateur.egal("[1,2,3]", EcritureJson.ecrire(List.of(1, 2, 3)), "liste");
        Verificateur.egal("[1,2]", EcritureJson.ecrire(new int[] {1, 2}), "tableau primitif");

        Adresse adresse = new Adresse();
        adresse.setRue("Lalana");
        adresse.setVille("Antananarivo");

        Employe employe = new Employe();
        employe.setNom("Johny");
        employe.setSalaire(1234.5);
        employe.setAdresse(adresse);

        String json = EcritureJson.ecrire(employe);
        Verificateur.verifier(json.contains("\"nom\":\"Johny\""), "bean : propriété simple");
        Verificateur.verifier(json.contains("\"salaire\":1234.5"), "bean : nombre");
        Verificateur.verifier(json.contains("\"adresse\":{"), "bean : objet imbriqué");
        Verificateur.verifier(json.contains("\"rue\":\"Lalana\""), "bean : propriété imbriquée");

        Noeud noeud = new Noeud();
        noeud.setNom("racine");
        noeud.setParent(noeud);
        String cycle = EcritureJson.ecrire(noeud);
        Verificateur.verifier(cycle.contains("\"parent\":null"), "cycle coupé sans erreur");
    }
}
