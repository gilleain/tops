package view;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import tops.view.diagram.AwtRenderer;
import tops.view.diagram.DiagramGenerator;
import tops.view.model.Shape;
import tops.view.util.ImageFactory;

public class TestRenderStack {
    
    public static void main(String[] args) throws IOException {
        String name = "blah";
//        String vertices = "NHEeEeEehHHHhHC";
        String vertices = "NHEeEeEehHC";
        String edges = "2:3A3:4A4:5A";
//        String edges = "";
        int width = 900;
        int height = 400;
        
        ImageFactory imageFactory = new ImageFactory(width, height);
        AwtRenderer renderer = new AwtRenderer();
        Rectangle2D canvas = new Rectangle2D.Double(0, 0, width, height);
        DiagramGenerator generator = new DiagramGenerator();
        Shape shape = generator.generate(vertices, edges, "");
        renderer.render(shape, imageFactory.getGraphics(), canvas);
        imageFactory.save("tmp.png");
    }

}
