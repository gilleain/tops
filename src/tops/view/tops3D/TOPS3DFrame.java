package tops.view.tops3D;

import javax.swing.JFrame;

public class TOPS3DFrame extends JFrame {

    private Architect architect;

    private SimpleStructure simpleStructure;

    private Tops3DCanvas canvas;

    public TOPS3DFrame(String vertices, String hbonds, String chirals) {
        SimpleFramework framework = new SimpleFramework();
        this.architect = new Architect(framework);
        this.architect.buildStrandSheet(vertices, hbonds, chirals);
        this.simpleStructure = framework.getStructure();
        System.out.println(this.simpleStructure.toString());
        this.canvas = new Tops3DCanvas();
        this.canvas.setCenter(this.simpleStructure.getCenter());
        this.canvas.setScene(this.simpleStructure.getSceneGraph());
        this.getContentPane().add(this.canvas);
    }

    public static void main(String[] args) {
        String vertices = args[0];
        String hbonds = args[1];
        String chirals = args[2];
        TOPS3DFrame t3D = new TOPS3DFrame(vertices, hbonds, chirals);
        t3D.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        t3D.pack();
        t3D.setSize(500, 400);
        t3D.setVisible(true);
    }
}
