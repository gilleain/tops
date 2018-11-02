package tops.port.calculate;

import java.io.IOException;

import org.junit.Test;

import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;

public class TestCalculateMergedStrands extends TestCalculateRelativeSides {
    
    private static final String PATH = DsspDirectory.DIR;
    
    public void calculate(Chain chain) {
        super.calculate(chain);
        CalculateMergedStrands calculation = new CalculateMergedStrands();
        calculation.calculate(chain);
    }
    
//    @Test
//    public void run1IFC() throws IOException {
//        DsspReader dsspReader = new DsspReader();
//        Protein protein = 
//                dsspReader.readDsspFile(PATH + "/1ifc.dssp");
//        Chain chain = protein.getChains().get(0);
//        calculate(chain);
//    }

}
