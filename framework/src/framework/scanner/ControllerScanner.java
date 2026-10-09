package framework.scanner;

import framework.annotation.Controller;
import java.io.File;
import java.util.ArrayList;
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
        if (files == null) return;

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
                    boolean startsWithPackage = false;
                    for (String pkg : packages) {
                        if (clazz.getName().startsWith(pkg.trim())) {
                            startsWithPackage = true;
                            break;
                        }
                    }
                    if (startsWithPackage && clazz.isAnnotationPresent(Controller.class)) {
                        controllers.add(clazz);
                    }
                } catch (Exception e) {
                    System.err.println("Impossible de charger : " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }
}