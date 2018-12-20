package tops.cli.view;

import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.dw.io.TopsFileReader;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.TopsFileFormatException;
import tops.port.model.DomainDefinition;
import tops.web.display.applet.TopsDrawCanvas;

public class TopsToImageCommand extends BaseCommand {

    @Override
    public String getDescription() {
        return "Make a tops file into an image";
    }

    @Override
    public String getHelp() {
        return "InputFile.tops";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        TopsToImageCommand tti = new TopsToImageCommand();

        String infile = args[0];
        if (tti.makeImageFiles(infile) == 0) {
            output("TopsToImage completed successfully");
        } else {
            output("TopsToImage failed");
        }
    }
    

    public int makeImageFiles(String infile) {

        File inFile = new File(infile);
        String filestem = this.getFileStem(infile);
        String outfile;
        Protein p;

        try {
            p = new TopsFileReader().readTopsFile(inFile);
        } catch (FileNotFoundException e1) {
            error("Error: file " + inFile.toString() + "not found");
            return 1;
        } catch (TopsFileFormatException e1) {
            error("Error: format error in file " + inFile.toString());
            return 1;
        } catch (IOException e1) {
            error("Error: IO error reading file "+ inFile.toString());
            return 1;
        }

        List<Cartoon> diagrams = p.getLinkedLists();
        List<DomainDefinition> domains = p.getDomainDefs();

        int n = 0;
        List<TopsDrawCanvas> drawCanvases = new ArrayList<>();
        float minScale = 1.0F;
        float scale;
        int index = 0;
        for (Cartoon rootSSE : diagrams) {
            DomainDefinition domDefinition = domains.get(index);
            TopsDrawCanvas tdc = new TopsDrawCanvas(rootSSE, domDefinition.toString());
            
            tdc.setBorder(42);
            tdc.setMinHeight(280);
            tdc.setMinWidth(360);
            tdc.setPrefHeight(280);
            tdc.setPrefWidth(360);
            
            tdc.setUseBorder(false);
            tdc.setSize(tdc.getPreferredSize());

            scale = tdc.getMaxScale();
            if (scale < minScale)
                minScale = scale;

            drawCanvases.add(tdc);
            index++;
        }

        for (TopsDrawCanvas tdc : drawCanvases) {

            n++;
            outfile = filestem + n + ".png";

            tdc.setCanvasCoordinates(minScale);

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
            } catch (IOException ioe) {
                error("Error: IO problem while printing image");
                return 1;
            }
        }

        return 0;

    }

    private void printImage(String outfile, int w, int h, Image img) throws IOException {
        BufferedImage bufferedImg = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImg.createGraphics();
        g2.drawImage(img, null, null);
        ImageIO.write(bufferedImg, "PNG", new File(outfile));
    }


    private String getFileStem(String filename) {
        int seppt = filename.lastIndexOf('.');
        if (seppt >= filename.length() || seppt < 0)
            seppt = filename.length();

        return filename.substring(0, seppt);

    }

}
