package tops.port.calculate.chirality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import tops.port.calculate.CalculateStructureAxes;
import tops.port.calculate.Configure;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Hand;
import tops.port.model.Protein;
import tops.port.model.SSE;

public class TestSimpleChirality {
    
    private void testChiral(String pdbid, int pIndex, int qIndex) throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile(
                        String.format(
                                "/Users/maclean/data/dssp/reps/%s.dssp", pdbid));
        Chain chain = protein.getChains().get(0);
        Configure configure = new Configure();
        configure.configure(chain);
        for (SSE sse : chain.getSSEs()) {
            System.out.println(sse.getSymbolNumber() +
                    " " + sse.getSSEType() + " " 
                    + (sse.axis == null? "null" : sse.axis.getCentroid()));
        }
        SimpleChirality chirality = new SimpleChirality();
        SSE p = chain.getSSEs().get(pIndex);
        SSE q = chain.getSSEs().get(qIndex);
        Hand chiral = chirality.chiral3d(chain, p, q);
        assertEquals(Hand.RIGHT, chiral);
        System.out.println(chain.toString());
    }
    
    @Test
    public void testChiral3d_1aba() throws IOException {
        testChiral("1aba", 1, 3);
    }
    
    @Test
    public void testChiral3d_2igd() throws IOException {
        testChiral("2igd", 2, 4);
    }

}
