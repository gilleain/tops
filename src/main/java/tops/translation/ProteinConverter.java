package tops.translation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tops.dw.protein.CATHcode;
import tops.dw.protein.Cartoon;
import tops.dw.protein.SecStrucElement;
import tops.port.model.DomainDefinition;
import tops.port.model.DomainDefinition.DomainType;
import tops.translation.model.BackboneSegment;
import tops.translation.model.Chain;
import tops.translation.model.Helix;
import tops.translation.model.Protein;
import tops.translation.model.Sheet;
import tops.translation.model.Strand;
import tops.translation.model.Terminus;

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
		    Cartoon s = toCartoon(chain);
			char chainLabel = chain.getCathCompatibleLabel().charAt(0);
			DomainDefinition d = new DomainDefinition(
			        new CATHcode(newProtein.getID() + String.valueOf(chainLabel)), DomainType.CHAIN_SET);
			oldProtein.addTopsLinkedList(s, d);
		}
		
		return oldProtein;
	}
	
	private static Cartoon toCartoon(Chain chain) {
	    Cartoon cartoon = new Cartoon();
		int i = 1;
		Map<BackboneSegment, SecStrucElement> segmentElementMap = new HashMap<BackboneSegment, SecStrucElement>();
		for (BackboneSegment backboneSegment : chain) {
		    SecStrucElement s = new SecStrucElement();
		    cartoon.addSSE(s);
			segmentElementMap.put(backboneSegment, s);
			
			if (backboneSegment instanceof Strand) {
				s.setType("E");
			} else if (backboneSegment instanceof Helix) {
				s.setType("H");
			} else if (backboneSegment instanceof Terminus) {
				s.setType(String.valueOf(((Terminus) backboneSegment).getTypeChar()));
			}
			if (backboneSegment.getOrientation().equals("UP")) {
				s.setDirection("UP");
			} else {
				s.setDirection("DOWN");
			}
			s.setPDBStartResidue(backboneSegment.firstPDB());
			s.setPDBFinishResidue(backboneSegment.lastPDB());
			s.setSymbolNumber(i);
			i++;
		}
		Iterator<Sheet> sheetIterator = chain.sheetIterator(); 
		while (sheetIterator.hasNext()) {
			Sheet sheet = sheetIterator.next();
			BackboneSegment prevStrand = null;
			for (BackboneSegment strand : sheet) {
				if (prevStrand != null) {
					SecStrucElement prevElement = segmentElementMap.get(prevStrand);
					SecStrucElement currElement = segmentElementMap.get(strand);
					prevElement.addBridgePartner(currElement.getSymbolNumber());
					currElement.addBridgePartner(prevElement.getSymbolNumber());
					prevElement.addBridgePartnerSide("R"); // XXX
					currElement.addBridgePartnerSide("L"); // XXX
					char relativeOrientation = prevStrand.getRelativeOrientation(strand);
					prevElement.addBridgePartnerType(String.valueOf(relativeOrientation));
					currElement.addBridgePartnerType(String.valueOf(relativeOrientation));
				}
				prevStrand = strand;
			}
		}
		return cartoon;
	}
}

