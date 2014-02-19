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
