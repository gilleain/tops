package tops.view.cartoon;

import java.io.OutputStream;

public interface ByteCartoonBuilder extends CartoonBuilder {

    void printProduct(OutputStream os);

}
