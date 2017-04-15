package tops.view.cartoon.builder;

//Make PDF files using the iText library

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

import tops.view.cartoon.ByteCartoonBuilder;

public class PDFBuilder implements ByteCartoonBuilder {

    private GraphicsBuilder graphicsBuilder;

    private PDDocument document; // the product that this builder makes
    
    public PDFBuilder(Image image, Rectangle bb) {
    	try {
    		this.document = new PDDocument();

    		PDPage page = new PDPage();
    		
    		graphicsBuilder = new GraphicsBuilder(image.getGraphics(), "", bb, bb.width, bb.height);

    		PDXObjectImage ximage = new PDJpeg(document, (BufferedImage) image);
    		PDPageContentStream content = new PDPageContentStream(document, page);
    		content.drawImage(ximage, 50, 400);
    		content.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void printProduct(OutputStream output) {
        try {
        	this.document.save(output);
			this.document.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (COSVisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
