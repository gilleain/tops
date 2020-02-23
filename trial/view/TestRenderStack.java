package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import tops.view.diagram.AwtRenderer;
import tops.view.diagram.DiagramGenerator;
import tops.view.model.Shape;

public class TestRenderStack {
    
    public static void main(String[] args) throws IOException {
        String name = "blah";
        String vertices = "NEeEeEeC";
        String edges = "1:2A2:3A3:4A";
        int width = 800;
        int height = 600;
        
        Image image = new BufferedImage(
                width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) image.getGraphics();
//        g.setColor(Color.GRAY);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        AwtRenderer renderer = new AwtRenderer();
        Rectangle2D canvas = new Rectangle2D.Double(0, 0, width, height);
        DiagramGenerator generator = new DiagramGenerator();
        Shape shape = generator.generate(vertices, edges, "");
        renderer.render(shape, g, canvas);
        ImageIO.write((RenderedImage)image, "PNG", new File("tmp.png"));
    }

}
