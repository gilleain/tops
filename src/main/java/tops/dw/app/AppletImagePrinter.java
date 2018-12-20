package tops.dw.app;

import java.net.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class AppletImagePrinter {

    private Image image = null;

    private String host = null;

    private int port = 80;

    private String cgiProg = null;

    private String errorString = "No error";

    public AppletImagePrinter(Image img, String host, int port, String cgiPath, String cgiPrintProg) {
        this.image = img;
        this.host = host;
        this.port = port;
        this.cgiProg = cgiPath + cgiPrintProg;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public URL doPrint() {

        URL target = null;

        if (this.host == null) {
            this.errorString = "AppletImagePrinter: No Host";
            return null;
        }

        if (this.cgiProg == null) {
            this.errorString = "AppletImagePrinter: No CGI program";
            return null;
        }

        if (this.image == null) {
            this.errorString = "AppletImagePrinter: No Image";
            return null;
        }

        String query = this.formQuery(this.image);

        if (query != null) {

            CGIrequest cgir = new CGIrequest(this.host, this.port, "POST", this.cgiProg, query);
            InputStream cgiResponse = cgir.doRequest();

            if (cgiResponse == null) {
                System.out.println(cgir.getErrorString());
                this.errorString = "AppletImagePrinter: CGI error";
                target = null;
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(cgiResponse))) {
                    String line;

                    /* expect URL_FOLLOWS in response */
                    line = br.readLine();
                    while ((line != null) && (!line.equals("URL_FOLLOWS"))) {
                        line = br.readLine();
                    }

                    if ("URL_FOLLOWS".equals(line)) {
                        line = br.readLine();
                        target = getURL(line);
                    } else {
                        target = null;
                        this.errorString = "AppletImagePrinter: Error unexpected CGI response";
                    }
                } catch (IOException ioe) {
                    this.errorString = "AppletImagePrinter: Error reading CGI response";
                    target = null;
                }
            }

            try {
                cgir.close();
            } catch (IOException e) {
            }

        } else {
            this.errorString = "AppletImagePrinter: Error forming query";
            target = null;
        }

        return target;
    }
    
    private URL getURL(String line) {
        if (line != null) {
            try {
                return new URL(line);
            } catch (MalformedURLException urle) {
                this.errorString = "AppletImagePrinter: URL Error";
                return null;
            }
        } else {
            this.errorString = "AppletImagePrinter: Error unexpected CGI response";
            return null;
        }
    }

    private String formQuery(Image img) {

        String query = null;

        if (img == null)
            return null;

        int w = 500;
        int h = 400;

        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        boolean success = false;
        try {
            success = pg.grabPixels();
        } catch (InterruptedException e) {
            return null;
        }

        if (success) {
            ColorModel cm = pg.getColorModel();

            StringBuilder sb = new StringBuilder();
            sb.append("P3 " + w + " " + h + " 255\n");

            int[] background = new int[3];
            this.getBackground(cm, pixels, background);

            for (int i = 0; i < w * h; i++) {

                int red = cm.getRed(pixels[i]);
                int green = cm.getGreen(pixels[i]);
                int blue = cm.getBlue(pixels[i]);

                if (red != background[0] || green != background[1] || blue != background[2]) {
                    sb.append(i).append(" ");
                    sb.append(red).append(" ").append(green).append(" ").append(blue);
                    sb.append("\n");
                }
            }
            query = sb.toString();

        } else {
            query = null;
        }
        return query;
    }

    /* for the moment we'll assume a white background */
    private void getBackground(ColorModel cmod, int[] pixels, int[] background) {
        background[0] = 255;
        background[1] = 255;
        background[2] = 255;
    }

}
