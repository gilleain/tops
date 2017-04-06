package tops.port.model;

import java.util.List;

public class Cartoon extends Chain {

    public Cartoon(char nameChar) {
        super(nameChar);
    }


    public Cartoon(SSE newRoot) {
        super(' '); // XXX
        // TODO Auto-generated constructor stub
    }


    public Cartoon(List<SSE> sses) {
        super(' ');
        for (SSE sse : sses) {
            addSSE(sse);
        }
    }

}
