package tops.dw.app;

import java.net.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class AppletImagePrinter {

    /* START instance variables */

    private Image image = null;

    private String host = null;

    private int port = 80;

    private String cgi_prog = null;

    private String ErrorString = "No error";

    /* END instance variables */

    public AppletImagePrinter(Image img, String Host, int Port,
            String CGI_path, String CGI_print_prog) {
        this.image = img;
        this.host = Host;
        this.port = Port;
        this.cgi_prog = CGI_path + CGI_print_prog;
    }

    public String getErrorString() {
        return this.ErrorString;
    }

    public URL doPrint() {

        URL target = null;

        if (this.host == null) {
            this.ErrorString = "AppletImagePrinter: No Host";
            return null;
        }

        if (this.cgi_prog == null) {
            this.ErrorString = "AppletImagePrinter: No CGI program";
            return null;
        }

        if (this.image == null) {
            this.ErrorString = "AppletImagePrinter: No Image";
            return null;
        }

        String query = this.formQuery(this.image);

        if (query != null) {

            CGIrequest cgir = new CGIrequest(this.host, this.port, "POST", this.cgi_prog,
                    query);
            InputStream cgi_response = cgir.doRequest();

            if (cgi_response == null) {
                System.out.println(cgir.getErrorString());
                this.ErrorString = "AppletImagePrinter: CGI error";
                target = null;
            } else {
                try {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(cgi_response));
                    String line;

                    /* expect URL_FOLLOWS in response */
                    line = br.readLine();
                    while ((line != null) && (!line.equals("URL_FOLLOWS"))) {
                        line = br.readLine();
                    }

                    if (line.equals("URL_FOLLOWS")) {
                        line = br.readLine();
                        if (line != null) {
                            try {
                                target = new URL(line);
                            } catch (MalformedURLException urle) {
                                target = null;
                                this.ErrorString = "AppletImagePrinter: URL Error";
                            }
                        } else {
                            target = null;
                            this.ErrorString = "AppletImagePrinter: Error unexpected CGI response";
                        }

                    } else {
                        target = null;
                        this.ErrorString = "AppletImagePrinter: Error unexpected CGI response";
                    }
                } catch (IOException ioe) {
                    this.ErrorString = "AppletImagePrinter: Error reading CGI response";
                    target = null;
                }
            }

            try {
                cgir.Close();
            } catch (IOException e) {
            }

        } else {
            this.ErrorString = "AppletImagePrinter: Error forming query";
            target = null;
        }

        return target;

    }

    private String formQuery(Image img) {

        String query = null;

        if (img == null)
            return null;

        int w = 500;
        int h = 400;

        int pixels[] = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        boolean success = false;
        try {
            success = pg.grabPixels();
        } catch (InterruptedException e) {
            return null;
        }

        if (success) {
            ColorModel cm = pg.getColorModel();

            StringBuffer sb = new StringBuffer();
            sb.append("P3 " + w + " " + h + " 255\n");

            int i;
            int red, green, blue;
            int background[] = new int[3];
            this.getBackground(cm, pixels, background);

            for (i = 0; i < w * h; i++) {

                red = cm.getRed(pixels[i]);
                green = cm.getGreen(pixels[i]);
                blue = cm.getBlue(pixels[i]);

                if ((red != background[0]) || (green != background[1])
                        || (blue != background[2])) {
                    sb.append(i);
                    sb.append(" ");
                    sb.append(red);
                    sb.append(" ");
                    sb.append(green);
                    sb.append(" ");
                    sb.append(blue);
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
    private void getBackground(ColorModel cmod, int pixels[], int background[]) {

        background[0] = 255;
        background[1] = 255;
        background[2] = 255;

    }

}
