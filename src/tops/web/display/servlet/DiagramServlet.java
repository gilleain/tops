package tops.web.display.servlet;

import java.awt.Graphics2D;

import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Acme.JPM.Encoders.GifEncoder;
import com.eteks.java2d.PJABufferedImage;

import tops.view.tops2D.diagram.DiagramDrawer;

public class DiagramServlet extends HttpServlet {

    private int DEFAULT_WIDTH = 300;

    private int DEFAULT_HEIGHT = 200;

    private int w, h;

//    private String head;
    private String body;
    private String tail;
    private String matches;

    static {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("awt.toolkit", "com.eteks.awt.PJAToolkit");

        try {
            Class c = DiagramDrawer.class;
            com.eteks.awt.PJAGraphicsManager.getDefaultGraphicsManager()
                    .loadFont(
                            c.getClassLoader().getResourceAsStream("pja.pjaf"));
        } catch (Exception ie) {
            System.err.println("Can not load font " + ie);
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo(); // like
                                                // "/200/300/NEEC/1:2P//2bopA0.gif
        // String topsString = request.getParameter("data");
        path = path.substring(1); // chomp first char
        String[] bits = path.split("/");

        String width = bits[0];
        // String width = request.getParameter("width");
        String height = bits[1];
        // String height = request.getParameter("height");

        // matches = request.getParameter("match");
        this.matches = bits[2];

        this.body = bits[3];
        this.tail = bits[4];
//        head = bits[5];

        if (this.tail.equals("none"))
            this.tail = new String();
        if (this.matches.equals("none"))
            this.matches = new String();

        if (width.equals("default"))
            this.w = this.DEFAULT_WIDTH;
        else
            this.w = Integer.parseInt(width);

        if (height.equals("default"))
            this.h = this.DEFAULT_HEIGHT;
        else
            this.h = Integer.parseInt(height);

        response.setContentType("image/gif");
        OutputStream out = response.getOutputStream();

        int type = BufferedImage.TYPE_INT_RGB; // which type?
        BufferedImage image = new PJABufferedImage(this.w, this.h, type);
        Graphics2D g2 = image.createGraphics();

        DiagramDrawer dd = new DiagramDrawer(this.body, this.tail, this.matches, this.w, this.h);
        dd.paint(g2);

        GifEncoder gif = new GifEncoder(image, out);
        gif.encode();
    }

}
