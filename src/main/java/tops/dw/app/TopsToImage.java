package tops.dw.app;

import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import tops.dw.io.TopsFileReader;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.TopsFileFormatException;
import tops.port.model.DomainDefinition;
import tops.web.display.applet.TopsDrawCanvas;

public class TopsToImage {

    public static void main(String args[]) {    // TODO: make this a command

        TopsToImage tti = new TopsToImage();

        if (args.length != 1) {
            System.out.println("Usage: TopsToImage InputFile.tops");
        } else {
            String infile = args[0];
            if (tti.makeImageFiles(infile) == 0) {
                System.out.println("TopsToImage completed successfully");
            } else {
                System.out.println("TopsToImage failed");
            }
        }

    }

    private int makeImageFiles(String infile) {

        File inFile = new File(infile);
        String filestem = this.getFileStem(infile);
        String outfile;
        Protein p;

        try {
            p = new TopsFileReader().readTopsFile(inFile);
        } catch (FileNotFoundException e1) {
            System.out
                    .println("Error: file " + inFile.toString() + "not found");
            return 1;
        } catch (TopsFileFormatException e1) {
            System.out.println("Error: format error in file "
                    + inFile.toString());
            return 1;
        } catch (IOException e1) {
            System.out.println("Error: IO error reading file "
                    + inFile.toString());
            return 1;
        }

        TopsDrawCanvas.BORDER = 42;
        TopsDrawCanvas.MIN_HEIGHT = 280;
        TopsDrawCanvas.MIN_WIDTH = 360;
        TopsDrawCanvas.PREF_HEIGHT = 280;
        TopsDrawCanvas.PREF_WIDTH = 360;

        List<Cartoon> diagrams = p.getLinkedLists();
        List<DomainDefinition> domains = p.getDomainDefs();

        int n = 0;
        Vector<TopsDrawCanvas> draw_canvs = new Vector<TopsDrawCanvas>();
        float MinScale = 1.0F, scale;
        int index = 0;
        for (Cartoon rootSSE : diagrams) {
            DomainDefinition domDefinition = domains.get(index);
            TopsDrawCanvas tdc = new TopsDrawCanvas(rootSSE, domDefinition.toString());
            tdc.setUseBorder(false);
            tdc.setSize(tdc.getPreferredSize());

            scale = tdc.getMaxScale();
            if (scale < MinScale)
                MinScale = scale;

            draw_canvs.addElement(tdc);
            index++;
        }

        Enumeration<TopsDrawCanvas> canvs = draw_canvs.elements();
        while (canvs.hasMoreElements()) {

            n++;
            outfile = filestem + n + ".png";

            TopsDrawCanvas tdc = (TopsDrawCanvas) canvs.nextElement();
            tdc.setCanvasCoordinates(MinScale);

            Frame f = new Frame("Dummy frame");
            Panel pn = new Panel();
            pn.add(tdc);
            pn.setSize(tdc.getPreferredSize());
            f.add(pn);
            f.setSize(tdc.getPreferredSize());
            f.setVisible(true);

            Image img = tdc.getImage();
            int w = tdc.getSize().width;
            int h = tdc.getSize().height;
            try {
                this.printImage(outfile, w, h, img);
                f.setVisible(false);
                f.dispose();
            } catch (InterruptedException ie) {
                System.out.println("Error: interrupted while printing image");
                return 1;
            } catch (IOException ioe) {
                System.out.println("Error: IO problem while printing image");
                return 1;
            }
        }

        return 0;

    }

    private void printImage(String outfile, int w, int h, Image img)
            throws InterruptedException, IOException {
        BufferedImage bufferedImg = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImg.createGraphics();
        g2.drawImage(img, null, null);
        ImageIO.write(bufferedImg, "PNG", new File(outfile));
    }


    private String getFileStem(String Filename) {
        int seppt = Filename.lastIndexOf(".");
        if (seppt >= Filename.length() || seppt < 0)
            seppt = Filename.length();

        return Filename.substring(0, seppt);

    }
    
}
