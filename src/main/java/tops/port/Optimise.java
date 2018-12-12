package tops.port;

import static tops.port.ConnectionCalculator.lineHitSymbol;
import static tops.port.IntersectionCalculator.IntersectionType.CROSSING;
import static tops.port.IntersectionCalculator.IntersectionType.SUPERIMPOSING;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Point2d;

import tops.port.IntersectionCalculator.Intersection;
import tops.port.calculate.chirality.ChiralityCalculator;
import tops.port.model.Cartoon;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.Hand;
import tops.port.model.Neighbour;
import tops.port.model.SSE;

public class Optimise {
    
    private Logger log = Logger.getLogger(Optimise.class.getName());
    
    private double parallelScale = 5.0;
    private int nEnergyComps = 9;
    
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
        double[] energyComps = new double[nEnergyComps];
        double totalEnergy = 0;

        SSE root = chain.getSSEs().get(0);

        if (clashPenalty > 0) {
            for (SSE sseA : chain.iterNext(root)) {
                for (SSE sseB : chain.iterNext(sseA)) {
                    for (SSE sseC : chain.iterFixed(sseA)) {
                        for (SSE sseD : chain.iterFixed(sseB)) {
                            if (sseC == sseD) continue;
                            double centerSeparation = distance2D(sseC, sseD);
                            double diameter = sseC.getSymbolRadius() + sseD.getSymbolRadius();
                            if (centerSeparation < diameter) {
                                double dHard = 3 * diameter / 4;
                                double clashEnergy;
                                if (centerSeparation <= dHard) {
                                    clashEnergy = clashPenalty;
                                } else {
                                    clashEnergy = clashPenalty * 
                                        Math.exp(centerSeparation - dHard / centerSeparation - diameter);
                                }
                                totalEnergy += clashEnergy;
                                energyComps[0] += clashEnergy;
                                //print TotalEnergy, d, D, DHard
                            }
                        }
                    }
                }
            }
        }
        if (chainPenalty > 0) {
            List<SSE> sses = chain.getSSEs(); 
            for (int index = 0; index < sses.size(); index++) {
                SSE sseA = sses.get(index);
                SSE sseB = sses.get(index + 1);
                if (sseB == null || (sseA.isTerminus() && sseB.isTerminus())) continue;
                double centerSeparation = distance2D(sseA, sseB);
                double diameter = sseA.getSymbolRadius() + sseB.getSymbolRadius();
                if (centerSeparation > diameter) {
                    double penalty = (centerSeparation - diameter) / gridUnitSize;
                    double cE = Math.pow(penalty , 2) * chainPenalty;
                    //print "d > D for", sseA, sseB, int(d), D, Ce
                    totalEnergy += cE;
                    energyComps[1] += cE;
                }
            }
        }

        if (neighbourPenalty > 0) {
            for (SSE sseA : chain.getSSEs()) {
                if (sseA.getNeighbours().isEmpty()) continue;
                Neighbour first = sseA.getNeighbours().get(0);   // assumes sorted...
                //NOTE: tops files do not contain neighbour distances!
                if (first.getDistance() == -1) break;	
                for (int i = 0; i < sseA.getNeighbours().size(); i++) {
                    Neighbour neighbour = sseA.getNeighbours().get(i); 
                    SSE sseB = neighbour.getSse();
                    double centerSeparation = distance2D(sseA, sseB);
                    double diameter = sseA.getSymbolRadius() + sseB.getSymbolRadius();
                    double ratio = first.getDistance() / neighbour.getDistance();
                    double clashEnergy = Math.pow((centerSeparation - diameter) / gridUnitSize, 2) 
                            * neighbourPenalty * Math.pow(ratio, 2);
                    totalEnergy += clashEnergy;
                    energyComps[2] += clashEnergy;
                }
            }
        }

