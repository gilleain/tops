package python;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.List;
import java.util.Random;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import python.model.Cartoon;
import python.model.Chain;
import python.model.FixedType;
import python.model.Hand;
import python.model.Neighbour;
import python.model.SSE;

public class Optimise {
    

    private class Intersection {
        public Vector2d point;
        public int type;

        public Intersection(Vector2d point, int type) {
            this.point = point;
            this.type = type;
        }
    }

    private int NOT_CROSSING = 0;
    private int CROSSING = 1;
    private int SUPERIMPOSING = 2;
    private double PARALLEL_SCALE = 5.0;
    private int N_ENERGY_COMPS = 9;
    private double AnglePenalty		 = 0;
    private double ChainPenalty		 = 10;
    private double ClashPenalty		 = 1500;
    private double LineHitPenalty		 = ClashPenalty / 2;
    private double CrossPenalty		 = 250;
    private double HandPenalty		 = 500;
    private double NeighbourPenalty	 = 50;
    private double InsideBarrelPenalty	 = 500;
    private double Multiplicity		 = 4;
    private int NoConfigs		 = 50;
    private int RandomSeed		 = 28464;
    private double StartTemperature	 = 10000;
    private double FinishTemperature	 = 9000;
    private double Decrement		 = 10;
    private double StepSize		 = 100;
    private double ArcsSample		 = 0;
    private double LineSample		 = 25;
    private double GridSize		 = 50;
    private double GridUnitSize		 = 50;
    private Random random = new Random();

    // TODO refactor away this global! 
    private long[][] Energy;

