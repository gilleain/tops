package tops.port.model;

import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Axis {
    
    private char SSEType;
    private double length;
    public Vector3d AxisStartPoint;
    public Vector3d AxisFinishPoint;

    public Axis(List<Point3d> coords) { 
        int n = coords.size();
        boolean isE = (this.SSEType == 'E');
        if ((n < 3 && isE) || (n < 5 && !isE)) {
            this.AxisStartPoint = new Vector3d(coords.get(0));
            this.AxisFinishPoint = new Vector3d(coords.get(n - 1));
            this.length = 0.0;
        } else {
            this.length = calculate(toDoubleArr(coords), isE, n);
        }
    }
    
    private double[][] toDoubleArr(List<Point3d> coords) {  // unpleasant, but still
        double[][] x = new double[coords.size()][3];
        for (int i = 0; i < coords.size(); i++) {
            x[i][0] = coords.get(i).x;
            x[i][1] = coords.get(i).y;
            x[i][2] = coords.get(i).z;
        }
        return x;
    }
    
    private double calculate(double[][] x, boolean isE, int n) {
        /* Calculate vectors orthogonal to the helix axis */
        int l=n-2;
        double[] ap = new double[] {0.0, 0.0, 0.0};
        double[][] p = new double[n][3];
        
        for (int j=0;j<3;j++) {
            
                for (int i=0;i<l;i++) {
                        p[i][j] = x[i][j] + x[i+2][j] - 2.0*x[i+1][j];
                        if (isE) p[i][j] = x[i+1][j] + p[i][j] / 4.0;
                        ap[j] += p[i][j];
                }

         /* Additional vectors for strands:
            add vector on beginning and end
            helices:
                use 0.0, 0.0, 0.0 as additional point */
                if (isE) {
                        ap[j] += p[l][j]   = x[0][j]   + ( 2.0*x[1][j]   - x[0][j]   - x[2][j] )   / 4.0;
                        ap[j] += p[l+1][j] = x[n-1][j] + ( 2.0*x[n-2][j] - x[n-3][j] - x[n-1][j] ) / 4.0;
                } else {
                        p[l][j] = 0.0;
                }

                ap[j]/=(float) (l + ((isE)?2:1));
            for (int i = 0; i < (l + ((isE) ? 2 : 1)); i++) {
                p[i][j] -= ap[j];
            }
        }

        /* Calculate covariance matrix */
        l += ((isE)?2:1);
        double[][] m = new double[3][3];
        for (int j=0;j<3;j++) {
                for (int k=0;k<3;k++) {
                    ap[j] = 0.0;
                        for (int i=0;i<l;i++) {
                                ap[j] += p[i][j] * p[i][k];
                        }
                        m[j][k] = ap[j] / (float) l;
                }
        }

        /* Diagonalise matrix and sort eigen values */
        double[] v = new double[n];
        double[][] e = new double[n][n];
        int njr = Jacobi( m, 3, v, e);    // TODO - try/catch?
        
        Eigsrt( v, e, 3 );

        /* Store unit vector of axis */
        int j=((isE)?0:2);
        for (int i=0;i<3;i++) {
                ap[i] = e[i][j];
        }
        
        /* Calculate centroid of secondary structure */
        double[] st = {0, 0, 0};
        double[] fn = {0, 0, 0};
        double[] c =  {0, 0, 0};
        for (j=0;j<3;j++) {
                for (int i=0;i<n;i++) {
                        c[j] += x[i][j];
                }
                c[j] /= (float) n;
                st[j] = x[0][j] - c[j];
                fn[j] = x[n-1][j] - c[j];
        }

        /* Calculate beginning and end */
        double sb = DotProduct( ap, st );
        double se = DotProduct( ap, fn );
        for (int i=0;i<3;i++) {
                st[i] = c[i] + sb * ap[i];
                fn[i] = c[i] + se * ap[i];
        }
        
        AxisStartPoint = new Vector3d(st[0], st[1], st[2]);
        AxisFinishPoint = new Vector3d(fn[0], fn[1], fn[2]);

        /* return length of axis */
        return Math.sqrt(SQR(st[0]-fn[0])+SQR(st[1]-fn[1])+SQR(st[2]-fn[2]));
    }
    
    private double SQR(double x) { return x * x; }
    
    /*
        Function to calculate dot product.
    
        Written by Tom Flores, Laboratory of Molecular Biology,
        Department of Crystallography, Birkbeck, Malet Street,
        London. WC1E 7HX.
    
        Version 1: 21st November 1991.
     */
    private double DotProduct(double[] a, double[] b) {
        int i;
        double dotp = 0.0;

        for (i = 0; i < 3; i++) {
            dotp += a[i] * b[i];
        }

        return dotp;
    }
    

    /* 
        function eigsrt

        Given the eigenvalues d[n] and eigenvectors v[n][n] as output from jacobi
        this routine sorts the eigenvalues into descending order and rearranges
        the columns of v correspondingly
     */
    private void Eigsrt(double[] d, double[][] v, int n) {

        for (int i=0;i<n-1;i++) {
            int k = i;
            double p=d[k];
            for (int j=i+1;j<n;j++) {
                if (d[j] >= p) {
                    k = j;
                    p=d[k];
                }
            }
            if (k != i) {
                d[k]=d[i];
                d[i]=p;
                for (int l =0;l<n;l++) {
                    p=v[l][i];
                    v[l][i]=v[l][k];
                    v[l][k]=p;
                }
            }
        }
    }
    
    /* 
    function jacobi

    Computes all eignevalues and eigenvectors of a real symmetric matrix a[n][n].
    On output, elements of a above diagonal are destroyed. d[n] returns the
    eigenvalues of a. v[n][n] is a matrix whose columns contain, on output, the
    normalised eigenvectors of a, nrot returns the number of jacobi rotations that
    were required.

    From Numerical Recipes by Press et al.

    Updated to remove error message and return non zero on failure.

    Tom F. July 1992.
*/
    private void ROTATE(double s, double tau, double[][] a, int i, int j, int k, int l) {
        double g = a[i][j];
        double h = a[k][l];
        a[i][j] = g-s*(h+g*tau);
        a[k][l] = h+s*(g-h*tau);
    }

    private int Jacobi(double[][] a, int n, double[] d, double[][] v) {
        double tresh,theta,tau,t,s,h,g,c;
        double[] b = new double[n];
        double[] z = new double[n];
        
        for (int ip=0;ip<n;ip++) {
            for (int iq=0;iq<n;iq++) {
                v[ip][iq]=0.0;
            }
            v[ip][ip]=1.0;
        }
        for (int ip=0;ip<n;ip++) {
            b[ip]=d[ip]=a[ip][ip];
            z[ip]=0.0;
        }
        
        int nrot=0;
        for (int i=0;i<50;i++) {
            double sm=0.0;
            for (int ip=0;ip<n-1;ip++) {
                for (int iq=ip+1;iq<n;iq++) {
                    sm += Math.abs(a[ip][iq]);
                }
            }
            if (sm == 0.0) {
                return nrot;
            }
            if (i < 3) {
                tresh=0.2*sm/(n*n);
            } else {
                tresh=0.0;
            }
            for (int ip=0;ip<n-1;ip++) {
                for (int iq=ip+1;iq<n;iq++) {
                    g=100.0*Math.abs(a[ip][iq]);
                    if (i > 3 && Math.abs(d[ip])+g == Math.abs(d[ip]) && Math.abs(d[iq])+g == Math.abs(d[iq]))
                        a[ip][iq]=0.0;
                    else if (Math.abs(a[ip][iq]) > tresh) {
                        h=d[iq]-d[ip];
                        if (Math.abs(h)+g == Math.abs(h))
                            t=(a[ip][iq])/h;
                        else {
                            theta=0.5*h/(a[ip][iq]);
                            t=1.0/(Math.abs(theta)+Math.sqrt(1.0+theta*theta));
                            if (theta < 0.0) t = -t;
                        }
                        c=1.0/Math.sqrt(1+t*t);
                        s=t*c;
                        tau=s/(1.0+c);
                        h=t*a[ip][iq];
                        z[ip] -= h;
                        z[iq] += h;
                        d[ip] -= h;
                        d[iq] += h;
                        a[ip][iq]=0.0;
                        for (int j=0;j<=ip-1;j++) {
                            ROTATE(s, tau, a,j,ip,j,iq);
                        }
                        for (int j=ip+1;j<=iq-1;j++) {
                            ROTATE(s, tau, a,ip,j,j,iq);
                        }
                        for (int j=iq+1;j<n;j++) {
                            ROTATE(s, tau, a,ip,j,iq,j);
                        }
                        for (int j=0;j<n;j++) {
                            ROTATE(s, tau, v,j,ip,j,iq);
                        }
                        ++nrot;
                    }
                }
            }
            for (int ip=0;ip<n;ip++) {
                b[ip] += z[ip];
                d[ip]=b[ip];
                z[ip]=0.0;
            }
        }
        return nrot;
    }

    
    public Vector3d getVector() {
        // return vector(end - start)
        Vector3d v = new Vector3d(AxisFinishPoint);
        v.sub(AxisStartPoint);
        return v;
    }
    
    public Point3d getCentroid() {  // XXX is 'centroid' the right word?
        // return sum(start, end) / 2
        Point3d p = new Point3d(AxisStartPoint);
        p.add(AxisFinishPoint);
        p.scale(0.5);
        return p;
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
        if (this.AxisStartPoint == null) {
            return "[AxisStartPoint, AxisEndPoint]";
        } else {
            String s = String.format("%s %2.2f %2.2f %2.2f", "AxisStartPoint", this.AxisStartPoint.x, this.AxisStartPoint.y, this.AxisStartPoint.z);
            String e = String.format("%s %2.2f %2.2f %2.2f", "AxisFinishPoint", this.AxisFinishPoint.x, this.AxisFinishPoint.y, this.AxisFinishPoint.z);
            return String.format("[%s, %s]", s, e);
        }
    }
}