        if (crossPenalty > 0) {
            List<SSE> sses = chain.getSSEs();
            for (int index = 0; index < sses.size() - 3; index++) {
                SSE sseA = sses.get(index);
                SSE nextA = sses.get(index + 1);
                if (nextA == null || (sseA.isTerminus() && nextA.isTerminus())) continue;
                for (int secondIndex = index + 2; index < sses.size(); secondIndex++) {
                    SSE sseB = sses.get(secondIndex);
                    SSE nextB = sses.get(secondIndex + 1);
                    if (nextB == null || (sseB.isTerminus() && nextB.isTerminus())) continue;
                    log.info(sseA.getCartoonCenter() + " "
                                   + nextA.getCartoonCenter() + " "
                                   + sseB.getCartoonCenter() + " "
                                   + nextB.getCartoonCenter() + " ");
                    Intersection intersection = lineCross(sseA, nextA, sseB, nextB);
                    log.log(Level.INFO, "Intersection {0}", intersection);
                    if (intersection == null) continue; // XXX FIXME
                    if (intersection.getType() == CROSSING) {
                        //print "crossing between", sseA, "-", sseA.To, "&&", sseB, "-", sseB.To
                        totalEnergy += crossPenalty;
                        energyComps[3] += crossPenalty;
                    }
                    if (intersection.getType() == SUPERIMPOSING) {
                        //print "superimposition of lines", sseA, "-", sseA.To, "&&", sseB, "-", sseB.To
                        totalEnergy += parallelScale * crossPenalty;
                        energyComps[3] += parallelScale * crossPenalty;
                    }
                }
            }
        }

        if (handPenalty > 0 && chain.numberOfSSEs() > 4) {
            for (SSE sse : chain.getSSEs()) {
                if (sse.chirality != Hand.NONE && 
                        ChiralityCalculator.hand2D(chain, sse) != sse.chirality) {
                    totalEnergy += handPenalty;
                    energyComps[4] += handPenalty;
                }
            }
        }

        if (anglePenalty > 0) {
            for (int index = 1; index < chain.numberOfSSEs() - 1; index++) {
                SSE prev = chain.getSSEs().get(index - 1);
                SSE sse = chain.getSSEs().get(index);
                SSE next = chain.getSSEs().get(index + 1);
                double angle = calculateAngle(prev, sse, next);
                double angleEnergy = anglePenalty * (1.0 - Math.abs(Math.cos(multiplicity * angle)));
                totalEnergy += angleEnergy;
                energyComps[5] += angleEnergy;
            }
        }

        if (lineHitPenalty > 0) {
            SSE prev = null;
            for (SSE sse : chain.getSSEs()) {
                if (prev == null || (prev.isTerminus() && sse.isTerminus())) continue;
                if (lineHitSymbol(chain, prev, sse) != null) {
                    // print "line hit symbol", sseA, sseB
                    totalEnergy += lineHitPenalty;
                    energyComps[6] += lineHitPenalty;
                }
            }
        }	

        if (insideBarrelPenalty > 0) {
            for (SSE sse : chain.iterFixed(chain.getSSEs().get(0))) {
                if (sse.hasFixedType(FixedType.BARREL, FixedType.CURVED_SHEET)) {
                    double barrelEnergy = insideBarrelEnergy(chain, sse, insideBarrelPenalty);
                    totalEnergy += barrelEnergy;
                    energyComps[7] += barrelEnergy;
                }
            }
        }

