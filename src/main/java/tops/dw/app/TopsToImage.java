package tops.dw.app;

import java.io.*;
import java.util.*;
import java.awt.image.*;
import java.awt.*;

import javax.imageio.ImageIO;

import tops.dw.editor.TopsDrawCanvas;
import tops.dw.protein.*;

public class TopsToImage {

    static public void main(String args[]) {

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
            p = new Protein(inFile);
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

        Enumeration<SecStrucElement> Diagrams = p.getLinkedLists().elements();
        Enumeration<DomainDefinition> Domains = p.getDomainDefs().elements();

        int n = 0;
        Vector<TopsDrawCanvas> draw_canvs = new Vector<TopsDrawCanvas>();
        float MinScale = 1.0F, scale;
        while (Diagrams.hasMoreElements() && Domains.hasMoreElements()) {
            SecStrucElement root_sse = Diagrams.nextElement();
            DomainDefinition DomDef = Domains.nextElement();
            TopsDrawCanvas tdc = new TopsDrawCanvas(root_sse, DomDef.toString());
            tdc.setUseBorder(false);
            tdc.setSize(tdc.getPreferredSize());

            scale = tdc.getMaxScale();
            if (scale < MinScale)
                MinScale = scale;

            draw_canvs.addElement(tdc);
        }

        Enumeration<TopsDrawCanvas> canvs = draw_canvs.elements();
        while (canvs.hasMoreElements()) {

            n++;
            outfile = filestem + n + ".png";

            TopsDrawCanvas tdc = (TopsDrawCanvas) canvs.nextElement();
            tdc.SetCanvasCoordinates(MinScale);

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
