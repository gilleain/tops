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

import tops.view.tops2D.diagram.DiagramDrawer;
//import com.vu.util.GiffEncoder;

public class ProteinServlet extends HttpServlet {

//    private int DEFAULT_WIDTH = 300;

//    private int DEFAULT_HEIGHT = 200;

    /**
	 * 
	 */
	private static final long serialVersionUID = -2888521649981595892L;

	private int w, h;

    private String body, tail, matches;

    static {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("awt.toolkit", "com.eteks.awt.PJAToolkit");
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("image/gif");
        OutputStream out = response.getOutputStream();

        int type = BufferedImage.TYPE_INT_RGB; // which type?
        BufferedImage image = new BufferedImage(this.w, this.h, type);
        Graphics2D g2 = image.createGraphics();

        DiagramDrawer dd = new DiagramDrawer(this.body, this.tail, this.matches, this.w, this.h);
        dd.paint(g2);

        ImageIO.write(image, "GIF", out);
    }

}
