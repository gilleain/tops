package tops.port.calculate;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;
import tops.port.model.StringConverter;
import tops.port.model.TSE;

public class TestCalculateSheets extends TestCalculateNeighbours {
    
   
    public void calculate(Chain chain) {
        super.calculate(chain);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
    }
    
    @Test
    public void testSingleSheet() {
        String topsString = "head NEeEC 1:2A2:3A";
        Chain chain = StringConverter.convert(topsString);
        
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        
        TSE sheet1 = chain.getTSEs().get(0);
        System.out.println(sheet1);
        assertEquals(3, sheet1.size());
    }

    @Test
    public void testMultipleSheet() {
        String topsString = "head NEEEeeeC 1:4A2:5A";
        Chain chain = StringConverter.convert(topsString);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        TSE sheet1 = chain.getTSEs().get(0);
        System.out.println(sheet1);
        assertEquals(2, sheet1.size());
        
        TSE sheet2 = chain.getTSEs().get(1);
        System.out.println(sheet2);
        assertEquals(2, sheet2.size());
    }
    
    @Test
    public void testBarrel() {
        String topsString = "head NEeEeC 1:2A1:4A2:3A3:4A";
        Chain chain = StringConverter.convert(topsString);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        
        TSE barrel1 = chain.getTSEs().get(0);
        System.out.println(barrel1);
        assertEquals(4, barrel1.size());
    }
    
    @Test
    public void testSigmaBarrel() {
        String topsString = "head NeEeEeC 1:2A2:5A2:3A3:4A4:5A";
        Chain chain = StringConverter.convert(topsString);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        
        TSE barrel1 = chain.getTSEs().get(0);
        System.out.println(barrel1);
        assertEquals(4, barrel1.size());
    }
    
    @Test
    public void testDisconnectedBarrels() {
        String topsString = "head NEeEeEeEeC 1:2A1:4A2:3A3:4A5:6A5:8A6:7A7:8A";
        Chain chain = StringConverter.convert(topsString);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        
        TSE barrel1 = chain.getTSEs().get(0);
        System.out.println(barrel1);
        assertEquals(4, barrel1.size());
        
        TSE barrel2 = chain.getTSEs().get(1);
        System.out.println(barrel2);
        assertEquals(4, barrel2.size());
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
