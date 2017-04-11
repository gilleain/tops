package tops.port.calculate;

import java.io.IOException;

import org.junit.Test;

import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;

public class TestCalculateFixedHands extends TestCalculateSandwiches {
    
    public void calculate(Chain chain) {
        super.calculate(chain);
        CalculateFixedHands calculation = new CalculateFixedHands();
        calculation.calculate(chain);
    }
    
//    @Test
    public void test1GSO() throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile("/Users/maclean/data/dssp/reps/1gso.dssp");
        Chain chain = protein.getChains().get(0);
        calculate(chain);
        for (SSE sse : chain.getSSEs()) {
            System.out.println(sse.getSymbolNumber() + 
                    String.format(" at (%s, %s) is %s", 
                            sse.getCartoonX(), sse.getCartoonY(), sse.getFixedType()));
        }
    }
    
//    @Test
//    public void run1IFC() throws IOException {
//        DsspReader dsspReader = new DsspReader();
//        Protein protein = 
//                dsspReader.readDsspFile("/Users/maclean/data/dssp/reps/1ifc.dssp");
//        Chain chain = protein.getChains().get(0);
//        calculate(chain);
//        for (SSE sse : chain.getSSEs()) {
//            System.out.println( 
//                    String.format("%s%s at (%s, %s) is %s", 
//                            sse.getSymbolNumber(), sse.getSSEType(), 
//                            sse.getCartoonX(), sse.getCartoonY(), sse.getFixedType()));
//        }
//    }

}
