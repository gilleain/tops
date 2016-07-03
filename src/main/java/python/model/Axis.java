package python.model;

import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

public class Axis {
    
    private char SSEType;
    private Vector2d start;
    private Vector2d finish;
    private double length;
    public Vector3d AxisStartPoint;
    public Vector3d AxisFinishPoint;

    public Axis(List<Point3d> coords) { 
//        int n = coords.size();
//        boolean isE = (this.SSEType == 'E');
//        if ((n < 3 && isE) || (n < 5 && !isE)) {
//            //print "len, start, end = ", len(chain.CACoords), this.SeqStartResidue, this.SeqFinishResidue
//            this.start  = x[0];
//            this.finish= x[-1];
//            this.length = 0.0;
//        }
//
//        // Calculate centroid of secondary structure, and eigenvalues
//        centroid = new Vector2d(average(coords));
//        coords = array([array(xi - centroid) for xi in coords]);
//        B = matrixmultiply(transpose(coords), coords);
//        eigenvalues, eigenvectors = la.eigenvectors(B);
//
//        // Store unit vector of axis 
//        axis = new Vector2d(eigenvectors[eigenvalues.argmax()])l
        
//        start = new Vector2d(coords[0]);
//        finish = new Vector2d(coords[n - 1]);
//
//        // Calculate beginning and end 
//        this.AxisStartPoint = centroid + ((axis * start) * axis);
//        this.AxisFinishPoint = centroid + ((axis * finish) * axis);
//
//        // return start, finish, and length of axis 
//        this.AxisLength = chain.distance3D(start, finish);
    }
    
    public double getLength() {
        return this.length;
    }
    
    private Vector3d diff(Vector3d a, Vector3d b) {
        Vector3d c = new Vector3d(a);
        c.sub(b);
        return c;
    }
    
    private Vector3d mult(Vector3d a, Vector3d b) {
        Vector3d c = new Vector3d(a);
        c.angle(b);
        return c;
    }
    
    private Vector3d mult(Vector3d a, double d) {
        Vector3d x = new Vector3d(a);
        x.scale(d);
        return x;
    }
    
    private Vector3d plus(Vector3d a, Vector3d b) {
        Vector3d c = new Vector3d(a);
        c.add(b);
        return c;
    }

    /*
    function closest_approach

    Tom F. July 1992

    Function to calculate the closest approach between two vectors. This
    is a direct translation of my Fortran routine from Jan 1990. (CLSAPH)
    Function requires:
        float   *bk, *ek    Beginning and end of axis j
        float   *bj, *ej    Beginning and end of axis k
        float   *pck, *pcj  Points of closest approach
        float   *sk, *sj    Scaling values - assigns where pc are
    Returns -999.9 on failure else returns torsion angle
    */
    public TorsionResult ClosestApproach(Axis other) {
        Vector3d bj = this.AxisStartPoint;// XXX which way round?
        Vector3d ek = this.AxisFinishPoint;// XXX which way round?
        Vector3d bk = other.AxisStartPoint;// XXX which way round?
        Vector3d ej = other.AxisFinishPoint;  // XXX which way round?
        if (bj == null || bk == null || ej == null || ek == null) return new TorsionResult();

        // Calculate constants
        Vector3d bjbk = diff(bj, bk);
        Vector3d ekbk = diff(ek, bk);
        Vector3d ejbj = diff(ej, bj);

        // Calculate coefficients of scale factors
        double w1  = bjbk.dot(ekbk);
        double w2  = bjbk.dot(ejbj);
        double u11 = ekbk.dot(ekbk);
        double u22 = ejbj.dot(ejbj);
        double u12 = ejbj.dot(ekbk);
        double det = u11 * u22 - u12 * u12;

        // Check if parallel 
        if (det == 0.0) return new TorsionResult();
        else {

            // Calculate scaling factors 
            double sk = (w1 * u22 - w2 * u12) / det;
            double sj = (w1 * u12 - w2 * u11) / det;

            // Calculate points of intersection
            Vector3d pck  = plus(bk, (mult(ekbk, sk)));
            Vector3d pcj  = plus(bj, (mult(ejbj, sj)));
            ekbk = diff(plus(pck, bk), ek);
            ejbj = diff(plus(pcj, bj), ej);
            
            // Return the torsion angle and other values
            return new TorsionResult( pck, pcj, sk, sj, this.Torsion(ekbk, pck, pcj, ejbj));
        }
    }

    public static double Torsion(Vector3d a, Vector3d b, Vector3d c, Vector3d d) {
        double conv = 0.01745329;

        // Calculate vectors and lengths a-b, b-c, c-d 
//        Vector3d a_b = diff(b, a);
//        Vector3d b_c = diff(c, b);
//        Vector3d c_d = diff(d, c);

//        double len_a_b = a_b.length();
//        double len_b_c = b_c.length();
//        double len_c_d = c_d.length();

        // Error check, are any vectors of zero length ?
//        if (len_a_b == 0.0 || len_b_c == 0.0 || len_c_d == 0.0) return -999.0;

        // Calculate dot products to form cosines 
//        Vector3d ab_bc = mult(mult(a_b, b_c), 1 / (len_a_b * len_b_c));
//        Vector3d ab_cd = mult(mult(a_b, c_d), 1 / (len_a_b * len_c_d));
//        Vector3d bc_cd = mult(mult(b_c, c_d), 1 / (len_b_c * len_c_d));

//        // Form sines 
//        double s_ab = Math.sqrt(1.0 - mult(ab_bc, ab_bc));
//        double s_bc = Math.sqrt(1.0 - mult(bc_cd, bc_cd));
//        double s = s_ab * s_bc;
//        if (s == 0.0) return 0.0;
//
//        double costor = (mult(ab_bc , bc_cd) - ab_cd ) / ((double) s );
//        double costsq = costor * costor;
//        double tor;
//        if (costsq >= 1.0 && costor < 0.0) tor = 180.0;
//
//        // If the angle is not == 180 degs calculate sign using sine 
//        if (costsq < 1.0) {
//            double sintor = Math.sqrt(1.0 - costsq);
//            tor = Math.atan2(sintor, costor);
//            tor = tor / conv;
//
//            // Find unit vectors 
//            a_b /= len_a_b;
//            b_c /= len_b_c;
//            c_d /= len_c_d;
//
//            // Find determinant 
//            double sign  = a_b[0] * (b_c[1] * c_d[2] - b_c[2] * c_d[1]);
//            sign += a_b[1] * (b_c[2] * c_d[0] - b_c[0] * c_d[2]);
//            sign += a_b[2] * (b_c[0] * c_d[1] - b_c[1] * c_d[0]);
//
//            // Change sign if necessary 
//            if (sign < 0.0) tor = -1.0 * tor;
//        }

        // Return torsion 
//        return tor;
        return 0.0;
     }

    public String toString() {
        if (this.AxisStartPoint == null) return new String[] {"AxisStartPoint", "AxisEndPoint"}.toString();
        String s = String.format("%s %0.2f %0.2f %0.2f", "AxisStartPoint", this.AxisStartPoint.x, this.AxisStartPoint.y, this.AxisStartPoint.z);
        String e = String.format("%s %0.2f %0.2f %0.2f", "AxisFinishPoint", this.AxisFinishPoint.x, this.AxisFinishPoint.y, this.AxisFinishPoint.z);
        return new String[] {s, e}.toString();  // FIXME
    }
}
