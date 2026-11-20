package framework.upload;

/**
 * Fichier reçu depuis un formulaire multipart.
 *
 * Le framework remplit cet objet à partir de la Part correspondante : le
 * contrôleur n'a jamais besoin de manipuler les API Jakarta Servlet.
 */
public class Fichier {

    private String nom;
    private String typeContenu;
    private long taille;
    private byte[] contenu;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTypeContenu() {
        return typeContenu;
    }

    public void setTypeContenu(String typeContenu) {
        this.typeContenu = typeContenu;
    }

    public long getTaille() {
        return taille;
    }

    public void setTaille(long taille) {
        this.taille = taille;
    }

    public byte[] getContenu() {
        return contenu;
    }

    public void setContenu(byte[] contenu) {
        this.contenu = contenu;
    }
}
