package tops.view.tops2D.cartoon.builder;

//Make PDF files using the iText library

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;

import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import tops.view.tops2D.cartoon.CartoonBuilder;

public class PDFBuilder implements CartoonBuilder {

    private Graphics2D g; // for drawing

    private Document document; // the product that this builder makes

    public PDFBuilder(Rectangle bb, OutputStream out) {

        this.document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(this.document, out);
            this.document.open();

            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(bb.width, bb.height);
            tp.setWidth(bb.width);
            tp.setHeight(bb.height);

            this.g = tp.createGraphics(bb.width, bb.height);

            this.g.setColor(Color.white);
            this.g.fillRect(0, 0, bb.width, bb.height);
            this.g.setColor(Color.black);
            this.g.drawRect(0, 0, bb.width - 2, bb.height - 2); // bounds
            this.g.dispose();
            cb.addTemplate(tp, 50, 400);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        }
    }

    public void printProduct() {
        this.document.close();
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
