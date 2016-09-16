package tops.port.calculate;

import static tops.port.model.SSE.SSEType.EXTENDED;

import java.io.IOException;

import org.junit.Test;

import tops.port.model.Bridge;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;
import tops.port.model.StringConverter;

public class TestCalculateSheets extends TestCalculateNeighbours {
    
   
    public void calculate(Chain chain) {
        super.calculate(chain);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
    }
    
    @Test
    public void testMultipleSheet() {
        String topsString = "head NEEeeC 1:3A2:4A";
        Chain chain = StringConverter.convert(topsString);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        System.out.println(chain.getTSEs().get(0));
        System.out.println(chain.getTSEs().get(1));
    }
    
    @Test
    public void testSingleSheet() {
        String topsString = "head NEeEC 1:2A2:3A";
        Chain chain = StringConverter.convert(topsString);
        
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        System.out.println(chain.getTSEs().get(0));
    }
    
    private Bridge makeBridge(SSE start, SSE end) {
        Bridge bridge = new Bridge();
        bridge.setStartSSE(start);
        bridge.setEndSSE(end);
        return bridge;
    }
    
    @Test
    public void test1IFC() throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile("/Users/maclean/data/dssp/reps/1ifc.dssp");
        Chain chain = protein.getChains().get(0);
        calculate(chain);
        for (SSE sse : chain.getSSEs()) {
            System.out.println(sse.getSymbolNumber() + 
                    String.format(" at (%s, %s)", sse.getCartoonX(), sse.getCartoonY()));
        }
    }

}
