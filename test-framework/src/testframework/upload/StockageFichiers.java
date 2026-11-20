package testframework.upload;

import framework.upload.Fichier;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Stockage des fichiers uploadés dans l'application de test.
 *
 * Le dossier cible est lu dans WEB-INF/upload.properties. Un chemin relatif
 * est résolu sous la racine de l'application ; un chemin absolu est utilisé
 * tel quel. Les noms sont assainis et jamais écrasés.
 */
public class StockageFichiers {

    private final Path dossier;

    public StockageFichiers(Path dossier) {
        this.dossier = dossier;
    }

    public static Path lireDossier(ServletContext contexte) {
        Properties proprietes = new Properties();
        try (InputStream flux = contexte.getResourceAsStream("/WEB-INF/upload.properties")) {
            if (flux == null) {
                throw new IllegalStateException("WEB-INF/upload.properties introuvable.");
            }
            proprietes.load(flux);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire WEB-INF/upload.properties.", e);
        }

        String configure = proprietes.getProperty("dossier", "uploads").trim();
        Path chemin = Paths.get(configure);
        if (!chemin.isAbsolute()) {
            chemin = Paths.get(contexte.getRealPath("/")).resolve(chemin);
        }
        return chemin.normalize();
    }

    public Path enregistrer(Fichier fichier) throws IOException {
        Files.createDirectories(dossier);
        Path cible = dossier.resolve(nomUnique(nomSur(fichier.getNom())));
        Files.write(cible, fichier.getContenu());
        return cible;
    }

    private String nomSur(String nom) {
        String base = nom == null ? "" : nom.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).trim();
        base = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (base.isEmpty() || base.equals(".") || base.equals("..")) {
            return "fichier";
        }
        return base;
    }

    private String nomUnique(String nom) {
        if (!Files.exists(dossier.resolve(nom))) {
            return nom;
        }

        String base = nom;
        String extension = "";
        int point = nom.lastIndexOf('.');
        if (point > 0) {
            base = nom.substring(0, point);
            extension = nom.substring(point);
        }

        int compteur = 1;
        String candidat;
        do {
            candidat = base + "-" + System.currentTimeMillis() + "-" + compteur + extension;
            compteur++;
        } while (Files.exists(dossier.resolve(candidat)));
        return candidat;
    }
}
