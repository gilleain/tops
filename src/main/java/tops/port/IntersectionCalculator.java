package tops.port;

import static java.lang.Math.max;
import static java.lang.Math.min;

import javax.vecmath.Vector2d;

public class IntersectionCalculator {
    
    public class Intersection {
        public Vector2d point;
        public IntersectionType type;

        public Intersection(Vector2d point, IntersectionType type) {
            this.point = point;
            this.type = type;
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
    
    public Intersection lineCross(
            double px, double py, 
            double qx, double qy, 
            double rx, double ry,
            double sx, double sy) {
        
        double TOL = 0.01;

        Intersection intersection = simpleIntersection(px, py, qx, qy, rx, ry, sx, sy);
        if (intersection.type == IntersectionType.CROSSING) {
            return intersection;
        }

        // Special cases 
        double x = qx;
        double y = qy;

        // - for superimposing condition require that the lines do not just overlap : a single point 
        //both lines are nearly vertical
        if (Math.abs(px - qx) < TOL && Math.abs(rx - sx) < TOL) {
            x = px;
            y = parallel(py, qy, ry, sy);
            py += 2.0 * TOL * signof(py, qy);
            qy -= 2.0 * TOL * signof(py, qy);
            ry += 2.0 * TOL * signof(ry, sy);
            sy -= 2.0 * TOL * signof(ry, sy);
            if (Math.abs(px - rx) < TOL && overlap(y, py, qy, ry, sy)) {
                return new Intersection(new Vector2d(x, y), IntersectionType.SUPERIMPOSING);
            } else {
                //print "not crossing, both vertical:", p, q, r, s
                return new Intersection(new Vector2d(x, y), IntersectionType.NOT_CROSSING);
            }
        }

        //the first line is nearly vertical
        if (Math.abs(px - qx) < TOL) {
            double c1 = px;
            double m2 = slope(rx, ry, sx, sy);
            double c2 = constant(rx, ry, sx, sy);
            y = m2 * c1 + c2;
            if (Math.abs(m2) > TOL) {
                x = (y - c2) / m2;
            } else {
                x = px;
            }
            if (overlap(x, px, qx, rx, sx) && overlap(y, py, qy, ry, sy)) {
                return new Intersection(new Vector2d(x, y), IntersectionType.CROSSING);
            } else {
                //print "not crossing, first vertical:", p, q, r, s
                return new Intersection(new Vector2d(x, y), IntersectionType.NOT_CROSSING);
            }
        }

        //the second line is nearly vertical
        if (Math.abs(rx - sx) < TOL) {
            double m1 = slope(px, py, qx, qy);
            double c1 = constant(px, py, qx, qy);
            double c2 = rx;
            y = m1 * c2 + c1;
            if (Math.abs(m1) > TOL) {
                x = (y - c1) / m1;
            } else {
                x = rx;
                if (overlap(x, px, qx, rx, sx) && overlap(y, py, qy, ry, sy)) {
                    return new Intersection(new Vector2d(x, y), IntersectionType.CROSSING);
                } else {
                    //print "not crossing, second vertical:", p, q, r, s
                    return new Intersection(new Vector2d(x, y), IntersectionType.NOT_CROSSING);
                }   
            }

            // Calculate slopes
            m1 = slope(px, py, qx, qy);
            c1 = constant(px, py, qx, qy);
            double m2 = slope(rx, ry, sx, sy);
            c2 = constant(rx, ry, sx, sy);

            // Parallel case - for superimposing condition require that the lines do not just overlap : a single point */
            if (Math.abs(m1 - m2) < TOL) {
                x = parallel(px, qx, rx, sx);
                y = x * m1 + c1;
                px += 2.0 * TOL * signof(px, qx);
                qx -= 2.0 * TOL * signof(px, qx);
                rx += 2.0 * TOL * signof(rx, sx);
                sx -= 2.0 * TOL * signof(rx, sx);
                if (Math.abs(c1 - c2) < TOL && overlap(x, px, qx, rx, sx)) { 
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
            if (overlap(x, px, qx, rx, sx) && overlap(y, py, qy, ry, sy)) {
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
        double TOL = 0.01;
        double denom = (bx - ax) * (dy - cy) - (by - ay) * (dx - cx);
        //print "denom = ", denom
        if (denom > TOL) {
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