    private double calculateEnergy(Chain chain) {
        double[] EnergyComps = new double[N_ENERGY_COMPS];
        double TotalEnergy = 0;

        SSE root = chain.getSSEs().get(0);

        if (ClashPenalty > 0) {
            for (SSE sseA : chain.iterNext(root)) {
                for (SSE sseB : chain.iterNext(sseA)) {
                    for (SSE sseC : chain.iterFixed(sseA)) {
                        for (SSE sseD : chain.iterFixed(sseB)) {
                            if (sseC == sseD) continue;
                            double d = Chain.distance2D(sseC, sseD);
                            double D = sseC.SymbolRadius + sseD.SymbolRadius;
                            if (d < D) {
                                double DHard = 3 * D / 4;
                                double Ce;
                                if (d <= DHard) Ce = ClashPenalty;
                                else Ce = ClashPenalty * Math.exp(d - DHard / d - D);
                                TotalEnergy += Ce;
                                EnergyComps[0] += Ce;
                                //print TotalEnergy, d, D, DHard
                            }
                        }
                    }
                }
            }
        }
        if (ChainPenalty > 0) {
            for (SSE sseA : chain.getSSEs()) {
                SSE sseB = sseA.To;
                if (sseB == null || (sseA.isTerminus() && sseB.isTerminus())) continue;
                double d = Chain.distance2D(sseA, sseB);
                double D = sseA.SymbolRadius + sseB.SymbolRadius;
                if (d > D) {
                    double penalty = (d - D) / GridUnitSize;
                    double Ce = Math.pow(penalty , 2) * ChainPenalty;
                    //print "d > D for", sseA, sseB, int(d), D, Ce
                    TotalEnergy += Ce;
                    EnergyComps[1] += Ce;
                }
            }
        }

        if (NeighbourPenalty > 0) {
            for (SSE sseA : chain.getSSEs()) {
                Neighbour first = sseA.Neighbours.get(0);   // assumes sorted...
                //NOTE: tops files do not contain neighbour distances!
                if (first.distance == -1) break;	
                for (int i = 0; i < sseA.Neighbours.size(); i++) {
                    Neighbour neighbour = sseA.Neighbours.get(i); 
                    SSE sseB = neighbour.sse;
                    double d = Chain.distance2D(sseA, sseB);
                    double D = sseA.SymbolRadius + sseB.SymbolRadius;
                    double ratio = first.distance / neighbour.distance;
                    double Ce = Math.pow((d - D) / GridUnitSize, 2) * NeighbourPenalty * Math.pow(ratio, 2);
                    TotalEnergy += Ce;
                    EnergyComps[2] += Ce;
                }
            }
        }

        if (CrossPenalty > 0) {
            for (SSE sseA : chain.getSSEs().subList(0, chain.numberOfSSEs() - 3)) {
                if (sseA.To == null || (sseA.isTerminus() && sseA.To.isTerminus())) continue;
                for (SSE sseB : chain.rangeFrom(sseA.To.To)) {
                    if (sseB.To == null || (sseB.isTerminus() && sseB.To.isTerminus())) continue;
                    Intersection intersection = lineCross(sseA, sseA.To, sseB, sseB.To);
                    if (intersection.type == CROSSING) {
                        //print "crossing between", sseA, "-", sseA.To, "&&", sseB, "-", sseB.To
                        TotalEnergy += CrossPenalty;
                        EnergyComps[3] += CrossPenalty;
                    }
                    if (intersection.type == SUPERIMPOSING) {
                        //print "superimposition of lines", sseA, "-", sseA.To, "&&", sseB, "-", sseB.To
                        TotalEnergy += PARALLEL_SCALE * CrossPenalty;
                        EnergyComps[3] += PARALLEL_SCALE * CrossPenalty;
                    }
                }
            }
        }

        if (HandPenalty > 0 && chain.numberOfSSEs() > 4) {
            for (SSE sse : chain.getSSEs()) {
                if (sse.Chirality != Hand._no_hand && chain.Hand2D(sse) != sse.Chirality) {
                    TotalEnergy += HandPenalty;
                    EnergyComps[4] += HandPenalty;
                }
            }
        }

        if (AnglePenalty > 0) {
            for (SSE sse : chain.getSSEs().subList(0, chain.numberOfSSEs() - 2)) {
                double D = angle(sse, sse.To, sse.To.To);
                double Ce = AnglePenalty * (1.0 - Math.abs(Math.cos(Multiplicity * D)));
                TotalEnergy += Ce;
                EnergyComps[5] += Ce;
            }
        }

        if (LineHitPenalty > 0) {
            for (SSE sseA : chain.getSSEs()) {
                SSE sseB = sseA.To;
                if (sseB == null || (sseA.isTerminus() && sseB.isTerminus())) continue;
                if (Chain.LineHitSymbol(chain, sseA, sseB) != null) {
                    // print "line hit symbol", sseA, sseB
                    TotalEnergy += LineHitPenalty;
                    EnergyComps[6] += LineHitPenalty;
                }
            }
        }	

        if (InsideBarrelPenalty > 0) {
            for (SSE sse : chain.iterFixed(chain.getSSEs().get(0))) {
                if (sse.FixedType == FixedType.FT_BARREL || sse.FixedType == FixedType.FT_CURVED_SHEET) {
                    double Ce = insideBarrelEnergy(chain, sse, InsideBarrelPenalty);
                    TotalEnergy += Ce;
                    EnergyComps[7] += Ce;
                }
            }
        }

        EnergyComps[8] = TotalEnergy;
        return TotalEnergy;
    }


    /*
    function angle

    Tom F. August 1992

    Function to return the angle between two vectors
     */

    private double angle( SSE p, SSE q, SSE r ) {

        double l1 = Math.sqrt( SQR(p.getCartoonX() - q.getCartoonX()) + SQR( p.CartoonY - q.CartoonY) );
        double l2 = Math.sqrt( SQR(r.getCartoonX() - q.getCartoonX()) + SQR( r.CartoonY - q.CartoonY) );
        if ( l1==0.0 || l2==0.0 ){
            return 0.0;
        }
        l1 = ( (p.getCartoonX() - q.getCartoonX())*(r.getCartoonX() - q.getCartoonX()) 
                + (p.CartoonY - q.CartoonY)*(r.CartoonY - q.CartoonY) ) / (l1 * l2);
        if (l1 < -1.0) l1 = -1.0;
        if (l1 > 1.0) l1 = 1.0;
        return Math.acos(l1);
    }

    private double SQR(double x) { return x * x; }

    private double insideBarrelEnergy(Chain chain, SSE FixedStart, double Penalty) {
        Chain.Circle boundingCircle = chain.FixedBoundingCircle(FixedStart);
        double energy = 0;
        for (SSE sse : chain.getSSEs()) {
            SSE foundFixedStart = chain.FindFixedStart(sse);
            if (foundFixedStart != FixedStart && sse.IsInCircle(boundingCircle)) {
                energy += Penalty;
            }
        }
        return energy;
    }

