package tops.cli.drawing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.drawing.layers.GeometricParameters;
import tops.drawing.layers.LayerDiagram;

public class LayerDiagramCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String directions = args[0];
        
        GeometricParameters params;
        if (args.length == 1) {
            params = new GeometricParameters();
        } else {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            params = new GeometricParameters(newArgs);
        }
        
        LayerDiagram layerDiagram = new LayerDiagram();
        layerDiagram.setParams(params);
        layerDiagram.createFromTopsString(directions);
        
        JPanel picture = new JPanel();
        
        Dimension d = new Dimension(500, 500);
        picture.setPreferredSize(d);
        
        JFrame t2df = new JFrame(directions);
        t2df.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        t2df.getContentPane().add(picture, BorderLayout.CENTER);
        t2df.setSize(d);
        t2df.setLocation(800, 30);
        t2df.pack();
        t2df.setVisible(true);
        
        Graphics2D g2 = (Graphics2D) picture.getGraphics();
        layerDiagram.draw(g2);        
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
