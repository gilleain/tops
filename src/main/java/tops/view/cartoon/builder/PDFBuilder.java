package tops.view.cartoon.builder;

//Make PDF files using the iText library

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
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

    private Graphics2D g; // for drawing

    private PDDocument document; // the product that this builder makes
    
    public PDFBuilder(Image image, Rectangle bb) {
    	try {
    		this.document = new PDDocument();

    		PDPage page = new PDPage();

    		this.g = (Graphics2D) image.getGraphics();

    		this.g.setColor(Color.white);
    		this.g.fillRect(0, 0, bb.width, bb.height);
    		this.g.setColor(Color.black);
    		this.g.drawRect(0, 0, bb.width - 2, bb.height - 2); // bounds
    		this.g.dispose();

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

    public void connect(int x1, int y1, int x2, int y2) {
        this.g.drawLine(x1, y1, x2, y2);
    }

    public void drawHelix(int x, int y, int r, Color c) {
        int d = 2 * r;
        int ex = x - r;
        int ey = y - r;

        if (c != null) {
            this.g.setColor(c);
            this.g.fillOval(ex, ey, d, d);
            this.g.setColor(Color.black);
        }

        this.g.drawOval(ex, ey, d, d);
    }

    public void drawStrand(int pointX, int pointY, int leftX, int leftY,
            int rightX, int rightY, Color c) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = pointX;
        xPoints[1] = leftX;
        xPoints[2] = rightX;

        yPoints[0] = pointY;
        yPoints[1] = leftY;
        yPoints[2] = rightY;
        if (c != null) {
            this.g.setColor(c);
            this.g.fillPolygon(xPoints, yPoints, 3);
            this.g.setColor(Color.black);
        }

        this.g.drawPolygon(xPoints, yPoints, 3);
    }

    public void drawTerminus(int x, int y, int r, String label) {
        // g.drawRect(x, y, x + r, y + r);
        if (label != null)
            this.g.drawString(label, x, y);
    }
}
