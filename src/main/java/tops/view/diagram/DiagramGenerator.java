package tops.view.diagram;

import java.awt.Color;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import tops.view.model.Arc;
import tops.view.model.Box;
import tops.view.model.Bullet;
import tops.view.model.Shape;
import tops.view.model.ShapeList;
import tops.view.model.Triangle;

public class DiagramGenerator {
    
    private static final Vector2d UP = new Vector2d(0, 0);
    
    private static final Vector2d DOWN = new Vector2d(180, 0);  // XXX
    
    public Shape generate(String vertexString, String edgeString, String highlightString) {
        ShapeList root = new ShapeList();
        ShapeList vertices = generateVertices(vertexString);
        root.add(vertices);
        root.add(generateEdges(edgeString, vertices));
        return root;
    }


    private ShapeList generateVertices(String vertices) {
        ShapeList root = new ShapeList();
        
        double yAxis = 0.5;
        Point2d position = new Point2d(yAxis, yAxis);
        int numberOfVertices = vertices.length();
        for (int index = 0; index < numberOfVertices; index++) {
            switch (vertices.charAt(index)) {
                case 'E':
                    root.add(new Triangle(UP, position, 1.0));
                    break;
                case 'e':
                    root.add(new Triangle(DOWN, position, 1.0));
                    break;
                case 'H':
                    root.add(new Bullet(UP, position));
                    break;
                case 'h':
                    root.add(new Bullet(DOWN, position));
                    break;
                default:
                    root.add(new Box(position, 1, 1));
                    break;
            }
            position = new Point2d(position.x + 1, yAxis);
        }
        return root;
    }
    
    private Shape generateEdges(String estr, ShapeList vertices) {
        ShapeList root = new ShapeList();
        
        int pos = 0;
        int last = 0;

        while (pos < estr.length()) { // for each char of the estr
            char ch = estr.charAt(pos);
            if (Character.isLetter(ch)) { // when you find a letter (A, P, R, L)
                String edgeStr = estr.substring(last, pos); // store the edge
                int Cpos = edgeStr.indexOf(':');
                
                int l = Integer.parseInt(edgeStr.substring(0, Cpos));
                int r = Integer.parseInt(edgeStr.substring(Cpos + 1, edgeStr.length()));
                
                Point2d left = vertices.get(l).getCenter();
                Point2d right = vertices.get(r).getCenter();
                double height = (right.x - left.x) / 2;
                
                switch (ch) {
                    case 'A':
                        root.add(new Arc(UP, left, right, height, colorFor(Edge.Type.ANTIPARALLEL_HBOND)));
                        break;

                    case 'P':
                        root.add(new Arc(UP, left, right, height, colorFor(Edge.Type.PARALLEL_HBOND)));
                        break;

                    case 'R':
                        root.add(new Arc(DOWN, left, right, height, colorFor(Edge.Type.RIGHT_CHIRAL)));
                        break;

                    case 'L':
                        root.add(new Arc(DOWN, left, right, height, colorFor(Edge.Type.LEFT_CHIRAL)));
                        break;

                    case 'Z':
                        root.add(new Arc(UP, left, right, height, colorFor(Edge.Type.PARALLEL_HBOND)));
                        root.add(new Arc(DOWN, left, right, height, colorFor(Edge.Type.RIGHT_CHIRAL)));
                        break;

                    case 'X':
                        root.add(new Arc(UP, left, right, height, colorFor(Edge.Type.PARALLEL_HBOND)));
                        root.add(new Arc(DOWN, left, right, height, colorFor(Edge.Type.LEFT_CHIRAL)));
                        break;
                        
                    default:
                        // TODO - log error or raise exception
                        break;
                }
                last = pos + 1;
            }
            ++pos;
        }
        return root;
    }
    
    private Color colorFor(Edge.Type type) {
        switch (type) {
            case ANTIPARALLEL_HBOND: return Color.GREEN;
            case PARALLEL_HBOND: return Color.RED;
            case LEFT_CHIRAL: return Color.GRAY;    // TODO - can't remember ...
            case RIGHT_CHIRAL: return Color.BLUE;
            default: return Color.BLACK;
        }
    }

}
