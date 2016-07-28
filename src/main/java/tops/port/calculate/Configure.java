package tops.port.calculate;

import java.util.ArrayList;
import java.util.List;

import tops.port.model.Chain;

/**
 * TODO : rename class!!
 * 
 * @author maclean
 *
 */
public class Configure {

    private int neighbourCutoffDistance = 10;
    private double gridUnitSize = 50;   
    private List<Calculation> calculators;
    
    public Configure() {
        calculators = new ArrayList<Calculation>();
        calculators.add(new CalculateStructureAxes());
        calculators.add(new CalculateRelativeSides());
        calculators.add(new CalculateMergedStrands());
        calculators.add(new CalculateNeighbours());
        calculators.add(new CalculateSheets());
        calculators.add(new CalculateSandwiches());
        calculators.add(new CalculateFixedHands());
        calculators.add(new CalculateDirection());
        calculators.add(new CalculateHands());
        
        for (Calculation calculator : calculators) {
            calculator.setParameter("neighbourCutoffDistance", neighbourCutoffDistance);
            calculator.setParameter("gridUnitSize", gridUnitSize);
        }
    }

    public void configure(Chain chain) {
        System.out.println("Beginning to configure the master linked list");
        for (Calculation calculator : calculators) {
            calculator.calculate(chain);
        }
        chain.clearPlaced();
    }

}