    public void optimise(Cartoon cartoon) {
        System.out.println("Beginning optimization of cartoon");
        System.out.println("Temperature   LowestEnergy   Acceptance ratio (%)");

        double Seed = -RandomSeed; // TODO : optionally pass in this seed?
        double CurrentTemperature = StartTemperature;

        int NumberSymbols = cartoon.getSSEs().size();
        if (NumberSymbols < 3) return;


        int NumberFixed = cartoon.NumberFixed();

        double[] X = new double[NumberSymbols];
        double[] Y = new double[NumberSymbols];
        double[] E = new double[(NumberFixed + 1)];
        double[] I = new double[(NumberFixed + 1)];

        double MaxXMove = gridFix(NumberSymbols * GridUnitSize / 4);
        double MaxYMove = MaxXMove;

        // generate starting point for optimization
        SSE root = cartoon.getSSEs().get(0);
        int i = 0;
        for (SSE sseA : cartoon.iterNext(root)) {
            if (sseA == null) break;
            double MoveY = i * GridSize - sseA.CartoonY;
            double MoveX = - sseA.getCartoonX();
            sseA.CartoonY = (int) (i * GridSize);
            sseA.setCartoonX(0);

            sseA.SymbolPlaced = true;

            for (SSE sseB : cartoon.iterFixed(sseA)) {
                if (sseB == null) break;
                sseB.setCartoonX((int)(sseB.getCartoonX() + MoveX));
                sseB.CartoonY += MoveY;
                sseB.SymbolPlaced = true;
            }
            i++;
        }

        //store coords
        int j = 0;
        for (SSE sse : cartoon.getSSEs()) {
            X[j] = sse.getCartoonX();
            Y[j] = sse.CartoonY;
            j++;
        }

        //optimize
        SSE CenterFixed = cartoon.LargestFixed();
        // print "CenterFixed =", CenterFixed
        int NoMove = cartoon.NumberLink(CenterFixed);

        double LowestEnergy = calculateEnergy(cartoon);
        double StartingEnergy = LowestEnergy;
        double CurrentEnergy = LowestEnergy;

        //increase values
        int LocNoConfigs = NoConfigs * NumberFixed;

        while (CurrentTemperature > FinishTemperature) {
            int NumberLow = 0;
            for (int configNumber = 0; configNumber < LocNoConfigs; configNumber++) {

                SSE sse = root;
                while (sse == CenterFixed) {
                    List<SSE> nextList = cartoon.iterNext(root);
                    // print nextList
                    sse = nextList.get(random.nextInt(nextList.size()));
                }

                double rx = 0;
                double ry = 0;
                while (rx != 0 && ry != 0) {
                    Point2d p = generateRandomXandY(sse);
                    rx = p.x;
                    ry = p.y;
                }

                //truncate to within a given range from the center
                double lx = sse.getCartoonX() + rx - CenterFixed.getCartoonX();
                if (Math.abs(lx) > MaxXMove) {
                    if (lx < 0) 	rx += -lx - MaxXMove;
                    else  		rx += -lx + MaxXMove;
                }

                double ly = sse.CartoonY + ry - CenterFixed.CartoonY;
                if (Math.abs(lx) > MaxYMove) {
                    if (ly < 0) 	ry += -ly - MaxYMove;
                    else  		ry += -ly + MaxYMove;
                }

                //roll for number of moves
                int NumberMove;
                if (NoMove == 0) {
                    NumberMove = 1;
                } else {
                    NumberMove = random.nextInt(NoMove) + 1;
                }

                //replace by test values
                int l = 0;
                for (SSE sseA : cartoon.iterNext(sse)) {
                    if (sseA == CenterFixed || l == NumberMove) break;
                    for (SSE sseB : cartoon.iterFixed(sseA)) {
                        sseB.setCartoonX((int) (sseB.getCartoonX() + rx));
                        sseA.setCartoonX((int) (sseA.getCartoonX() + ry));
                    }
                    l += 1;
                }

                //test step
                double NewEnergy = calculateEnergy(cartoon);
                boolean accept = metropolis(NewEnergy - CurrentEnergy, CurrentTemperature);
                if (accept) {
                    NumberLow += 1;
                    CurrentEnergy = NewEnergy;
                } else {
                    i = 0;
                    for (SSE sseA : cartoon.iterNext(sse)) {
                        if (sseA == CenterFixed || i == NumberMove) break;
                        for (SSE sseB : cartoon.iterFixed(sseA)) {
                            sseB.setCartoonX((int) (sseB.getCartoonX() - rx));
                            sseA.setCartoonX((int) (sseA.getCartoonX() - ry));
                        }
                        i += 1;
                    }
                }

                //save if best energy
                if (CurrentEnergy < LowestEnergy) {
                    LowestEnergy = CurrentEnergy;
                    int k = 0;
                    for (SSE sseZ : cartoon.getSSEs()) {
                        X[k] = sseZ.getCartoonX();
                        Y[k] = sseZ.CartoonY;
                        k++;
                    }
                }
            }

            double fraction = NumberLow * 100 / LocNoConfigs;
            System.out.println(String.format("%10ld %12ld %17d", CurrentTemperature, LowestEnergy, fraction));

            // lower the temperature
            CurrentTemperature = CurrentTemperature * (100 - Decrement) / 100;
        }

        //set the coordinates to the saved best coordinates
        int z = 0;
        for (SSE sse : cartoon.getSSEs()) {
            sse.setCartoonX((int) X[z]);
            sse.CartoonY = (int) Y[z];
            z++;
        }

        calculateEnergy(cartoon);
        PrintEnergy(Energy);
    }

