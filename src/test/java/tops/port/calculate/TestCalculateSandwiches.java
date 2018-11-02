package tops.port.calculate;

import java.io.IOException;

import org.junit.Test;

import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.tse.BaseTSE;

public class TestCalculateSandwiches extends TestCalculateSheets {
    
    private static final String PATH = DsspDirectory.DIR;
    
    public void calculate(Chain chain) {
        super.calculate(chain);
        CalculateSandwiches calculation = new CalculateSandwiches();
        calculation.calculate(chain);
    }
    
    private void test(String pdbCode) throws IOException {
        DsspReader dsspReader = new DsspReader();
        String path = String.format(PATH + "/%s.dssp", pdbCode);
        Protein protein = dsspReader.readDsspFile(path);
        Chain chain = protein.getChains().get(0);
        calculate(chain);
        for (BaseTSE tse : chain.getTSEs()) {
            System.out.println(tse.getClass().getSimpleName());
        }
    }
    
//    @Test
    public void run1DLF() throws IOException {
      test("1dlfH");
    }
    
//    @Test
    public void run1AAC() throws IOException {
      test("1aacH");
    }

}
