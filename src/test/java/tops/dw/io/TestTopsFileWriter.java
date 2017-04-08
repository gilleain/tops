package tops.dw.io;

import static tops.port.model.Direction.DOWN;
import static tops.port.model.Direction.UP;
import static tops.port.model.SSEType.CTERMINUS;
import static tops.port.model.SSEType.EXTENDED;
import static tops.port.model.SSEType.HELIX;
import static tops.port.model.SSEType.NTERMINUS;

import java.awt.Color;

import org.junit.Test;

import tops.dw.protein.CATHcode;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.port.model.Direction;
import tops.port.model.DomainDefinition;
import tops.port.model.DomainDefinition.DomainType;
import tops.port.model.SSEType;

public class TestTopsFileWriter {

    
    @Test
    public void write() {
        TopsFileWriter topsFileWriter = new TopsFileWriter();
        
        Protein protein = new Protein();
        protein.setName("2BOP");
        CATHcode c = new CATHcode("2bopA0");
        DomainDefinition dd = new DomainDefinition(c, DomainType.CHAIN_SET);
        dd.addSegment(c.getChain(), 10, 100);
        SecStrucElement root = make("A", Color.BLACK, UP, "N1", 10, 11, 1, NTERMINUS);
        SecStrucElement e1 = make("A", Color.RED, UP, "E1", 12, 16, 2, EXTENDED);
        SecStrucElement h2 = make("A", Color.BLACK, DOWN, "H2", 17, 25, 3, HELIX);
        SecStrucElement e3 = make("A", Color.BLACK, UP, "E3", 26, 29, 4, EXTENDED);
        SecStrucElement c4 = make("A", Color.BLACK, UP, "C4", 30, 31, 5, CTERMINUS);
        
        protein.addTopsLinkedList(new Cartoon(root, e1, h2, e3, c4), dd);
        topsFileWriter.writeTopsFile(protein, System.out);
    }
    
    @Test
    public void writeMultiple() {
        TopsFileWriter topsFileWriter = new TopsFileWriter();
        
        Protein protein = new Protein();
        protein.setName("2BOP");
        CATHcode c = new CATHcode("2bopA0");
        DomainDefinition dd = new DomainDefinition(c, DomainType.CHAIN_SET);
        dd.addSegment(c.getChain(), 10, 100);
        SecStrucElement root = make("A", Color.BLACK, UP, "N1", 10, 11, 1, NTERMINUS);
        SecStrucElement e1 = make("A", Color.RED, UP, "E1", 12, 16, 2, EXTENDED);
        SecStrucElement h2 = make("A", Color.BLACK, DOWN, "H2", 17, 25, 3, HELIX);
        SecStrucElement e3 = make("A", Color.BLACK, UP, "E3", 26, 29, 4, EXTENDED);
        SecStrucElement c4 = make("A", Color.BLACK, UP, "C4", 30, 31, 5, CTERMINUS);
        
        protein.addTopsLinkedList(new Cartoon(root, e1, h2, e3, c4), dd);
        
        CATHcode c2 = new CATHcode("2bopB0");
        DomainDefinition dd2 = new DomainDefinition(c2, DomainType.CHAIN_SET);
        dd2.addSegment(c2.getChain(), 110, 200);
        SecStrucElement root2 = make("B", Color.BLACK, UP, "N5", 10, 11, 1, NTERMINUS);
        SecStrucElement e6 = make("B", Color.RED, UP, "E6", 12, 16, 2, EXTENDED);
        SecStrucElement h7 = make("B", Color.BLACK, DOWN, "H7", 17, 25, 3, HELIX);
        SecStrucElement e8 = make("B", Color.BLACK, UP, "E8", 26, 29, 4, EXTENDED);
        SecStrucElement c9 = make("B", Color.BLACK, UP, "C9", 30, 31, 5, CTERMINUS);
        
        protein.addTopsLinkedList(new Cartoon(root2, e6, h7, e8, c9), dd2);
        
        topsFileWriter.writeTopsFile(protein, System.out);
    }
    
    public SecStrucElement make(String chain, Color color, Direction direction,
                                String label, int pdbStart, int pdbEnd, int symbolNumber,
                                SSEType type) {
        SecStrucElement root = new SecStrucElement();
        root.setColour(color);
        root.setDirection(direction);
        root.setLabel(label);
        root.setPDBStartResidue(pdbStart);
        root.setPDBFinishResidue(pdbEnd);
        root.setSymbolNumber(symbolNumber);
        root.setType(type);
        return root;
    }
}
