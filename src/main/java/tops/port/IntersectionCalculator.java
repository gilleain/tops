package tops.port;

import static java.lang.Math.max;
import static java.lang.Math.min;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class IntersectionCalculator {
    
    public class Intersection {
        private final Vector2d point;
        private final IntersectionType type;

        public Intersection(Vector2d point, IntersectionType type) {
            this.point = point;
            this.type = type;
        }
        
        public Vector2d getPoint() {
            return this.point;
        }
        
        public IntersectionType getType() {
            return this.type;
        }
        
        public String toString() {
            return String.format("%s at %s", type, point);
        }
    }
    
    public enum IntersectionType {
        NOT_CROSSING,
        CROSSING,
        SUPERIMPOSING;
    }
    
    public Intersection lineCross(Point2d p, Point2d q, Point2d r, Point2d s) {
        double tolerance = 0.01;

        Intersection intersection = simpleIntersection(p.x, p.y, q.x, q.y, r.x, r.y, s.x, s.y);
        if (intersection.type == IntersectionType.CROSSING) {
            return intersection;
        }

        // Special cases 
        double x = q.x;
        double y = q.y;

        // - for superimposing condition require that the lines do not just overlap : a single point 
        //both lines are nearly vertical
        if (Math.abs(p.x - q.x) < tolerance && Math.abs(r.x - s.x) < tolerance) {
            x = p.x;
            y = parallel(p.y, q.y, r.y, s.y);
            p.y += 2.0 * tolerance * signof(p.y, q.y);
            q.y -= 2.0 * tolerance * signof(p.y, q.y);
            r.y += 2.0 * tolerance * signof(r.y, s.y);
            s.y -= 2.0 * tolerance * signof(r.y, s.y);
            if (Math.abs(p.x - r.x) < tolerance && overlap(y, p.y, q.y, r.y, s.y)) {
                return new Intersection(new Vector2d(x, y), IntersectionType.SUPERIMPOSING);
            } else {
                //print "not crossing, both vertical:", p, q, r, s
                return new Intersection(new Vector2d(x, y), IntersectionType.NOT_CROSSING);
            }
        }

        //the first line is nearly vertical
        if (Math.abs(p.x - q.x) < tolerance) {
            double c1 = p.x;
            double m2 = slope(r.x, r.y, s.x, s.y);
            double c2 = constant(r.x, r.y, s.x, s.y);
            y = m2 * c1 + c2;
            if (Math.abs(m2) > tolerance) {
                x = (y - c2) / m2;
            } else {
                x = p.x;
            }
            if (overlap(x, p.x, q.x, r.x, s.x) && overlap(y, p.y, q.y, r.y, s.y)) {
                return new Intersection(new Vector2d(x, y), IntersectionType.CROSSING);
            } else {
                //print "not crossing, first vertical:", p, q, r, s
                return new Intersection(new Vector2d(x, y), IntersectionType.NOT_CROSSING);
            }
        }

        //the second line is nearly vertical
        if (Math.abs(r.x - s.x) < tolerance) {
            double m1 = slope(p.x, p.y, q.x, q.y);
            double c1 = constant(p.x, p.y, q.x, q.y);
            double c2 = r.x;
            y = m1 * c2 + c1;
            if (Math.abs(m1) > tolerance) {
                x = (y - c1) / m1;
            } else {
                x = r.x;
                if (overlap(x, p.x, q.x, r.x, s.x) && overlap(y, p.y, q.y, r.y, s.y)) {
                    return new Intersection(new Vector2d(x, y), IntersectionType.CROSSING);
                } else {
                    //print "not crossing, second vertical:", p, q, r, s
                    return new Intersection(new Vector2d(x, y), IntersectionType.NOT_CROSSING);
                }   
            }

            // Calculate slopes
            m1 = slope(p.x, p.y, q.x, q.y);
            c1 = constant(p.x, p.y, q.x, q.y);
            double m2 = slope(r.x, r.y, s.x, s.y);
            c2 = constant(r.x, r.y, s.x, s.y);

            // Parallel case - for superimposing condition require that the lines do not just overlap : a single point */
            if (Math.abs(m1 - m2) < tolerance) {
                x = parallel(p.x, q.x, r.x, s.x);
                y = x * m1 + c1;
                p.x += 2.0 * tolerance * signof(p.x, q.x);
                q.x -= 2.0 * tolerance * signof(p.x, q.x);
                r.x += 2.0 * tolerance * signof(r.x, s.x);
                s.x -= 2.0 * tolerance * signof(r.x, s.x);
                if (Math.abs(c1 - c2) < tolerance && overlap(x, p.x, q.x, r.x, s.x)) { 
                    return new Intersection(new Vector2d(x, y), IntersectionType.SUPERIMPOSING);
                } else {
                    //print "not crossing, parallel :", p, q, r, s
                    return new Intersection(new Vector2d(x, y), IntersectionType.NOT_CROSSING);
                }
            }

            // Find crossing point
            x = slope(-m1, c1, -m2, c2);
            y = constant(-m1, c1, -m2, c2) ;

            // Does crossing point lie inside either line (only need to test one)
            if (overlap(x, p.x, q.x, r.x, s.x) && overlap(y, p.y, q.y, r.y, s.y)) {
                return new Intersection(new Vector2d(x, y), IntersectionType.CROSSING);
            } else {
                //print "not crossing other:", p, q, r, s
                //print "crossing point", x, y, "for", px, py, qx, qy, rx, ry, sx, sy
                return new Intersection(new Vector2d(x, y), IntersectionType.NOT_CROSSING);
            }
        }
        return null;
    }
    
    public Intersection simpleIntersection(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        double tolerance = 0.01;
        double denom = (bx - ax) * (dy - cy) - (by - ay) * (dx - cx);
        //print "denom = ", denom
        if (denom > tolerance) {
            double r = (ay - cy) * (dx - cx) - (ax - cx) * (dy - cy) / denom;
            double s = (ay - cy) * (bx - ax) - (ax - cx) * (by - ay) / denom;
            if (0 <= r && r <= 1 && 0 <= s && s <= 1) {
                //print "0<=r<=1,0<=s<=1!", r, s
                return new Intersection(new Vector2d(ax + r * (bx - ax), ay + r * (by - ay)), IntersectionType.CROSSING);
            } else {
                return new Intersection(new Vector2d(0, 0), IntersectionType.NOT_CROSSING);
            }
        } else {
            return new Intersection(new Vector2d(0, 0), IntersectionType.SUPERIMPOSING);
        }
    }
    

    private double slope(double x1, double y1, double x2, double y2) {
        return (y1 - y2) / (x1 - x2);
    }

    private boolean overlap(double p, double x1, double x2, double x3, double x4) {
        double tol = 0.01;
        return (min(x1, x2) - p < tol) 
                && (p - max(x1, x2) < tol) 
                && (min(x3, x4) - p < tol) 
                && (p - max(x3, x4) < tol);
    }

    private double parallel(double x1, double x2, double x3, double x4) {
        return (min(max(x1, x2), max(x3, x4)) + max(min(x1, x2), min(x3, x4))) / 2.0;
    }

    private double signof(double a, double  b) {
        if (b > a) return 1.0;
        else    return -1.0;
    }

    // XXX was called 'Const' but this is a java keyword!
    private double constant(double x1, double y1, double x2, double y2) {
        return (x2 * y1) - (x1 * y2) / (x2 - x1);
    }



}
