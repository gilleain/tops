package tops.translation;

import java.io.PrintWriter;

import org.junit.Test;

import tops.dw.protein.SecStrucElement;

public class ProteinConverterTest {
	
	public tops.translation.Protein makeNewProtein(String... sseTypes) {
		tops.translation.Protein p = new tops.translation.Protein();
		Chain chain = new Chain("A");
		int start = 1;
		for (String sseType : sseTypes) {
			BackboneSegment segment = null;
			if (sseType.equals("E")) {
				segment = new Strand(new Residue(start, start));
				segment.setOrientation("UP");
			} else if (sseType.equals("e")) {
				segment = new Strand(new Residue(start, start));
				segment.setOrientation("DOWN");
			} else if (sseType.equals("H")) {
				segment = new Helix(new Residue(start, start));
				segment.setOrientation("UP");
			} else if (sseType.equals("h")) {
				segment = new Helix(new Residue(start, start));
				segment.setOrientation("DOWN");
			} else if (sseType.equals("N")) {
				segment = new Terminus("N", 'N');
			} else if (sseType.equals("C")) {
				segment = new Terminus("C", 'C');
			}
			if (segment != null) {
				segment.expandBy(new Residue(start + 1, start + 1));
				chain.addBackboneSegment(segment);
			}
			start += 2;
		}
		p.addChain(chain);
		return p;
	}
	
	public void print(tops.dw.protein.Protein protein) {
		PrintWriter writer = new PrintWriter(System.out);
		for (int i = 0; i < protein.NumberDomains(); i++) {
			SecStrucElement root = protein.getDomain(i);
			SecStrucElement current = root;
			while (current != null) {
				current.PrintAsText(writer);
				writer.println();
				writer.flush();
				current = current.GetTo();
			}
		}
	}
	
	@Test
	public void singleSSE() {
		tops.translation.Protein p = makeNewProtein("N", "E", "C");
		tops.dw.protein.Protein q = ProteinConverter.convert(p);
		print(q);
	}
	
	@Test
	public void twoSSE() {
		tops.translation.Protein p = makeNewProtein("N", "E", "H", "C");
		tops.dw.protein.Protein q = ProteinConverter.convert(p);
		print(q);
	}
	
	@Test
	public void threeSSE() {
		tops.translation.Protein p = makeNewProtein("N", "E", "H", "e", "C");
		tops.dw.protein.Protein q = ProteinConverter.convert(p);
		print(q);
	}

}
