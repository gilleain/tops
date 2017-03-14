package tops.port;

import static tops.port.IntersectionCalculator.IntersectionType.CROSSING;
import static tops.port.IntersectionCalculator.IntersectionType.SUPERIMPOSING;

import java.util.List;
import java.util.Random;

import javax.vecmath.Point2d;

import tops.port.IntersectionCalculator.Intersection;
import tops.port.calculate.chirality.ChiralityCalculator;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.Hand;
import tops.port.model.Neighbour;
import tops.port.model.SSE;

public class Optimise {
    
    private double PARALLEL_SCALE = 5.0;
    private int N_ENERGY_COMPS = 9;
    
    private double anglePenalty		 = 0;
    private double arcsSample		 = 0;
    private double chainPenalty		 = 10;
    private double clashPenalty		 = 1500;
    private double crossPenalty		 = 250;
    private double decrement		 = 10;
    private double finishTemperature	 = 9000;
    private double gridSize		 = 50;
    private double gridUnitSize		 = 50;
    private double handPenalty		 = 500;
    private double insideBarrelPenalty	 = 500;
    private double lineSample		 = 25;
    private double multiplicity		 = 4;
    private double neighbourPenalty	 = 50;
    private int noConfigs		 = 50;
    private int randomSeed		 = 28464;
    private double startTemperature	 = 10000;
    private double stepSize		 = 100;
    
    private double lineHitPenalty		 = clashPenalty / 2;
    
    private Random random = new Random();

    // TODO refactor away this global! 
    private long[][] Energy;
    
    private IntersectionCalculator intersectionCalculator;
    
    public Optimise() {
        this.intersectionCalculator = new IntersectionCalculator();
    }
    
    private Intersection lineCross(SSE p, SSE q, SSE r, SSE s) {
        return intersectionCalculator.lineCross(
                p.getCartoonCenter(), 
                q.getCartoonCenter(),
                r.getCartoonCenter(),
                s.getCartoonCenter());
    }

