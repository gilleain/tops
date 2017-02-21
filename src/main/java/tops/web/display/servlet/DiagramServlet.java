package tops.web.display.servlet;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tops.view.diagram.DiagramDrawer;

public class DiagramServlet extends HttpServlet {

	private static final long serialVersionUID = 2431033580433443788L;

	private int DEFAULT_WIDTH = 300;

    private int DEFAULT_HEIGHT = 200;

    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo(); // like "/200/300/NEEC/1:2P//2bopA0.gif
        path = path.substring(1); // chomp first char
        String[] bits = path.split("/");

        String width = bits[0];
        String height = bits[1];

        String matches = bits[2];

        String body = bits[3];
        String tail = bits[4];

        if (tail.equals("none")) {
            tail = new String();
        }
        if (matches.equals("none")) {
            matches = new String();
        }

        int w;
        if (width.equals("default")) {
            w = this.DEFAULT_WIDTH;
        } else {
            w = Integer.parseInt(width);
        }

        int h;
        if (height.equals("default")) {
            h = this.DEFAULT_HEIGHT;
        } else {
            h = Integer.parseInt(height);
        }

        response.setContentType("image/gif");
        OutputStream out = response.getOutputStream();

        int type = BufferedImage.TYPE_INT_RGB; // which type?
        BufferedImage image = new BufferedImage(w, h, type);
        Graphics2D g2 = image.createGraphics();

        DiagramDrawer dd = new DiagramDrawer(body, tail, matches, w, h);
        dd.paint(g2);

       ImageIO.write(image, "GIF", out);
    }

}
