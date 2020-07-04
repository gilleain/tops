package view;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import tops.view.diagram.AwtRenderer;
import tops.view.diagram.DiagramGenerator;
import tops.view.model.Shape;
import tops.view.util.ImageFactory;

public class TestRenderStack {
    
    public static void makeMultipleEdges(String filename) throws IOException {
        String vertices = "NHEeEeEehHC";
        String edges = "2:3A3:4A4:5A";
        DiagramGenerator generator = new DiagramGenerator();
        render(generator.generate(vertices, edges, ""), filename);
    }
    
    public static void makeSingleEdge(String filename) throws IOException {
        String vertices = "NEEC";
        String edges = "1:2P";
        DiagramGenerator generator = new DiagramGenerator();
        render(generator.generate(vertices, edges, ""), filename);
    }
    
    public static void render(Shape shape, String filename) throws IOException {
        int width = 900;
        int height = 400;
        
        ImageFactory imageFactory = new ImageFactory(width, height);
        AwtRenderer renderer = new AwtRenderer();
        Rectangle2D canvas = new Rectangle2D.Double(0, 0, width, height);
        
        renderer.render(shape, imageFactory.getGraphics(), canvas);
        imageFactory.save(filename);
    }
    
    public static void main(String[] args) throws IOException {
        makeSingleEdge("single_edge.png");
        makeMultipleEdges("multiple_edges.png");
       
    }

}
