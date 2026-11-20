package framework.tests;

public final class LanceurTests {

    private LanceurTests() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== RegistreRoutes ==");
        RegistreRoutesTest.executer();

        System.out.println();
        System.out.println("== Chemins ==");
        CheminsTest.executer();

        System.out.println();
        System.out.println("== ResolveurArguments ==");
        ResolveurArgumentsTest.executer();

        System.out.println();
        System.out.println("== EcritureJson ==");
        EcritureJsonTest.executer();

        System.out.println();
        System.out.println("== FrameworkSession ==");
        FrameworkSessionTest.executer();

        System.exit(Verificateur.bilan());
    }
}
