package framework.tests;

import framework.utils.Chemins;

public final class CheminsTest {

    private CheminsTest() {
    }

    public static void executer() {
        Verificateur.egal("/", Chemins.normaliser(null), "chemin nul ramené à la racine");
        Verificateur.egal("/", Chemins.normaliser(""), "chemin vide ramené à la racine");
        Verificateur.egal("/", Chemins.normaliser("/"), "racine conservée");
        Verificateur.egal("/users", Chemins.normaliser("users"), "slash initial ajouté");
        Verificateur.egal("/users", Chemins.normaliser("/users/"), "slash final supprimé");
        Verificateur.egal("/users", Chemins.normaliser("//users//"), "slashes superflus supprimés");
    }
}
