package tops.view.cartoon.builder;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;

import tops.view.cartoon.ByteCartoonBuilder;
import tops.view.cartoon.TextCartoonBuilder;

public class BuilderFactory {
    
    public ByteCartoonBuilder makeForImage(String type, Image image, Rectangle bb) throws IOException {
        if (type.equals("IMG")) {
            String name = "";   // TODO - move to interface method
            int width = bb.width;   // TODO
            int height = bb.height; // TODO
            return new IMGBuilder(image, name, bb, width, height);
        } else if (type.equals("PDF")) {
            return new PDFBuilder(image, bb);
        } else {
            throw new IOException("Unsupported output type : " + type);
        }
    }
    
    public TextCartoonBuilder make(String type, Rectangle bb) throws IOException {
        if (type.equals("SVG")) {
            return new SVGBuilder(bb);
        } else if (type.equals("PS")) {
            return new PSBuilder(bb);
        } else {
            throw new IOException("Unsupported output type : " + type);
        }
    }

}
