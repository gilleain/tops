package tops.translation;

import org.junit.Test;

import tops.dw.io.TopsFileWriter;
import tops.translation.model.BackboneSegment;
import tops.translation.model.Chain;
import tops.translation.model.Helix;
import tops.translation.model.Residue;
import tops.translation.model.Strand;
import tops.translation.model.Terminus;

public class ProteinConverterTest {
	
	public tops.translation.model.Protein makeNewProtein(String... sseTypes) {
		tops.translation.model.Protein p = new tops.translation.model.Protein();
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
	
	@Test
	public void singleSSE() {
	    TopsFileWriter topsFileWriter = new TopsFileWriter();
		tops.translation.model.Protein p = makeNewProtein("N", "E", "C");
		tops.dw.protein.Protein q = ProteinConverter.convert(p);
		topsFileWriter.writeTopsFile(q, System.out);
	}
	
	@Test
	public void twoSSE() {
	    TopsFileWriter topsFileWriter = new TopsFileWriter();
		tops.translation.model.Protein p = makeNewProtein("N", "E", "H", "C");
		tops.dw.protein.Protein q = ProteinConverter.convert(p);
		topsFileWriter.writeTopsFile(q, System.out);
	}
	
	@Test
	public void threeSSE() {
	    TopsFileWriter topsFileWriter = new TopsFileWriter();
		tops.translation.model.Protein p = makeNewProtein("N", "E", "H", "e", "C");
		tops.dw.protein.Protein q = ProteinConverter.convert(p);
		topsFileWriter.writeTopsFile(q, System.out);
	}

}
