package framework.scanner;

import framework.annotation.Controller;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControllerScanner {

    public static List<Class<?>> scan(String classesPath, String controllerPackages) {
        List<Class<?>> controllers = new ArrayList<>();
        File root = new File(classesPath);

        // Découpage des packages (séparateurs : virgule, point-virgule ou espace)
        String[] packages = controllerPackages.split("[,;\\s]+");

        scanDirectory(root, root, packages, controllers);
        return controllers;
    }

    private static void scanDirectory(File root, File current, String[] packages, List<Class<?>> controllers) {
        File[] files = current.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files);

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(root, file, packages, controllers);
            } else if (file.getName().endsWith(".class")) {
                try {
                    String className = file.getAbsolutePath()
                            .replace(root.getAbsolutePath() + File.separator, "")
                            .replace(File.separator, ".")
                            .replace(".class", "");

                    Class<?> clazz = Class.forName(className);
                    if (appartientAuxPackages(clazz, packages) && clazz.isAnnotationPresent(Controller.class)) {
                        controllers.add(clazz);
                    }
                } catch (Exception e) {
                    System.err.println("Impossible de charger : " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean appartientAuxPackages(Class<?> clazz, String[] packages) {
        for (String pkg : packages) {
            String prefixe = pkg.trim();
            if (prefixe.isEmpty()) {
                continue;
            }
            if (clazz.getName().equals(prefixe) || clazz.getName().startsWith(prefixe + ".")) {
                return true;
            }
        }
        return false;
    }
}
