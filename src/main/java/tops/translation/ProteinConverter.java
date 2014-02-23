package tops.translation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tops.dw.protein.CATHcode;
import tops.dw.protein.DomainDefinition;
import tops.dw.protein.SecStrucElement;

/**
 * Temporary converter from tops.translation.Protein objects into tops.dw.protein.Protein objects.
 * 
 * @author maclean
 *
 */
public class ProteinConverter {

	public static tops.dw.protein.Protein convert(Protein newProtein) {
		tops.dw.protein.Protein oldProtein = new tops.dw.protein.Protein();
		oldProtein.setName(newProtein.getID());
		for (Chain chain : newProtein) {
			SecStrucElement s = toSSE(chain);
			char chainLabel = chain.getCathCompatibleLabel().charAt(0);
			DomainDefinition d = new DomainDefinition(new CATHcode(newProtein.getID() + String.valueOf(chainLabel)));
			oldProtein.AddTopsLinkedList(s, d);
		}
		
		return oldProtein;
	}
	
	private static SecStrucElement toSSE(Chain chain) {
		SecStrucElement s = null;
		SecStrucElement head = null;
		SecStrucElement prev = null;
		int i = 1;
		Map<BackboneSegment, SecStrucElement> segmentElementMap = new HashMap<BackboneSegment, SecStrucElement>();
		for (BackboneSegment backboneSegment : chain) {
			s = new SecStrucElement();
			segmentElementMap.put(backboneSegment, s);
			if (head == null) {
				head = s;
			}
			if (backboneSegment instanceof Strand) {
				s.Type = "E";
			} else if (backboneSegment instanceof Helix) {
				s.Type = "H";
			} else if (backboneSegment instanceof Terminus) {
				s.Type = String.valueOf(((Terminus) backboneSegment).getTypeChar());
			}
			if (backboneSegment.getOrientation().equals("UP")) {
				s.Direction = "UP";
			} else {
				s.Direction = "DOWN";
			}
			s.PDBStartResidue = backboneSegment.firstPDB();
			s.PDBFinishResidue = backboneSegment.lastPDB();
			s.SymbolNumber = i;
			i++;
			s.SetFrom(prev);
			if (prev != null) {
				prev.SetTo(s);
			}
			prev = s;
		}
		Iterator<Sheet> sheetIterator = chain.sheetIterator(); 
		while (sheetIterator.hasNext()) {
			Sheet sheet = sheetIterator.next();
			BackboneSegment prevStrand = null;
			for (BackboneSegment strand : sheet) {
				if (prevStrand != null) {
					SecStrucElement prevElement = segmentElementMap.get(prevStrand);
					SecStrucElement currElement = segmentElementMap.get(strand);
					prevElement.AddBridgePartner(currElement.SymbolNumber);
					currElement.AddBridgePartner(prevElement.SymbolNumber);
					prevElement.AddBridgePartnerSide("R"); // XXX
					currElement.AddBridgePartnerSide("L"); // XXX
					char relativeOrientation = prevStrand.getRelativeOrientation(strand);
					prevElement.AddBridgePartnerType(String.valueOf(relativeOrientation));
					currElement.AddBridgePartnerType(String.valueOf(relativeOrientation));
				}
				prevStrand = strand;
			}
		}
		return head;
	}
}

