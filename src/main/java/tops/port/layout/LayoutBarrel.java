package tops.port.layout;

import tops.port.model.Bridge;
import tops.port.model.Chain;
import tops.port.model.Direction;
import tops.port.model.FixedType;
import tops.port.model.SSE;
import tops.port.model.tse.BaseTSE;

public class LayoutBarrel implements TSELayout {
    
    private double gridUnitSize;
    
    public LayoutBarrel(double gridUnitSize) {
        this.gridUnitSize = gridUnitSize;
    }

    @Override
    public void layout(Chain chain, BaseTSE barrel) {
        System.out.println("laying out barrel...");

        int numberOfStrands = barrel.size();

        double rads = Math.PI / (double) numberOfStrands;
        double X = rads;
        double Y = 0.5 / Math.sin(rads);
        double Z = 0.5 / Math.tan(rads);

        // this bit ensures that TIM barrels get the correct chirality """
        int start = 0;
        int increment = 0;
        if (barrel.get(1).getSymbolNumber() < barrel.get(barrel.size() - 1).getSymbolNumber()) {
            start = 0;
            increment = 1;
        } else {
            start = numberOfStrands - 1;
            increment = -1;
        }

        SSE p = barrel.get(start);
        SSE lastInBarrel = p;
        p.setDirection(Direction.DOWN);
        
//        System.out.println(String.format("make barrel start, nstrands, increment= %s %s %s", start, numberOfStrands, increment));
        int i = start;
        for (int count = 0; count < numberOfStrands; count++) { 
//            System.out.println("make barrel count= " + count);
            X = rads + 2.0 * count * rads;

            SSE q = barrel.get(i);
            q.setFixedType(FixedType.BARREL);   // TODO : remove

            if (count != 0) {
//                chain.moveFixed(p, q);    // TODO : remove?
                q.AssignRelDirection(lastInBarrel);
            }
            lastInBarrel = q;
            q.setCartoonX((int) ((Y * Math.sin(X) - 0.5) * gridUnitSize));
            q.setCartoonY((int) ((Y * Math.cos(X) - Z) * gridUnitSize));
            q.setSymbolPlaced(true);

            double Y1 = Y+1;
            
            // TODO - this should work on the existing TSE...
            for (Bridge bridge : chain.getBridges(q)) {
                SSE r = bridge.getOther(q);
                if (!barrel.contains(r)) {
                    r.setFixedType(FixedType.BARREL);
                    r.AssignRelDirection(q);
                    r.setCartoonX((int) ((Y1 * Math.sin(X) - 0.5) * gridUnitSize));
                    r.setCartoonY((int) ((Y1 * Math.cos(X) - Z) * gridUnitSize));
                    r.setSymbolPlaced(true);

                    Y1 += 1;

//                    chain.moveFixed(p, r); // TODO : remove?

                    // this bit of code is a cop out for about 1% of cases 
                    // where a sheet is attached to a barrel - it just detaches it
                    // TODO ...
//                    for (int k  = 0; k < MAXBP; k++) {
//                        SSE bp = r.BridgePartner.get(k);
//                        if (bp != null && bp != q) {
//                            this.DeleteBPRelation(r, bp);
////                            bp.ShuffleDownBPs(); TODO : remove
//                        }
//                    }
//                    r.ShuffleDownBPs();   TODO - remove - think this was only for NPE
                }
            }
            i += increment;
        }
    }

}
