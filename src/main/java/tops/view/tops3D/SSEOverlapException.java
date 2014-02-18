package tops.view.tops3D;

public class SSEOverlapException extends Exception {

    public SSEOverlapException(String first, String second) {
        super("overlap between SSE " + first + " and SSE " + second + "\n");
    }
}
