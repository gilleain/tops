package tops.view.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageFactory {
      
    private Image image;
    
    public ImageFactory(int width, int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
    }
    
    public Graphics2D getGraphics() {
        return (Graphics2D) image.getGraphics();
    }
    
    public void save(String filename) throws IOException {
        ImageIO.write((RenderedImage)image, "PNG", new File(filename));
    }
    
    

}
