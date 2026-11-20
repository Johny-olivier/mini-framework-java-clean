package framework.tests;

public final class LanceurTests {

    private LanceurTests() {
    }

    public static void main(String[] args) {
        System.out.println("== RegistreRoutes ==");
        RegistreRoutesTest.executer();

        System.out.println();
        System.out.println("== Chemins ==");
        CheminsTest.executer();

        System.exit(Verificateur.bilan());
    }
}
