package tops.drawing.layers;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import tops.drawing.model.Connection;
import tops.drawing.model.Helix;
import tops.drawing.model.SSE;
import tops.drawing.model.Strand;
import tops.drawing.model.TSE;
import tops.drawing.model.Topology;



/**
 * @author maclean
 *
 */
public class LayerDiagram {
    
    private ArrayList<ConnectionLine> connectionLines;
    private ArrayList<Layer> layers;
//    private TerminalBox nTerminus;
//    private TerminalBox cTerminus;
    
    private GeometricParameters p;
    
    public LayerDiagram() {
        this.layers = new ArrayList<Layer>();
        this.connectionLines = new ArrayList<ConnectionLine>();
        this.p = new GeometricParameters();
    }
    
    public void setParams(GeometricParameters p) {
        this.p = p;
    }
    
    public void createFromTopsString(String topsString) {
        Topology model = new Topology(topsString);
        this.createFromModel(model);
    }
    
    public void createFromModel(Topology model) {
       
        // convert the sheets into layers and set the z-order
        ArrayList<TSE> tses = model.getTSES();
        HashMap<Integer, LayerElement> sseToElementMap = new HashMap<Integer, LayerElement>();
        int zOrder = 0;
        for (int i = 0; i < tses.size(); i++) {
            Layer layer = new Layer(zOrder);
            TSE tse = (TSE) tses.get(i);
            ArrayList<SSE> elements = tse.getElements();
            for (int j = 0; j < elements.size(); j++) {
                SSE sse = (SSE) elements.get(j);
                LayerElement element;
                if (sse instanceof Strand) {
                    element = new Arrow(sse.isUp());
                } else if (sse instanceof Helix){
                    element = new HelixBox(sse.isUp());
                    System.out.println("creating box");
                } else {
                    continue;
                }
                layer.addLayerElement(element);
                sseToElementMap.put(sse.getSSENumber(), element);
                
            }
            this.layers.add(layer);
            zOrder++;
        }

        
        // make the connection lines
        ArrayList<Connection> connections = model.getConnections();
        for (int i = 0; i < connections.size(); i++) {
            Connection connection = (Connection) connections.get(i);
            SSE first = connection.getFirst();
            SSE second = connection.getSecond();
            LayerElement firstElement = (LayerElement) sseToElementMap.get(first.getSSENumber());
            LayerElement secondElement = (LayerElement) sseToElementMap.get(second.getSSENumber());
            this.connectionLines.add(new ConnectionLine(firstElement, secondElement));
        }
        
        // layout the elements in each layer
        // XXX we can't do this in the previous loop, as layers may
        // XXX consist of more than one sheet (or other LayerElement). 
        int leftCenter = this.p.leftMostCenter;
        int centerAxis = this.p.centralAxis;
        for (int l = 0; l < this.layers.size(); l++) {
            Layer layer = (Layer) this.layers.get(l);
            layer.layout(leftCenter, centerAxis, this.p);
            leftCenter += p.separation / 2;
            centerAxis += p.sheetSeparation;
        }
        
        for (int i = 0; i < this.connectionLines.size(); i++) {
            ((ConnectionLine) this.connectionLines.get(i)).layout(0, p);
        }
       
    }
    
    public void draw(Graphics2D g) {
        g.setStroke(new BasicStroke(2.0f));
        
//        this.nTerminus.draw(g);   XXX TODO
        
        for (int i = 0; i < this.layers.size(); i++) {
            Layer layer = (Layer) this.layers.get(i);
            layer.draw(g);
        }
        
        for (int j = 0; j < connectionLines.size(); j++) {
            ConnectionLine c = (ConnectionLine) connectionLines.get(j);
            c.draw(g);
        }
        
//        this.cTerminus.draw(g);   XXX TODO
    }
    
    public static void main(String[] args) {
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

}
