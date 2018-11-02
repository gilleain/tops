package tops.port.calculate;

import java.io.IOException;

import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;

public class TestCalculateDirection extends TestCalculateFixedHands {
    
    private static final String PATH = DsspDirectory.DIR;
    
    public void calculate(Chain chain) {
        super.calculate(chain);
        CalculateDirection calculation = new CalculateDirection();
        calculation.calculate(chain);
    }
    
//    @Test
    public void test1GSO() throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile(PATH + "/1gsoH.dssp");
        Chain chain = protein.getChains().get(0);
        calculate(chain);
        for (SSE sse : chain.getSSEs()) {
            System.out.println(sse.getSymbolNumber() + 
                    String.format(" at (%s, %s) is %s", 
                            sse.getCartoonX(), sse.getCartoonY(), sse.getDirection()));
        }
    }

}
