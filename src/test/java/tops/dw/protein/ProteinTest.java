package tops.dw.protein;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

public class ProteinTest {
	
	@Test
	public void read() throws FileNotFoundException, TopsFileFormatException, IOException {
		String filename ="/2bopA.tops";
		InputStream in = ProteinTest.class.getResourceAsStream(filename);
		Assert.assertNotNull(in);
		Protein protein = new Protein(in);
		Assert.assertNotNull(protein);
	}
	
	@Test
	public void write() {
		Protein protein = new Protein();
		protein.setName("2BOP");
		CATHcode c = new CATHcode("2bopA0");
		DomainDefinition dd = new DomainDefinition(c);
		dd.addSequenceFragment(new IntegerInterval(10, 100), 5);
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
		
		protein.AddTopsLinkedList(root, dd);
		protein.WriteTopsFile(System.out);
	}
	
	@Test
	public void writeMultiple() {
		Protein protein = new Protein();
		protein.setName("2BOP");
		CATHcode c = new CATHcode("2bopA0");
		DomainDefinition dd = new DomainDefinition(c);
		dd.addSequenceFragment(new IntegerInterval(10, 100), 5);
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
		
		protein.AddTopsLinkedList(root, dd);
		
		CATHcode c2 = new CATHcode("2bopB0");
		DomainDefinition dd2 = new DomainDefinition(c2);
		dd2.addSequenceFragment(new IntegerInterval(110, 200), 5);
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
		
		protein.AddTopsLinkedList(root2, dd2);
		
		protein.WriteTopsFile(System.out);
	}
	
	public SecStrucElement make(String chain, Color color, String direction,
								String label, int pdbStart, int pdbEnd, int symbolNumber, String type) {
		SecStrucElement root = new SecStrucElement();
		root.Chain = chain;
		root.Colour = color;
		root.Direction = direction;
		root.Label = label;
		root.PDBStartResidue = pdbStart;
		root.PDBFinishResidue = pdbEnd;
		root.SymbolNumber = symbolNumber;
		root.Type = type;
		return root;
	}

}
