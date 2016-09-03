package tops.port.calculate;

import java.io.IOException;

import javax.vecmath.Point3d;

import org.junit.Test;

import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;
import tops.port.model.SSE.SSEType;

public class TestCalculateStructureAxes {
    
    public void calculate(Chain chain) {
        CalculateStructureAxes calculation = new CalculateStructureAxes();
        calculation.calculate(chain);
    }
    
    private Chain makeChain() {
        Chain chain = new Chain('A');
        // Helix 20-30 from 1ecoA
        chain.addCACoord(new Point3d(7.8, 10.4, 14.5));
        chain.addCACoord(new Point3d(10.3, 12.7, 12.7));
        chain.addCACoord(new Point3d(7.6, 14.4, 10.7));
        chain.addCACoord(new Point3d(5.5, 15.0, 13.8));
        chain.addCACoord(new Point3d(8.3, 16.5, 15.8));
        chain.addCACoord(new Point3d(9.3, 18.7, 12.8));
        chain.addCACoord(new Point3d(5.8, 20.1, 12.7));
        chain.addCACoord(new Point3d(5.9, 20.9, 16.4));
        chain.addCACoord(new Point3d(9.3, 22.6, 16.2));
        chain.addCACoord(new Point3d(8.2, 24.6, 13.1));
        chain.addCACoord(new Point3d(5.1, 25.8, 14.9));
        SSE sse = new SSE(SSEType.HELIX);
        sse.sseData.SeqStartResidue = 1;
        sse.sseData.SeqFinishResidue = 10;
        chain.addSSE(sse);
        return chain;
    }
    
    @Test
    public void test1IFC() throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile("/Users/maclean/data/dssp/reps/1ifc.dssp");
        Chain chain = protein.getChains().get(0);
        calculate(chain);
        for (SSE sse : chain.getSSEs()) {
            System.out.println("[" + 
                    sse.sseData.PDBStartResidue + "-" + sse.sseData.PDBFinishResidue
                    + " " + sse.axis);
        }
    }
    
    @Test
    public void testSingleHelix() {
        CalculateStructureAxes calculation = new CalculateStructureAxes();
        Chain chain = makeChain();
        calculation.calculate(chain);
        System.out.println(chain.getSSEs().get(0).axis);
    }

}