        energyComps[8] = totalEnergy;
        return totalEnergy;
    }


    /*
    function angle

    Tom F. August 1992

    Function to return the angle between two vectors
     */

    private double calculateAngle( SSE p, SSE q, SSE r ) {

        double l1 = Math.sqrt( square(p.getCartoonX() - q.getCartoonX()) + square( p.getCartoonY() - q.getCartoonY()) );
        double l2 = Math.sqrt( square(r.getCartoonX() - q.getCartoonX()) + square( r.getCartoonY() - q.getCartoonY()) );
        if ( l1==0.0 || l2==0.0 ){
            return 0.0;
        }
        l1 = ( (p.getCartoonX() - q.getCartoonX())*(r.getCartoonX() - q.getCartoonX()) 
                + (p.getCartoonY() - q.getCartoonY())*(r.getCartoonY() - q.getCartoonY()) ) / (l1 * l2);
        if (l1 < -1.0) l1 = -1.0;
        if (l1 > 1.0) l1 = 1.0;
        return Math.acos(l1);
    }

    private double square(double x) { return x * x; }

    private double insideBarrelEnergy(Chain chain, SSE fixedStart, double penalty) {
        Circle boundingCircle = fixedBoundingCircle(chain, fixedStart);
        double energy = 0;
        for (SSE sse : chain.getSSEs()) {
            SSE foundFixedStart = chain.findFixedStart(sse);
            if (foundFixedStart != fixedStart && isInCircle(sse, boundingCircle)) {
                energy += penalty;
            }
        }
        return energy;
    }
    

    /**
    returns center and radius
    **/
    public Circle fixedBoundingCircle(Chain chain, SSE fixedStart) {

        int n = 0;
        double centerX = 0.0;
        double centerY = 0.0;
        for (SSE p : chain.iterFixed(fixedStart)) {
            centerX += p.getCartoonX();
            centerY += p.getCartoonY();
            n +=1;
        }
        if (n > 0) {
            centerX /= (double) n;
            centerY /= (double) n;
        }

        double rim = 0.0;
        for (SSE p : chain.iterFixed(fixedStart)) {
            double x = p.getCartoonX();
            double y = p.getCartoonY();

            double separation = distance2D(x, y, centerX, centerY);
            if (separation > rim) rim = separation;
        }

        double radius = rim - (0.5 * fixedStart.getSymbolRadius());
        return new Circle(centerX, centerY, radius);
    }
    
    //
    //really an sse method
    //
    public static double distance2D(SSE a, SSE b) {
        double x1 = a.getCartoonX();
        double y1 = a.getCartoonY();
        double x2 = b.getCartoonX();
        double y2 = b.getCartoonY();
        return distance2D(x1, y1, x2, y2);
    }

    public static double distance2D(double x1, double y1, double x2, double y2) {
        double dX = x1 - x2;
        double dY = y1 - y2;
        return Math.sqrt((dX * dX) + (dY * dY));
    }
    

    public final class Circle {
        double centerX;
        double centerY;
        double radius;
        public Circle(double centerX, double centerY, double radius) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }
    }
    
    public boolean isInCircle(SSE sse, Circle circle) {
        return isInCircle(sse, circle.centerX, circle.centerY, circle.radius);
    }

    public boolean isInCircle(SSE sse, double centerX, double centerY, double radius) {
        double x = (double) sse.getCartoonX();
        double y = (double) sse.getCartoonY();

        double sep = (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY);
        return Math.sqrt(sep) <= radius;
    }

    public void optimise(Chain cartoon) {
        log.info("Beginning optimization of cartoon");
        for (SSE sse : cartoon.getSSEs()) {
            log.info(sse.getSSEType() + " " + sse.getCartoonCenter());
        }
        
        log.info("Temperature   LowestEnergy   Acceptance ratio (%)");

        double seed = -randomSeed; // TODO : optionally pass in this seed?
        double currentTemperature = startTemperature;

        int numberSymbols = cartoon.getSSEs().size();
        if (numberSymbols < 3) return;


        int numberFixed = cartoon.numberFixed();

        double[] xArr = new double[numberSymbols];
        double[] yArr = new double[numberSymbols];
        double[] eArr = new double[numberFixed + 1];
        double[] iArr = new double[numberFixed + 1];

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
            xArr[j] = sse.getCartoonX();
            yArr[j] = sse.getCartoonY();
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
                    List<SSE> sses = cartoon.getSSEs();
                    sse = sses.get(random.nextInt(sses.size()));
                }

                double rx = 0;
                double ry = 0;
                while (rx != 0 && ry != 0) {
                    SSE last = cartoon.getLast(sse);
                    SSE next = cartoon.getNext(sse);
                    Point2d p = generateRandomXandY(last, sse, next);
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
                int numberMove;
                if (noMove == 0) {
                    numberMove = 1;
                } else {
                    numberMove = random.nextInt(noMove) + 1;
                }

                //replace by test values
                int l = 0;
                for (SSE sseA : cartoon.iterNext(sse)) {
                    if (sseA == centerFixed || l == numberMove) break;
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
                        if (sseA == centerFixed || i == numberMove) break;
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
                        xArr[k] = sseZ.getCartoonX();
                        yArr[k] = sseZ.getCartoonY();
                        k++;
                    }
                }
            }

            double fraction = numberLow * 100 / (double) locNoConfigs;
            log.log(Level.INFO, String.format("%10f %12f %17f", currentTemperature, lowestEnergy, fraction));

            // lower the temperature
            currentTemperature = currentTemperature * (100 - decrement) / 100;
        }

        //set the coordinates to the saved best coordinates
        int z = 0;
        for (SSE sse : cartoon.getSSEs()) {
            sse.setCartoonX((int) xArr[z]);
            sse.setCartoonY((int) yArr[z]);
            z++;
        }

        calculateEnergy(cartoon);
        printEnergy(Energy);
    }

    private void printEnergy(long[][] energyComps) {
        log.info(String.format("Clash energy            %ld" , energyComps[0]));
        log.info(String.format("Chain energy            %ld" , energyComps[1]));
        log.info(String.format("Neighbour energy        %ld" , energyComps[2]));
        log.info(String.format("LineCross energy        %ld" , energyComps[3]));
        log.info(String.format("Chirality energy        %ld" , energyComps[4]));
        log.info(String.format("Angle energy            %ld" , energyComps[5]));
        log.info(String.format("Line hit energy         %ld" , energyComps[6]));
        log.info(String.format("Inside barrel energy    %ld" , energyComps[7]));
        log.info(String.format("Total Energy            %ld" , energyComps[8]));
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


    private Point2d generateRandomXandY(SSE prev, SSE sse, SSE next) {
        double rx = 0;
        double ry = 0;

        double randomNumber = random.nextDouble();
        if (randomNumber <= lineSample / 100.0 && sse.hasFixed()) {
            if (prev != null) {
                randomNumber = random.nextDouble();
                randomNumber *= randomNumber;
                rx += randomNumber * (prev.getCartoonX() - sse.getCartoonX());
                ry += randomNumber * (prev.getCartoonY() - sse.getCartoonY());
            }
            if (next != null) {
                randomNumber = random.nextDouble();
                randomNumber *= randomNumber;
                rx += randomNumber * (next.getCartoonX() - sse.getCartoonX());
                ry += randomNumber * (next.getCartoonY() - sse.getCartoonY());
            }
            rx = rx == 0 ? 0 : gridFix(rx);
            ry = ry == 0 ? 0 : gridFix(ry);
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
                    lx = next.getCartoonX() - (double) sse.getCartoonY();
                    ly = next.getCartoonY() - (double) sse.getCartoonY();
                    rv = Math.sqrt((lx * lx) + (ly * ly));
                } else {
                    lx = prev.getCartoonX() - (double) sse.getCartoonY();
                    ly = prev.getCartoonY() - (double) sse.getCartoonY();
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

    private boolean metropolis(double energy, double temperature) {
        if (energy < 0) {
            return true;
        } else if (temperature > 0) {
            double exponent = -(energy * 100.0) / temperature;
            return random.nextDouble() < Math.exp(exponent);
        } else {
            return false;
        }
    }

}
