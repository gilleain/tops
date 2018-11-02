package tops.port.calculate;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;
import tops.port.model.StringConverter;
import tops.port.model.tse.Barrel;
import tops.port.model.tse.BaseTSE;
import tops.port.model.tse.Sheet;

public class TestCalculateSheets extends TestCalculateBridgePartners {
    
    private static final String PATH = DsspDirectory.DIR;
    
   
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
        
        BaseTSE sheet1 = chain.getTSEs().get(0);
        System.out.println(sheet1);
        assertThat(sheet1, instanceOf(Sheet.class));
        assertEquals(3, sheet1.size());
    }

    @Test
    public void testMultipleSheet() {
        String topsString = "head NEEEeeeC 1:4A2:5A";
        Chain chain = StringConverter.convert(topsString);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        BaseTSE sheet1 = chain.getTSEs().get(0);
        System.out.println(sheet1);
        assertThat(sheet1, instanceOf(Sheet.class));
        assertEquals(2, sheet1.size());
        
        BaseTSE sheet2 = chain.getTSEs().get(1);
        System.out.println(sheet2);
        assertThat(sheet2, instanceOf(Sheet.class));
        assertEquals(2, sheet2.size());
    }
    
    @Test
    public void testBarrel() {
        String topsString = "head NEeEeC 1:2A1:4A2:3A3:4A";
        Chain chain = StringConverter.convert(topsString);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        
        BaseTSE barrel1 = chain.getTSEs().get(0);
        System.out.println(barrel1);
        assertThat(barrel1, instanceOf(Barrel.class));
        assertEquals(4, barrel1.size());
    }
    
    @Test
    public void testSigmaBarrel() {
        String topsString = "head NeEeEeC 1:2A2:3A2:5A3:4A4:5A";
        Chain chain = StringConverter.convert(topsString);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        
        BaseTSE barrel1 = chain.getTSEs().get(0);
        System.out.println(barrel1);
        assertEquals(5, barrel1.size());
    }
    
    @Test
    public void testDisconnectedBarrels() {
        String topsString = "head NEeEeEeEeC 1:2A1:4A2:3A3:4A5:6A5:8A6:7A7:8A";
        Chain chain = StringConverter.convert(topsString);
        CalculateSheets calculation = new CalculateSheets();
        calculation.calculate(chain);
        
        BaseTSE barrel1 = chain.getTSEs().get(0);
        System.out.println(barrel1);
        assertEquals(4, barrel1.size());
        
        BaseTSE barrel2 = chain.getTSEs().get(1);
        System.out.println(barrel2);
        assertEquals(4, barrel2.size());
    }
    
//    @Test
//    public void run1IFC() throws IOException {
//        DsspReader dsspReader = new DsspReader();
//        Protein protein = 
//                dsspReader.readDsspFile(PATH + "/1ifcH.dssp");
//        Chain chain = protein.getChains().get(0);
//        calculate(chain);
//        for (SSE sse : chain.getSSEs()) {
//            System.out.println(sse.getSymbolNumber() + 
//                    String.format(" at (%s, %s)", sse.getCartoonX(), sse.getCartoonY()));
//        }
//    }

}
