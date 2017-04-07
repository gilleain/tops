package tops.port;

import javax.vecmath.Point2d;

import tops.port.model.Cartoon;
import tops.port.model.FixedType;
import tops.port.model.SSE;

public class ConnectionCalculator {
    
    public void calculateConnections(Cartoon cartoon, double radius) {
        int ExtensionIndex = 0;
        double[] Extensions = new double[] { 2.7, 7.7, 12.7, 17.7, 22.7 };

        for (SSE sse : cartoon.getSSEs()) {
            makeConnection(cartoon, radius, sse, cartoon.getNext(sse), ExtensionIndex, Extensions);
        }
    }
    

    private void makeConnection(Cartoon cartoon, double radius, SSE sse, SSE next, int ExtensionIndex, double[] Extensions) {
        double PSMALL = 0.001;
        if (next == null) return;

        // these are the condition under which the code generates a bent connection,
        // rather than the usual straight line joining symbols 
        if (cartoon.findFixedStart(sse) != cartoon.findFixedStart(next)) return;
        if (sse.hasFixedType(FixedType.SHEET) && !next.hasFixedType(FixedType.SANDWICH)) return;
        SSE r = LineHitSymbol(cartoon, sse, next);
        if (r != null && sse.getCartoonY() == next.getCartoonY()) {

            // we are guaranteed horizontal lines 
            double px = sse.getCartoonX();
            double py = sse.getCartoonY();
            double qx = next.getCartoonX();
            double qy = next.getCartoonY();

            double d1 = (sse.getDirection() == 'U')? -1.0 : 1.0;
        
            double my = r.getCartoonY() + ( d1 * ( PSMALL * Math.abs(px - qx) + radius + nextExtension(ExtensionIndex, Extensions) ));
            double ny = my;
            
            double d2, d3;
            if (px < qx) { 
                d2 = 1.0;
                d3 =-1.0;
            } else{ 
                d2 = -1.0;
                d3 =  1.0;
            }

            double mx = px + (d2 * (PSMALL * Math.abs(px - qx) + radius));
            double nx = qx + (d3 * (PSMALL * Math.abs(px - qx) + radius));

            sse.addConnection(new Point2d(mx, my));
            sse.addConnection(new Point2d(nx, ny));
        }
    }
    

    public static SSE LineHitSymbol(Cartoon chain, SSE p,SSE q) {
        double TOL = 0.001;

        double px = p.getCartoonX();
        double py = p.getCartoonY();
        double qx = q.getCartoonX();
        double qy = q.getCartoonY();

        for (SSE r : chain.getSSEs()) {
            double rx = r.getCartoonX();
            double ry = r.getCartoonY();

            double dprx = Math.abs(px - rx);
            double dpry = Math.abs(py - ry);
            double dqrx = Math.abs(qx - rx);
            double dqry = Math.abs(qy - ry);

            if (!((dprx < TOL && dpry < TOL) || (dqrx < TOL && dpry < TOL))) {

                double pr = (dprx * dprx) + (dpry * dpry);
                double pq = Math.pow((px - qx), 2) + Math.pow((py - qy), 2);
                double qr = (dqrx * dqrx) + (dqry * dqry);

                if (Math.min(pr,qr) + pq > Math.max(pr,qr)) {
                    double a  = (pr + pq - qr);
                    pr = Math.sqrt(pr);
                    pq = Math.sqrt(pq);

                    
                    a /= (2.0 * pr * pq);
                    if (Math.abs(a) > 1.0) {
                        if (a < 0) a = -1.0;
                        else     a = 1.0;
                    }
                    a  = Math.acos(a);

                    if (2 * pr * Math.tan(a) <= r.getSymbolRadius()) return r;
                }
            }
        }
        return null;
    }
    

    private double nextExtension(int ExtensionIndex, double[] Extensions) {
        if (ExtensionIndex >= Extensions.length) ExtensionIndex = 0;
        double extension = Extensions[ExtensionIndex];
        ExtensionIndex += 1;    // XXX incrementing a scope-local variable FIXME 
        return extension;
    }



}
