package tops.port.calculate;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import tops.port.model.Bridge;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;

public class TestCalculateBridgePartners extends TestCalculateMergedStrands {
    
    private static final String PATH = "/Users/maclean/data/dssp/reps/";
    
    public void calculate(Chain chain) {
        super.calculate(chain);
        CalculateBridgePartners calculation = new CalculateBridgePartners();
        calculation.calculate(chain);
    }
    
    @Test
    public void test2IGD() throws IOException {
        test("2igd");
    }
    
    @Test
    public void test2ENG() throws IOException {
        test("2eng");
    }
    
    @Test
    public void test1TGX() throws IOException {
        test("1tgx");
    }
    
    @Test
    public void test1ABA() throws IOException {
        test("1aba");
    }
    
    @Test
    public void test1AKR() throws IOException {
        test("1akr");
    }
    
    @Test
    public void test1BFD() throws IOException {
        test("1bfd");
    }
      
    
    private void test(String id) throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile(new File(PATH, id + ".dssp").toString());
        Chain chain = protein.getChains().get(0);
        CalculateBridgePartners calculate = new CalculateBridgePartners();
        calculate.calculate(chain);
        
        for (SSE sse : chain.getSSEs()) {
            System.out.println(sse.getSummary());
        }
        
        for (Bridge bridge : chain.getBridges()) {
            System.out.println(String.format("BRIDGE between %s and %s", 
                    bridge.getSseStart().getSymbolNumber(), bridge.getSseEnd().getSymbolNumber()));
            System.out.println(bridge);
        }
        
        System.out.println(id + chain.toString());
    }

}
