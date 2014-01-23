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
			} else if (sseType.equals("H")) {
				segment = new Helix(new Residue(start, start));
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
		tops.translation.Protein p = makeNewProtein("E");
		tops.dw.protein.Protein q = ProteinConverter.convert(p);
		print(q);
	}
	
	@Test
	public void twoSSE() {
		tops.translation.Protein p = makeNewProtein("E", "H");
		tops.dw.protein.Protein q = ProteinConverter.convert(p);
		print(q);
	}
	
	@Test
	public void threeSSE() {
		tops.translation.Protein p = makeNewProtein("E", "H", "E");
		tops.dw.protein.Protein q = ProteinConverter.convert(p);
		print(q);
	}

}
