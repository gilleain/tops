package tops.db.update;

public class FatalException extends Exception {

	private static final long serialVersionUID = 7077404518035578274L;

	public FatalException(String message) {
        super("Fatal Exception : " + message);
    }
}
