package tops.port.model;

import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Axis {
    
    private SSEType sseType;
    private double length;
    private Vector3d axisStartPoint;
    private Vector3d axisFinishPoint;

    public Axis(SSEType sseType, List<Point3d> coords) { 
        int n = coords.size();
        boolean isE = (this.sseType == SSEType.EXTENDED);
        if ((n < 3 && isE) || (n < 5 && !isE)) {
            this.axisStartPoint = new Vector3d(coords.get(0));
            this.axisFinishPoint = new Vector3d(coords.get(n - 1));
            this.length = 0.0;
        } else {
            this.length = calculate(toDoubleArr(coords), isE, n);
        }
    }
    
    public Axis() {
        // TODO Auto-generated constructor stub
    }
    

    public SSEType getSseType() {
        return sseType;
    }

    public Vector3d getAxisStartPoint() {
        return axisStartPoint;
    }

    public Vector3d getAxisFinishPoint() {
        return axisFinishPoint;
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
        int njr = jacobi( m, 3, v, e);    // TODO - try/catch?
        
        eigsrt( v, e, 3 );

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
        double sb = dotProduct( ap, st );
        double se = dotProduct( ap, fn );
        for (int i=0;i<3;i++) {
                st[i] = c[i] + sb * ap[i];
                fn[i] = c[i] + se * ap[i];
        }
        
        axisStartPoint = new Vector3d(st[0], st[1], st[2]);
        axisFinishPoint = new Vector3d(fn[0], fn[1], fn[2]);

        /* return length of axis */
        return Math.sqrt(square(st[0]-fn[0])+square(st[1]-fn[1])+square(st[2]-fn[2]));
    }
    
    private double square(double x) { return x * x; }
    
    /*
        Function to calculate dot product.
    
        Written by Tom Flores, Laboratory of Molecular Biology,
        Department of Crystallography, Birkbeck, Malet Street,
        London. WC1E 7HX.
    
        Version 1: 21st November 1991.
     */
    private double dotProduct(double[] a, double[] b) {
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
    private void eigsrt(double[] d, double[][] v, int n) {

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
    private void rotate(double s, double tau, double[][] a, int i, int j, int k, int l) {
        double g = a[i][j];
        double h = a[k][l];
        a[i][j] = g-s*(h+g*tau);
        a[k][l] = h+s*(g-h*tau);
    }

    private int jacobi(double[][] a, int n, double[] d, double[][] v) {
        double tresh;
        double theta;
        double tau;
        double t;
        double s;
        double h;
        double g;
        double c;
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
                            rotate(s, tau, a,j,ip,j,iq);
                        }
                        for (int j=ip+1;j<=iq-1;j++) {
                            rotate(s, tau, a,ip,j,j,iq);
                        }
                        for (int j=iq+1;j<n;j++) {
                            rotate(s, tau, a,ip,j,iq,j);
                        }
                        for (int j=0;j<n;j++) {
                            rotate(s, tau, v,j,ip,j,iq);
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
        Vector3d v = new Vector3d(axisFinishPoint);
        v.sub(axisStartPoint);
        return v;
    }
    
    public Point3d getCentroid() {  // XXX is 'centroid' the right word?
        // return sum(start, end) / 2
        Point3d p = new Point3d(axisStartPoint);
        p.add(axisFinishPoint);
        p.scale(0.5);
        return p;
    }
    
    public double getLength() {
        return this.length;
    }
    
    private static Vector3d diff(Vector3d vec1, Vector3d vec2) {
        Vector3d vec3 = new Vector3d(vec1);
        vec3.sub(vec2);
        return vec3;
    }
    
    private static Vector3d mult(Vector3d a, Vector3d b) {
        Vector3d c = new Vector3d(a);
        c.angle(b);
        return c;
    }
    
    private static Vector3d mult(Vector3d a, double d) {
        Vector3d x = new Vector3d(a);
        x.scale(d);
        return x;
    }
    
    private static Vector3d plus(Vector3d a, Vector3d b) {
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
    public TorsionResult closestApproach(Axis other) {
        Vector3d bj = this.axisStartPoint;// XXX which way round?
        Vector3d ek = this.axisFinishPoint;// XXX which way round?
        Vector3d bk = other.axisStartPoint;// XXX which way round?
        Vector3d ej = other.axisFinishPoint;  // XXX which way round?
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
            return new TorsionResult( pck, pcj, sk, sj, torsion(ekbk, pck, pcj, ejbj));
        }
    }
    
    private static double div(Vector3d vec, double scale) {
        vec.scale(scale);
        return vec.length();    // XXX TODO
    }

    public static double torsion(Vector3d a, Vector3d b, Vector3d c, Vector3d d) {
        final double conv = 0.01745329;

        // Calculate vectors and lengths a-b, b-c, c-d 
        Vector3d aDiffB = diff(b, a);
        Vector3d bDiffC = diff(c, b);
        Vector3d cDiffD = diff(d, c);

        double lenAB = aDiffB.length();
        double lenBC = bDiffC.length();
        double lenCD = cDiffD.length();

        // Error check, are any vectors of zero length ?
        if (lenAB == 0.0 || lenBC == 0.0 || lenCD == 0.0) return -999.0;

        // Calculate dot products to form cosines 
        Vector3d abBC = mult(mult(aDiffB, bDiffC), 1 / (lenAB * lenBC));
        Vector3d abCD = mult(mult(aDiffB, cDiffD), 1 / (lenAB * lenCD));
        Vector3d bcCD = mult(mult(bDiffC, cDiffD), 1 / (lenBC * lenCD));

        // Form sines 
        double sAB = Math.sqrt(1.0 - mult(abBC, abBC).length());    // XXX wrong
        double sBC = Math.sqrt(1.0 - mult(bcCD, bcCD).length());    // XXX wrong
        double s = sAB * sBC;
        if (s == 0.0) return 0.0;

        double costor = div(diff(mult(abBC , bcCD), abCD), s);
        double costsq = costor * costor;
        double tor = 0;
        if (costsq >= 1.0 && costor < 0.0) tor = 180.0;

        // If the angle is not == 180 degs calculate sign using sine 
        if (costsq < 1.0) {
            double sintor = Math.sqrt(1.0 - costsq);
            tor = Math.atan2(sintor, costor);
            tor = tor / conv;

            // Find unit vectors 
            aDiffB.scale(lenAB);
            bDiffC.scale(lenBC);
            cDiffD.scale(lenCD);

            // Find determinant 
            double sign  = aDiffB.x * (bDiffC.y * cDiffD.z - bDiffC.z * cDiffD.y);
            sign += aDiffB.y * (bDiffC.z * cDiffD.x - bDiffC.x * cDiffD.z);
            sign += aDiffB.z * (bDiffC.x * cDiffD.y - bDiffC.y * cDiffD.x);

            // Change sign if necessary 
            if (sign < 0.0) tor = -1.0 * tor;
        }

        // Return torsion 
        return tor;
     }

    public String toString() {
        if (this.axisStartPoint == null) {
            return "[AxisStartPoint, AxisEndPoint]";
        } else {
            String s = String.format("%s %2.2f %2.2f %2.2f", "AxisStartPoint", this.axisStartPoint.x, this.axisStartPoint.y, this.axisStartPoint.z);
            String e = String.format("%s %2.2f %2.2f %2.2f", "AxisFinishPoint", this.axisFinishPoint.x, this.axisFinishPoint.y, this.axisFinishPoint.z);
            return String.format("[%s, %s]", s, e);
        }
    }

    public void setAxisFinishPoint(Vector3d axisFinishPoint) {
        this.axisFinishPoint = axisFinishPoint;
    }

    public void setAxisStartPoint(Vector3d axisStartPoint) {
        this.axisStartPoint = axisStartPoint;
    }
}
