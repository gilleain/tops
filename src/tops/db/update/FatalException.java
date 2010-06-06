package tops.db.update;

public class FatalException extends Exception {

    public FatalException(String message) {
        super("Fatal Exception : " + message);
    }
}
