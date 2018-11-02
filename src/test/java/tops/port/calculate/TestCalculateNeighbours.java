package tops.port.calculate;

import java.io.IOException;

import org.junit.Test;

import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Neighbour;
import tops.port.model.Protein;
import tops.port.model.SSE;

public class TestCalculateNeighbours extends TestCalculateMergedStrands {
    
    private static final String PATH = DsspDirectory.DIR;
    
    public void calculate(Chain chain) {
        super.calculate(chain);
        CalculateNeighbours calculation = new CalculateNeighbours();
        calculation.calculate(chain);
    }
    
//    @Test
//    public void run1IFC() throws IOException {
//        DsspReader dsspReader = new DsspReader();
//        Protein protein = 
//                dsspReader.readDsspFile(PATH + "/1ifcH.dssp");
//        Chain chain = protein.getChains().get(0);
//        calculate(chain);
//        for (SSE sse : chain.getSSEs()) {
//            for (Neighbour neighbour : sse.getNeighbours()) {
//                System.out.println(sse.getSymbolNumber() + " -> " + neighbour);
//            }
//        }
//    }

}