    private void PrintEnergy(long[][] EnergyComps) {
        System.out.println(String.format("Clash energy            %ld" , EnergyComps[0]));
        System.out.println(String.format("Chain energy            %ld" , EnergyComps[1]));
        System.out.println(String.format("Neighbour energy        %ld" , EnergyComps[2]));
        System.out.println(String.format("LineCross energy        %ld" , EnergyComps[3]));
        System.out.println(String.format("Chirality energy        %ld" , EnergyComps[4]));
        System.out.println(String.format("Angle energy            %ld" , EnergyComps[5]));
        System.out.println(String.format("Line hit energy         %ld" , EnergyComps[6]));
        System.out.println(String.format("Inside barrel energy    %ld" , EnergyComps[7]));
        System.out.println(String.format("Total Energy            %ld" , EnergyComps[8]));
    }


    private Point2d generateRandomXandY(SSE sse) {
        double rx = 0;
        double ry = 0;

        double randomNumber = random.nextDouble();
        if (randomNumber <= LineSample / 100.0 && sse.Fixed != null) {
            if (sse.From != null) {
                randomNumber = random.nextDouble();
                randomNumber *= randomNumber;
                rx += randomNumber * (sse.From.getCartoonX() - sse.getCartoonX());
                ry += randomNumber * (sse.From.CartoonY - sse.CartoonY);
            }
            if (sse.To != null) {
                randomNumber = random.nextDouble();
                randomNumber *= randomNumber;
                rx += randomNumber * (sse.To.getCartoonX() - sse.getCartoonX());
                ry += randomNumber * (sse.To.CartoonY - sse.CartoonY);
            }
            rx = gridFix(rx);
            ry = gridFix(ry);
        }

        int direction;
        if (rx != 0 && ry != 0) {
            if (random.nextDouble() < 0.5) {
                direction = -1;
            } else {
                direction = 1;
            }
            rx = gridFix(StepSize * random.nextDouble()) * direction;
            double lx = 0;
            double ly = 0;
            double rv = 0;
            if (random.nextDouble() <= ArcsSample / 100.0) {
                if (random.nextDouble() < 0.5) {
                    lx = sse.To.getCartoonX() - sse.CartoonY;
                    ly = sse.To.CartoonY - sse.CartoonY;
                    rv = Math.sqrt((lx * lx) + (ly * ly));
                } else {
                    lx = sse.From.getCartoonX() - sse.CartoonY;
                    ly = sse.From.CartoonY - sse.CartoonY;
                    rv = Math.sqrt((lx * lx) + (ly * ly));
                }
            }
            if (ly !=0 && rv != 0) {
                double t = Math.atan(lx / Math.abs(ly));
                double div = rx / rv;
                if (ly < 0) t = Math.PI - t + div;
                else t = t + div;
                rx = gridFix(lx + rv * Math.sin(t));	
                ry = gridFix(ly + rv * Math.cos(t));
            } else {
                if (random.nextDouble() < 0.5) {
                    direction = -1;
                } else {
                    direction = 1;
                }
                ry = gridFix(StepSize * random.nextDouble()) * direction;
            }
        }
        return new Point2d(rx, ry);
    }

