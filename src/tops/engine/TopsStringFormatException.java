package tops.engine;

public class TopsStringFormatException extends Exception {

    private static final String message = "Error in Tops string : ";

    public TopsStringFormatException(String topsString) {
        super(TopsStringFormatException.message + topsString);
    }
}
