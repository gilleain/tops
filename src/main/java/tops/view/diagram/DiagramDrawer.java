package tops.view.diagram;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

public class DiagramDrawer {

    private int w;
    private int h;
    private double axis;
    private Graph gr;
    private Color backgroundColor = Color.white;
    private boolean showLabelNumbers;
    private DiagramConverter builder;

    public DiagramDrawer(int w, int h) {
        this.w = w;
        this.h = h;
        this.axis = (new Integer((2 * h) / 3)).doubleValue();
        this.gr = new Graph();
        this.showLabelNumbers = false;
    }

    public DiagramDrawer(String vertices, String edges, String highlights, int w, int h) {
        this(w, h);
        builder = new DiagramConverter();
        this.gr = builder.toDiagram(vertices, edges, highlights);
    }
    
    public void setGraph(Graph gr) {
        this.gr = gr;
    }
    
    public void setData(String vertices, String edges, String highlights) {
        this.gr = builder.toDiagram(vertices, edges, highlights);
    }
    
    public void setShowLabelNumbers(boolean val) {
        this.showLabelNumbers = val;
    }

    public void paint(Graphics g) {
        this.paint((Graphics2D) g);
    }

    public void paint(Graphics2D g2) {
        if (this.gr == null || this.gr.isEmpty()) {
            // System.out.println("graph empty");
        } else {
            if (this.gr.needsLayout()) {
                this.gr.layout(this.axis, this.w);
            }
            g2.setColor(this.backgroundColor);
            g2.fillRect(0, 0, this.w, this.h);
            
            int numberOfVertices = this.gr.numberOfVertices();
            
            // XXX : various attempts to parameterize the label positions
            //int labelY = (5 * this.h) / 6;
            //int labelY = (int) this.axis + ((3 * this.w) / (numberOfVertices * 2));
            int labelY = (int) this.axis + (this.w / numberOfVertices);
            
            Font f = g2.getFont();
            FontRenderContext frc = g2.getFontRenderContext();
            
            for (int i = 0; i < numberOfVertices; i++) {
                Vertex sse = this.gr.getVertex(i);
                g2.setColor(sse.getColor());
                g2.draw(sse.getShape());
                
                if (this.showLabelNumbers) {
                    // don't number the terminii
                    if (i != 0 && i < numberOfVertices - 1) {
                        String l = String.valueOf(i);
                        Rectangle2D stringBounds = f.getStringBounds(l, frc); 
                        double sWidth = stringBounds.getWidth();
                        int labelX = sse.getCenterX() - (int)(sWidth / 2);
                        g2.drawString(String.valueOf(i), labelX, labelY);
                    }
                    
                }
            }
            
            for (int j = 0; j < this.gr.numberOfEdges(); j++) {
                Edge edge = this.gr.getEdge(j);
                g2.setColor(edge.getColor());
                g2.draw(edge.getShape(this.axis));
            }
        }
    }

    public String toPostscript() {
        if (this.gr == null || this.gr.isEmpty()) {
            return "";
        } else {
            if (this.gr.needsLayout()) {
                this.gr.layout(this.axis, this.w);
            }
            return this.gr.toPostscript(this.w, this.h, this.axis);
        }
    }
}
