package tops.port.calculate;

import java.io.IOException;

import org.junit.Test;

import tops.port.model.BridgePartner;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;

public class TestCalculateRelativeSides {
    
    @Test
    public void test1IFC() throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile("/Users/maclean/data/dssp/reps/1ifc.dssp");
        Chain chain = protein.getChains().get(0);
        CalculateStructureAxes calculationA = new CalculateStructureAxes();
        calculationA.calculate(chain);
        CalculateRelativeSides calculationB = new CalculateRelativeSides();
        calculationB.calculate(chain);
        for (SSE sse : chain.getSSEs()) {
            for (BridgePartner bp : sse.getBridgePartners()) {
                System.out.println(sse.getSymbolNumber() + " ->" + bp);
            }
        }
    }

}
