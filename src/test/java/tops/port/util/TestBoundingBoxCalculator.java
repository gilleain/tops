package tops.port.util;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import tops.port.calculate.Configure;
import tops.port.calculate.util.BoundingBoxCalculator;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.tse.BaseTSE;
import tops.port.model.tse.Sheet;

public class TestBoundingBoxCalculator {
    
    private static final String DIR = "/Users/maclean/data/dssp/reps";
    
    private Protein get(String id) throws IOException {
        File dirFile = new File(DIR);
        DsspReader dsspReader = new DsspReader();
        File file = new File(dirFile, String.format("%s.dssp", id));
        return dsspReader.readDsspFile(file.getAbsolutePath());
    }
    
    @Test
    public void testSingleSheet() throws IOException {
        BoundingBoxCalculator bbCalc = new BoundingBoxCalculator();
        Protein protein = get("1aba");
        Chain chain = protein.getChains().get(0);
        Configure configure = new Configure();
        configure.configure(chain);
        for (BaseTSE tse : chain.getTSEs()) {
            if (tse instanceof Sheet) {
                Sheet sheet = (Sheet) tse;
                BoundingBoxCalculator.BoundingBox bb = bbCalc.calculate(chain, sheet);
                bb.hull.print(System.out);
            }
        }
    }

}
