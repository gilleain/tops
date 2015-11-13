package tops.view.tops2D.diagram;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tops.model.Chain;
import tops.model.HBondSet;
import tops.model.Protein;
import tops.model.SSE;

public class TestSSEDiagramBuilder {
    
    @Test
    public void buildFromProtein() {
        Protein protein = new Protein();
        Chain chain = new Chain("A");
        
        chain.addSSE(new SSE(1, SSE.Type.ALPHA_HELIX));
        chain.addSSE(new SSE(2, SSE.Type.TURN));
        chain.addSSE(new SSE(3, SSE.Type.ALPHA_HELIX));
        
        HBondSet hBondSet = new HBondSet();
        hBondSet.setStart(chain.getSSES().get(0));
        hBondSet.setEnd(chain.getSSES().get(2));
        
        chain.addHBondSet(hBondSet);
        protein.addChain(chain);
        
        SSEDiagramBuilder builder = new SSEDiagramBuilder();
        Graph graph = builder.build(protein);
        assertEquals(2, graph.numberOfVertices());
        assertEquals(1, graph.numberOfEdges());
    }
    
    @Test
    public void buildFromString() {
        SSEDiagramBuilder builder = new SSEDiagramBuilder();
        Graph graph = builder.build("NEEC", "1:2A", "");
        assertEquals(4, graph.numberOfVertices());
        assertEquals(1, graph.numberOfEdges());
    }

}
