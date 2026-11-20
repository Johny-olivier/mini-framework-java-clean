package framework.binding;

/**
 * Signale une donnée de requête absente ou invalide. Le servlet la traduit
 * en réponse HTTP 400.
 */
public class MauvaisParametreException extends RuntimeException {

    public MauvaisParametreException(String message) {
        super(message);
    }
}