    private double calculateEnergy(Chain chain) {
        double[] EnergyComps = new double[N_ENERGY_COMPS];
        double TotalEnergy = 0;

        SSE root = chain.getSSEs().get(0);

        if (clashPenalty > 0) {
            for (SSE sseA : chain.iterNext(root)) {
                for (SSE sseB : chain.iterNext(sseA)) {
                    for (SSE sseC : chain.iterFixed(sseA)) {
                        for (SSE sseD : chain.iterFixed(sseB)) {
                            if (sseC == sseD) continue;
                            double d = Chain.distance2D(sseC, sseD);
                            double D = sseC.getSymbolRadius() + sseD.getSymbolRadius();
                            if (d < D) {
                                double DHard = 3 * D / 4;
                                double Ce;
                                if (d <= DHard) Ce = clashPenalty;
                                else Ce = clashPenalty * Math.exp(d - DHard / d - D);
                                TotalEnergy += Ce;
                                EnergyComps[0] += Ce;
                                //print TotalEnergy, d, D, DHard
                            }
                        }
                    }
                }
            }
        }
        if (chainPenalty > 0) {
            for (SSE sseA : chain.getSSEs()) {
                SSE sseB = sseA.To;
                if (sseB == null || (sseA.isTerminus() && sseB.isTerminus())) continue;
                double d = Chain.distance2D(sseA, sseB);
                double D = sseA.getSymbolRadius() + sseB.getSymbolRadius();
                if (d > D) {
                    double penalty = (d - D) / gridUnitSize;
                    double Ce = Math.pow(penalty , 2) * chainPenalty;
                    //print "d > D for", sseA, sseB, int(d), D, Ce
                    TotalEnergy += Ce;
                    EnergyComps[1] += Ce;
                }
            }
        }

        if (neighbourPenalty > 0) {
            for (SSE sseA : chain.getSSEs()) {
                if (sseA.getNeighbours().size() == 0) continue;
                Neighbour first = sseA.getNeighbours().get(0);   // assumes sorted...
                //NOTE: tops files do not contain neighbour distances!
                if (first.distance == -1) break;	
                for (int i = 0; i < sseA.getNeighbours().size(); i++) {
                    Neighbour neighbour = sseA.getNeighbours().get(i); 
                    SSE sseB = neighbour.sse;
                    double d = Chain.distance2D(sseA, sseB);
                    double D = sseA.getSymbolRadius() + sseB.getSymbolRadius();
                    double ratio = first.distance / neighbour.distance;
                    double Ce = Math.pow((d - D) / gridUnitSize, 2) * neighbourPenalty * Math.pow(ratio, 2);
                    TotalEnergy += Ce;
                    EnergyComps[2] += Ce;
                }
            }
        }

        if (crossPenalty > 0) {
            for (SSE sseA : chain.getSSEs().subList(0, chain.numberOfSSEs() - 3)) {
                if (sseA.To == null || (sseA.isTerminus() && sseA.To.isTerminus())) continue;
                for (SSE sseB : chain.rangeFrom(sseA.To.To)) {
                    if (sseB.To == null || (sseB.isTerminus() && sseB.To.isTerminus())) continue;
                    System.out.print(sseA.getCartoonCenter() + " "
                            + sseA.To.getCartoonCenter() + " "
                            + sseB.getCartoonCenter() + " "
                            + sseB.To.getCartoonCenter() + " ");
                    Intersection intersection = lineCross(sseA, sseA.To, sseB, sseB.To);
                    System.out.println("Intersection " + intersection);
                    if (intersection == null) continue; // XXX FIXME
                    if (intersection.type == CROSSING) {
                        //print "crossing between", sseA, "-", sseA.To, "&&", sseB, "-", sseB.To
                        TotalEnergy += crossPenalty;
                        EnergyComps[3] += crossPenalty;
                    }
                    if (intersection.type == SUPERIMPOSING) {
                        //print "superimposition of lines", sseA, "-", sseA.To, "&&", sseB, "-", sseB.To
                        TotalEnergy += PARALLEL_SCALE * crossPenalty;
                        EnergyComps[3] += PARALLEL_SCALE * crossPenalty;
                    }
                }
            }
        }

        if (handPenalty > 0 && chain.numberOfSSEs() > 4) {
            for (SSE sse : chain.getSSEs()) {
                if (sse.Chirality != Hand.NONE && 
                        ChiralityCalculator.hand2D(chain, sse) != sse.Chirality) {
                    TotalEnergy += handPenalty;
                    EnergyComps[4] += handPenalty;
                }
            }
        }

        if (anglePenalty > 0) {
            for (SSE sse : chain.getSSEs().subList(0, chain.numberOfSSEs() - 2)) {
                double D = angle(sse, sse.To, sse.To.To);
                double Ce = anglePenalty * (1.0 - Math.abs(Math.cos(multiplicity * D)));
                TotalEnergy += Ce;
                EnergyComps[5] += Ce;
            }
        }

        if (lineHitPenalty > 0) {
            for (SSE sseA : chain.getSSEs()) {
                SSE sseB = sseA.To;
                if (sseB == null || (sseA.isTerminus() && sseB.isTerminus())) continue;
                if (Chain.LineHitSymbol(chain, sseA, sseB) != null) {
                    // print "line hit symbol", sseA, sseB
                    TotalEnergy += lineHitPenalty;
                    EnergyComps[6] += lineHitPenalty;
                }
            }
        }	

        if (insideBarrelPenalty > 0) {
            for (SSE sse : chain.iterFixed(chain.getSSEs().get(0))) {
                if (sse.hasFixedType(FixedType.BARREL, FixedType.CURVED_SHEET)) {
                    double Ce = insideBarrelEnergy(chain, sse, insideBarrelPenalty);
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

        double l1 = Math.sqrt( SQR(p.getCartoonX() - q.getCartoonX()) + SQR( p.getCartoonY() - q.getCartoonY()) );
        double l2 = Math.sqrt( SQR(r.getCartoonX() - q.getCartoonX()) + SQR( r.getCartoonY() - q.getCartoonY()) );
        if ( l1==0.0 || l2==0.0 ){
            return 0.0;
        }
        l1 = ( (p.getCartoonX() - q.getCartoonX())*(r.getCartoonX() - q.getCartoonX()) 
                + (p.getCartoonY() - q.getCartoonY())*(r.getCartoonY() - q.getCartoonY()) ) / (l1 * l2);
        if (l1 < -1.0) l1 = -1.0;
        if (l1 > 1.0) l1 = 1.0;
        return Math.acos(l1);
    }

    private double SQR(double x) { return x * x; }

    private double insideBarrelEnergy(Chain chain, SSE FixedStart, double Penalty) {
        Chain.Circle boundingCircle = chain.fixedBoundingCircle(FixedStart);
        double energy = 0;
        for (SSE sse : chain.getSSEs()) {
            SSE foundFixedStart = chain.findFixedStart(sse);
            if (foundFixedStart != FixedStart && sse.IsInCircle(boundingCircle)) {
                energy += Penalty;
            }
        }
        return energy;
    }

    public void optimise(Chain cartoon) {
        System.out.println("Beginning optimization of cartoon");
        for (SSE sse : cartoon.getSSEs()) {
            System.out.println(sse.getSSEType() + " " + sse.getCartoonCenter());
        }
        
        System.out.println("Temperature   LowestEnergy   Acceptance ratio (%)");

        double seed = -randomSeed; // TODO : optionally pass in this seed?
        double currentTemperature = startTemperature;

        int numberSymbols = cartoon.getSSEs().size();
        if (numberSymbols < 3) return;


        int numberFixed = cartoon.numberFixed();

        double[] X = new double[numberSymbols];
        double[] Y = new double[numberSymbols];
        double[] E = new double[numberFixed + 1];
        double[] I = new double[numberFixed + 1];

        double maxXMove = gridFix(numberSymbols * gridUnitSize / 4);
        double maxYMove = maxXMove;

        // generate starting point for optimization
        SSE root = cartoon.getSSEs().get(0);
        int i = 0;
        for (SSE sseA : cartoon.iterNext(root)) {
            if (sseA == null) break;
            double moveY = i * gridSize - sseA.getCartoonY();
            double moveX = - sseA.getCartoonX();
            sseA.setCartoonY((int) (i * gridSize));
            sseA.setCartoonX(0);

            sseA.setSymbolPlaced(true);

            for (SSE sseB : cartoon.iterFixed(sseA)) {
                if (sseB == null) break;
                sseB.setCartoonX((int)(sseB.getCartoonX() + moveX));
                sseB.setCartoonY((int)(sseB.getCartoonY() + moveY));
                sseB.setSymbolPlaced(true);
            }
            i++;
        }

        //store coords
        int j = 0;
        for (SSE sse : cartoon.getSSEs()) {
            X[j] = sse.getCartoonX();
            Y[j] = sse.getCartoonY();
            j++;
        }

        //optimize
        SSE centerFixed = largestFixed(cartoon);
        // print "CenterFixed =", CenterFixed
        int noMove = cartoon.numberLink(centerFixed);

        double lowestEnergy = calculateEnergy(cartoon);
        double startingEnergy = lowestEnergy;
        double currentEnergy = lowestEnergy;

        //increase values
        int locNoConfigs = noConfigs * numberFixed;

        while (currentTemperature > finishTemperature) {
            int numberLow = 0;
            for (int configNumber = 0; configNumber < locNoConfigs; configNumber++) {

                SSE sse = root;
                while (sse == centerFixed) {
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
                double lx = sse.getCartoonX() + rx - centerFixed.getCartoonX();
                if (Math.abs(lx) > maxXMove) {
                    if (lx < 0) {
                        rx += -lx - maxXMove;
                    } else {
                        rx += -lx + maxXMove;
                    }
                }

                double ly = sse.getCartoonY() + ry - centerFixed.getCartoonY();
                if (Math.abs(lx) > maxYMove) {
                    if (ly < 0) {
                        ry += -ly - maxYMove;
                    } else  {
                        ry += -ly + maxYMove;
                    }
                }

                //roll for number of moves
                int NumberMove;
                if (noMove == 0) {
                    NumberMove = 1;
                } else {
                    NumberMove = random.nextInt(noMove) + 1;
                }

                //replace by test values
                int l = 0;
                for (SSE sseA : cartoon.iterNext(sse)) {
                    if (sseA == centerFixed || l == NumberMove) break;
                    for (SSE sseB : cartoon.iterFixed(sseA)) {
                        sseB.setCartoonX((int) (sseB.getCartoonX() + rx));
                        sseA.setCartoonX((int) (sseA.getCartoonX() + ry));
                    }
                    l += 1;
                }

                //test step
                double newEnergy = calculateEnergy(cartoon);
                boolean accept = metropolis(newEnergy - currentEnergy, currentTemperature);
                if (accept) {
                    numberLow += 1;
                    currentEnergy = newEnergy;
                } else {
                    i = 0;
                    for (SSE sseA : cartoon.iterNext(sse)) {
                        if (sseA == centerFixed || i == NumberMove) break;
                        for (SSE sseB : cartoon.iterFixed(sseA)) {
                            sseB.setCartoonX((int) (sseB.getCartoonX() - rx));
                            sseA.setCartoonX((int) (sseA.getCartoonX() - ry));
                        }
                        i += 1;
                    }
                }

                //save if best energy
                if (currentEnergy < lowestEnergy) {
                    lowestEnergy = currentEnergy;
                    int k = 0;
                    for (SSE sseZ : cartoon.getSSEs()) {
                        X[k] = sseZ.getCartoonX();
                        Y[k] = sseZ.getCartoonY();
                        k++;
                    }
                }
            }

            double fraction = numberLow * 100 / locNoConfigs;
            System.out.println(String.format("%10f %12f %17f", currentTemperature, lowestEnergy, fraction));

            // lower the temperature
            currentTemperature = currentTemperature * (100 - decrement) / 100;
        }

        //set the coordinates to the saved best coordinates
        int z = 0;
        for (SSE sse : cartoon.getSSEs()) {
            sse.setCartoonX((int) X[z]);
            sse.setCartoonY((int) Y[z]);
            z++;
        }

        calculateEnergy(cartoon);
//        printEnergy(Energy);
    }

    private void printEnergy(long[][] EnergyComps) {
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
    
    public SSE largestFixed(Chain chain) {
        int largestSize = 0;
        SSE largest = null;
        for (SSE sse : chain.getSSEs()) {
            for (SSE fixedStart : chain.iterFixed(sse)) {
                int i = chain.fixedSize(fixedStart);
                if (i > largestSize) {
                    largestSize = i;
                    largest = sse;
                }
            }
        }
        return largest;
    }


    private Point2d generateRandomXandY(SSE sse) {
        double rx = 0;
        double ry = 0;

        double randomNumber = random.nextDouble();
        if (randomNumber <= lineSample / 100.0 && sse.hasFixed()) {
            if (sse.From != null) {
                randomNumber = random.nextDouble();
                randomNumber *= randomNumber;
                rx += randomNumber * (sse.From.getCartoonX() - sse.getCartoonX());
                ry += randomNumber * (sse.From.getCartoonY() - sse.getCartoonY());
            }
            if (sse.To != null) {
                randomNumber = random.nextDouble();
                randomNumber *= randomNumber;
                rx += randomNumber * (sse.To.getCartoonX() - sse.getCartoonX());
                ry += randomNumber * (sse.To.getCartoonY() - sse.getCartoonY());
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
            rx = gridFix(stepSize * random.nextDouble()) * direction;
            double lx = 0;
            double ly = 0;
            double rv = 0;
            if (random.nextDouble() <= arcsSample / 100.0) {
                if (random.nextDouble() < 0.5) {
                    lx = sse.To.getCartoonX() - sse.getCartoonY();
                    ly = sse.To.getCartoonY() - sse.getCartoonY();
                    rv = Math.sqrt((lx * lx) + (ly * ly));
                } else {
                    lx = sse.From.getCartoonX() - sse.getCartoonY();
                    ly = sse.From.getCartoonY() - sse.getCartoonY();
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
                ry = gridFix(stepSize * random.nextDouble()) * direction;
            }
        }
        return new Point2d(rx, ry);
    }

    private int gridFix(double x) { return (int) ( ((x + (gridSize / 2)) / gridSize) * gridSize ); }

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

}