    private int gridFix(double x) { return (int) ( ((x + (GridSize / 2)) / GridSize) * GridSize ); }

    private boolean metropolis(double E, double T) {
        if (E < 0) {
            return true;
        } else if (T > 0) {
            double exponent = -(E * 100.0) / T;
            return random.nextDouble() < Math.exp(exponent);
        } else {
            return false;
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
        else	return -1.0;
    }

    // XXX was called 'Const' but this is a java keyword!
    private double constant(double x1, double y1, double x2, double y2) {
        return (x2 * y1) - (x1 * y2) / (x2 - x1);
    }

    private Intersection simpleIntersection(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        double TOL = 0.01;
        double denom = (bx - ax) * (dy - cy) - (by - ay) * (dx - cx);
        //print "denom = ", denom
        if (denom > TOL) {
            double r = (ay - cy) * (dx - cx) - (ax - cx) * (dy - cy) / denom;
            double s = (ay - cy) * (bx - ax) - (ax - cx) * (by - ay) / denom;
            if (0 <= r && r <= 1 && 0 <= s && s <= 1) {
                //print "0<=r<=1,0<=s<=1!", r, s
                return new Intersection(new Vector2d(ax + r * (bx - ax), ay + r * (by - ay)), CROSSING);
            } else {
                return new Intersection(new Vector2d(0, 0), NOT_CROSSING);
            }
        } else {
            return new Intersection(new Vector2d(0, 0), SUPERIMPOSING);
        }
    }

    private Intersection lineCross(SSE p, SSE q, SSE r, SSE s) {
        double TOL = 0.01;
        double px = p.getCartoonX();
        double py = p.CartoonY;
        double qx = q.getCartoonX();
        double qy = q.CartoonY;
        double rx = r.getCartoonX();
        double ry = r.CartoonY;
        double sx = s.getCartoonX();
        double sy = s.CartoonY;

        Intersection intersection = simpleIntersection(px, py, qx, qy, rx, ry, sx, sy);
        if (intersection.type == CROSSING) {
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
                return new Intersection(new Vector2d(x, y), SUPERIMPOSING);
            } else {
                //print "not crossing, both vertical:", p, q, r, s
                return new Intersection(new Vector2d(x, y), NOT_CROSSING);
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
                return new Intersection(new Vector2d(x, y), CROSSING);
            } else {
                //print "not crossing, first vertical:", p, q, r, s
                return new Intersection(new Vector2d(x, y), NOT_CROSSING);
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
                    return new Intersection(new Vector2d(x, y), CROSSING);
                } else {
                    //print "not crossing, second vertical:", p, q, r, s
                    return new Intersection(new Vector2d(x, y), NOT_CROSSING);
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
                    return new Intersection(new Vector2d(x, y), SUPERIMPOSING);
                } else {
                    //print "not crossing, parallel :", p, q, r, s
                    return new Intersection(new Vector2d(x, y), NOT_CROSSING);
                }
            }

            // Find crossing point
            x = slope(-m1, c1, -m2, c2);
            y = constant(-m1, c1, -m2, c2) ;

            // Does crossing point lie inside either line (only need to test one)
            if (overlap(x, px, qx, rx, sx) && overlap(y, py, qy, ry, sy)) {
                return new Intersection(new Vector2d(x, y), CROSSING);
            } else {
                //print "not crossing other:", p, q, r, s
                //print "crossing point", x, y, "for", px, py, qx, qy, rx, ry, sx, sy
                return new Intersection(new Vector2d(x, y), NOT_CROSSING);
            }
        }
        return null;
    }
}
