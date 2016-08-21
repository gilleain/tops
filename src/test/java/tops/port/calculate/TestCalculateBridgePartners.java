package tops.port.calculate;

import java.io.IOException;

import org.junit.Test;

import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;

public class TestCalculateBridgePartners {
    
    @Test
    public void test() throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile("/Users/maclean/data/dssp/reps/2igd.dssp");
        Chain chain = protein.getChains().get(0);
        CalculateBridgePartners calculate = new CalculateBridgePartners();
        calculate.bridgePartFromHBonds(chain);
    }

}
