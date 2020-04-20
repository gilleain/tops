package tops.view.diagram;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import tops.view.model.Arc;
import tops.view.model.Box;
import tops.view.model.Bullet;
import tops.view.model.Shape;
import tops.view.model.ShapeList;
import tops.view.model.Triangle;

public class DiagramGenerator {
    
    private static final Vector2d UP = new Vector2d(0, -1);
    
    private static final Vector2d DOWN = new Vector2d(0, 1);  // XXX
    
    private static final Vector2d LEFT = new Vector2d(-1, 0);  // XXX
    
    private static final Vector2d RIGHT = new Vector2d(1, 0);  // XXX

    
    public Shape generate(String vertexString, String edgeString, String highlightString) {
        ShapeList root = new ShapeList();
        ShapeList vertices = generateVertices(vertexString);
        root.add(vertices);
        root.add(generateEdges(edgeString, vertices));
        return root;
    }


    private ShapeList generateVertices(String vertices) {
        ShapeList root = new ShapeList();
        
        
        final double triangleRadius = 1.0 / Math.sqrt(3);   // R = a / sqrt(3)
        // half the interior radius of the triangle, r = R / 2
        final double adjustment = triangleRadius / 4;
        final double upAdjustment   =  adjustment;
        final double downAdjustment = -adjustment;   
        
        final double bulletHeight = 0.75;
        final double upBulletAdjustment   = -bulletHeight / 2;
        final double downBulletAdjustment =  -bulletHeight / 2; 
        
        double yAxis = 0.5;
        Point2d position = new Point2d(yAxis, yAxis);
        int numberOfVertices = vertices.length();
        for (int index = 0; index < numberOfVertices; index++) {
            System.out.println(index + " " + position.x + " " + position.y);
            switch (vertices.charAt(index)) {
                case 'E':
                    position = new Point2d(position.x + 1, yAxis + upAdjustment);
                    root.add(new Triangle(UP, position, triangleRadius));
                    break;
                case 'e':
                    position = new Point2d(position.x + 1, yAxis + downAdjustment);
                    root.add(new Triangle(DOWN, position, triangleRadius));
                    break;
                case 'H':
                    position = new Point2d(position.x + 1, yAxis + upBulletAdjustment);
                    root.add(generateBullet(UP, position, bulletHeight, Color.BLACK));
                    break;
                case 'h':
                    position = new Point2d(position.x + 1, yAxis + downBulletAdjustment);
                    root.add(generateBullet(DOWN, position, bulletHeight, Color.BLACK));
                    break;
                default:
                    position = new Point2d(position.x + 1, yAxis);
                    root.add(new Box(position, 1, 1));
                    break;
            }
        }
        return root;
    }
    
    
    private Shape generateBullet(Vector2d orientation, Point2d center, double height, Color color) {
        double r = 0.25;
        Point2d start = new Point2d(LEFT);
        start.scaleAdd(r, center);
        Point2d end = new Point2d(RIGHT);
        end.scaleAdd(r, center);
        
        return new Arc(orientation, start, end, height, true, color);
    }
    
    private Shape generateEdges(String estr, ShapeList vertices) {
        ShapeList root = new ShapeList();
        
        int pos = 0;
        int last = 0;

        while (pos < estr.length()) { // for each char of the estr
            char ch = estr.charAt(pos);
            if (Character.isLetter(ch)) { // when you find a letter (A, P, R, L)
                String edgeStr = estr.substring(last, pos); // store the edge
                int cPos = edgeStr.indexOf(':');
                
                int l = Integer.parseInt(edgeStr.substring(0, cPos));
                int r = Integer.parseInt(edgeStr.substring(cPos + 1, edgeStr.length()));
                
                Rectangle2D leftBounds = vertices.get(l).getBounds();
                Rectangle2D rightBounds = vertices.get(r).getBounds();
                Point2d left = new Point2d(leftBounds.getCenterX(), leftBounds.getCenterY() - leftBounds.getHeight());
                Point2d right = new Point2d(rightBounds.getCenterX(), rightBounds.getCenterY() - rightBounds.getHeight());
                double height = (right.x - left.x) / 2;
                
                switch (ch) {
                    case 'A':
                        root.add(new Arc(UP, left, right, height, false, colorFor(Edge.Type.ANTIPARALLEL_HBOND)));
                        break;

                    case 'P':
                        root.add(new Arc(UP, left, right, height, false, colorFor(Edge.Type.PARALLEL_HBOND)));
                        break;

                    case 'R':
                        root.add(new Arc(DOWN, left, right, height, false, colorFor(Edge.Type.RIGHT_CHIRAL)));
                        break;

                    case 'L':
                        root.add(new Arc(DOWN, left, right, height, false, colorFor(Edge.Type.LEFT_CHIRAL)));
                        break;

                    case 'Z':
                        root.add(new Arc(UP, left, right, height, false, colorFor(Edge.Type.PARALLEL_HBOND)));
                        root.add(new Arc(DOWN, left, right, height, false, colorFor(Edge.Type.RIGHT_CHIRAL)));
                        break;

                    case 'X':
                        root.add(new Arc(UP, left, right, height, false, colorFor(Edge.Type.PARALLEL_HBOND)));
                        root.add(new Arc(DOWN, left, right, height, false, colorFor(Edge.Type.LEFT_CHIRAL)));
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
