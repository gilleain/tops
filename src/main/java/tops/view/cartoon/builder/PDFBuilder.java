package tops.view.cartoon.builder;

//Make PDF files using pdfbox

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import tops.view.cartoon.ByteCartoonBuilder;

public class PDFBuilder implements ByteCartoonBuilder {

    private static Logger logger = Logger.getLogger(PDFBuilder.class.getName());
    
    private GraphicsBuilder graphicsBuilder;

    private PDDocument document; // the product that this builder makes
    
    public PDFBuilder(Image image, Rectangle bb) {
        this.document = new PDDocument();
        graphicsBuilder = new GraphicsBuilder(image.getGraphics(), "", bb, bb.width, bb.height);
        
    	try (PDPageContentStream content = new PDPageContentStream(document, new PDPage())) {
    	    PDImageXObject ximage = LosslessFactory.createFromImage(document, (BufferedImage) image);
    		content.drawImage(ximage, 50, 400);
		} catch (IOException e) {
			logger.warning(e.toString());
		}
    }

    public void printProduct(OutputStream output) {
        try {
        	this.document.save(output);
			this.document.close();
		} catch (IOException e) {
		    logger.warning(e.toString());
		}
    }

    public void connect(Point p1, Point p2) {
        graphicsBuilder.connect(p1, p2);
    }

    public void drawHelix(Point center, int r, Color c) {
        graphicsBuilder.drawHelix(center, r, c);
    }

    public void drawStrand(Point center, Point left, Point right, Color c) {
        graphicsBuilder.drawStrand(center, left, right, c);
    }

    public void drawTerminus(Point center, int r, String label) {
        graphicsBuilder.drawTerminus(center, r, label);
    }

    @Override
    public void drawLabel(Point center, String text) {
        graphicsBuilder.drawLabel(center, text);
    }
}
