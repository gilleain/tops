package tops.dw;

import java.applet.Applet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;

import tops.dw.editor.TopsEditor;
import tops.dw.protein.Protein;

public class LaunchButton extends Applet implements MouseListener {

    private TopsEditor topsEd;

    private Image backgroundImage;

    private String proteinID; // applet parameter, the full domain

    private String classf; // applet parameter, the code for the classification
                            // (cath, scop etc)

    private String host;

    private int port;

//    private static final String cgi_query_prog = "find"; // the findfiles servlet

    private String backgroundImageUrl = "java11.gif";

//    private String cgi_prog_path;

    @Override
    public void init() {
        this.host = "www.tops.leeds.ac.uk";
        this.port = 8080;

        this.proteinID = this.getParameter("proteinID");
        this.classf = this.getParameter("classf");
        this.backgroundImageUrl = this.getParameter("img");

        // System.out.println("I am getting an image from : " + getCodeBase() +
        // backgroundImageUrl);
        this.backgroundImage = this.getImage(this.getCodeBase(), this.backgroundImageUrl);
        this.resize(this.backgroundImage.getWidth(this), this.backgroundImage
                .getHeight(this)); // make applet the same size as the image
        this.setBackground(Color.white);
        this.addMouseListener(this);
    }

    @Override
    public void stop() {
        if (this.topsEd != null)
            this.topsEd.quit();
    }

    @Override
    public void destroy() {
        if (this.topsEd != null)
            this.topsEd.quit();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(this.backgroundImage, 0, 0, this);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        Protein p = this.QueryTOPSDatabase(this.classf, this.proteinID);
        if (p != null) {
            this.topsEd = new TopsEditor(this, null);
            this.topsEd.addProtein(p);
        }
    }

    /*
     * This private method queries the TOPS database given a diagram id number
     * it returns a Protein object or null on error
     * 
     * Method used is not elegant - java calling a CGI script on the server
     * Might replace with a more elegant solution ( JDBC, or java/CORBA ) at a
     * later date.
     */

    private Protein QueryTOPSDatabase(String classf, String protein) {

        Protein p = null;

        String query = "path=" + classf + "&filename=" + protein;

        String file = "/tops/get" + "?" + query;
        java.net.URL servlet = null;
        try {
            servlet = new URL("http", this.host, this.port, file);
        } catch (java.net.MalformedURLException MURLE) {
            System.out.println("murle : " + MURLE);
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    servlet.openStream()));
            p = new Protein(br);
        } catch (IOException ioe) {
            System.out.println("IOE : " + ioe);
        }

        return p;

    }

}
