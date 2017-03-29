package tops.dw.io;

import java.awt.Color;

import org.junit.Test;

import tops.dw.protein.CATHcode;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.port.model.DomainDefinition;
import tops.port.model.DomainDefinition.DomainType;

public class TestTopsFileWriter {

    
    @Test
    public void write() {
        TopsFileWriter topsFileWriter = new TopsFileWriter();
        
        Protein protein = new Protein();
        protein.setName("2BOP");
        CATHcode c = new CATHcode("2bopA0");
        DomainDefinition dd = new DomainDefinition(c, DomainType.CHAIN_SET);
        dd.addSegment(c.getChain(), 10, 100);
        SecStrucElement root = make("A", Color.BLACK, "UP", "N1", 10, 11, 1, "N");
        SecStrucElement e1 = make("A", Color.RED, "UP", "E1", 12, 16, 2, "E");
        SecStrucElement h2 = make("A", Color.BLACK, "DOWN", "H2", 17, 25, 3, "H");
        SecStrucElement e3 = make("A", Color.BLACK, "UP", "E3", 26, 29, 4, "E");
        SecStrucElement c4 = make("A", Color.BLACK, "UP", "C4", 30, 31, 5, "C");
        root.SetTo(e1);
        e1.SetFrom(root);
        e1.SetTo(h2);
        h2.SetFrom(e1);
        h2.SetTo(e3);
        e3.SetFrom(h2);
        e3.SetTo(c4);
        c4.SetFrom(e3);
        
        protein.addTopsLinkedList(new Cartoon(root), dd);
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
        SecStrucElement root = make("A", Color.BLACK, "UP", "N1", 10, 11, 1, "N");
        SecStrucElement e1 = make("A", Color.RED, "UP", "E1", 12, 16, 2, "E");
        SecStrucElement h2 = make("A", Color.BLACK, "DOWN", "H2", 17, 25, 3, "H");
        SecStrucElement e3 = make("A", Color.BLACK, "UP", "E3", 26, 29, 4, "E");
        SecStrucElement c4 = make("A", Color.BLACK, "UP", "C4", 30, 31, 5, "C");
        root.SetTo(e1);
        e1.SetFrom(root);
        e1.SetTo(h2);
        h2.SetFrom(e1);
        h2.SetTo(e3);
        e3.SetFrom(h2);
        e3.SetTo(c4);
        c4.SetFrom(e3);
        
        protein.addTopsLinkedList(new Cartoon(root), dd);
        
        CATHcode c2 = new CATHcode("2bopB0");
        DomainDefinition dd2 = new DomainDefinition(c2, DomainType.CHAIN_SET);
        dd2.addSegment(c2.getChain(), 110, 200);
        SecStrucElement root2 = make("B", Color.BLACK, "UP", "N5", 10, 11, 1, "N");
        SecStrucElement e6 = make("B", Color.RED, "UP", "E6", 12, 16, 2, "E");
        SecStrucElement h7 = make("B", Color.BLACK, "DOWN", "H7", 17, 25, 3, "H");
        SecStrucElement e8 = make("B", Color.BLACK, "UP", "E8", 26, 29, 4, "E");
        SecStrucElement c9 = make("B", Color.BLACK, "UP", "C9", 30, 31, 5, "C");
        root2.SetTo(e6);
        e6.SetFrom(root2);
        e6.SetTo(h7);
        h7.SetFrom(e6);
        h7.SetTo(e8);
        e8.SetFrom(h7);
        e8.SetTo(c9);
        c9.SetFrom(e8);
        
        protein.addTopsLinkedList(new Cartoon(root2), dd2);
        
        topsFileWriter.writeTopsFile(protein, System.out);
    }
    
    public SecStrucElement make(String chain, Color color, String direction,
                                String label, int pdbStart, int pdbEnd, int symbolNumber, String type) {
        SecStrucElement root = new SecStrucElement();
        root.setChain(chain);
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
