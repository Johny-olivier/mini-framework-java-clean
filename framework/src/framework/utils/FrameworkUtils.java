package framework.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FrameworkUtils {
    // on va getter les fichiers qui ont une anotation (que ce soit @Controller, @Repository, ...) au niveau de la classe ou au niveau des attributs ou au niveau de methodes
    // sur la path (src/ o seulement src/controller, ...)
    public static List<String> getFichiersWhoHas(String annotation, String niveau, String strPath) {
        
        if (strPath == null) {
            strPath = "src/";
        }

        List<String> fichiers = new ArrayList<>();

        Path path = Paths.get(strPath);

        try (Stream<Path> stream = Files.walk(path)) {
            stream
                .filter(Files::isRegularFile)
                .filter(pth -> pth.getFileName().toString().toLowerCase().contains(annotation.toLowerCase()))
                .forEach(pth -> fichiers.add(pth.toString()));

        } catch (Exception e) {
            System.err.println("Erreur lors du parcours du dossier : " + e.getMessage());
        }

        return fichiers;
    }
}
