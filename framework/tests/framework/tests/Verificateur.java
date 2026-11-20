package framework.tests;

/**
 * Mini lanceur de tests, sans dépendance externe : chaque vérification
 * s'affiche, le programme renvoie 1 dès qu'une vérification échoue.
 */
public final class Verificateur {

    private static int total;
    private static int echecs;

    private Verificateur() {
    }

    public static void verifier(boolean condition, String description) {
        total++;
        if (condition) {
            System.out.println("  OK    " + description);
        } else {
            echecs++;
            System.out.println("  ECHEC " + description);
        }
    }

    public static void egal(Object attendu, Object obtenu, String description) {
        boolean identiques = attendu == null ? obtenu == null : attendu.equals(obtenu);
        verifier(identiques, description + " (attendu=" + attendu + ", obtenu=" + obtenu + ")");
    }

    public static void echoueAvec(Class<? extends Throwable> type, Runnable action, String description) {
        try {
            action.run();
            verifier(false, description + " (aucune exception levée)");
        } catch (Throwable e) {
            verifier(type.isInstance(e), description + " (" + e.getClass().getSimpleName() + ")");
        }
    }

    public static int bilan() {
        System.out.println();
        System.out.println(total + " vérification(s), " + echecs + " échec(s).");
        return echecs == 0 ? 0 : 1;
    }
}
