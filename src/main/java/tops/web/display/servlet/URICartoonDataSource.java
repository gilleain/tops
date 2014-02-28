package tops.web.display.servlet;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import tops.dw.protein.CATHcode;
import tops.dw.protein.DomainDefinition;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;

/**
 * Get the location for a protein-cartoon from a URI like "/cath/1.2/2bopA0.gif" or
 *  "/cath/2bopA0.gif" which leads to a set of parameters to find the data.
 *  
 * @author maclean
 *
 */
public class URICartoonDataSource implements CartoonDataSource {

	private Map<String, String> params;

	public URICartoonDataSource(HttpServletRequest request) throws StringIndexOutOfBoundsException, IOException {
		String uri = request.getPathInfo(); 
		PathParser pathParser = new PathParser();
		this.params = pathParser.parsePath(uri);
	}

	public Map<String, String> getParams() {
		return this.params;
	}

	@Override
	public Protein getCartoon(String directory) throws IOException {

		// first, try and get the data from the given source
		File f = new File(directory, params.get("filename"));
		if (!f.canRead()) {
			throw new IOException("File not found " + params.get("filename"));
		}
		
		 // assuming the tops.dw.protein can be created, determine what to return
        Protein protein = this.getProtein(params.get("domain"), f);

		// do highlights
		String highlight = params.get("highlight");
		if (highlight != null) {
			this.highlight(protein.getDomain(0), highlight);
		}

		return protein;
	}

	private Protein getProtein(String domid, File f) throws IOException {
		Protein p = new Protein(f);
//		System.out.println("Got protein " + p.getName());

		Vector<SecStrucElement> doms = p.GetLinkedLists();
		int domainIndex = p.GetDomainIndex(new CATHcode(domid));
		if (domainIndex == -1) {
			return p;
		}

		Protein pp = new Protein();
		SecStrucElement s = doms.elementAt(domainIndex);
		DomainDefinition d = p.GetDomainDefs().elementAt(domainIndex);
        pp.AddTopsLinkedList(s, d);
//        System.out.println("Made protein " + pp.getName());
        return pp;
	}

	private void highlight(SecStrucElement root, String highlight) {
		Color strandColor = Color.yellow;
		Color helixColor = Color.red;
		Color otherColor = Color.blue;

		// check for special cases - "none" and "all"
		if (highlight.equals("none")) {
			return;
		} else if (highlight.equals("all")) {
			for (SecStrucElement s = root; s != null; s = s.GetTo()) {
				if (s.Type.equals("E")) {
					s.setColour(strandColor);
				} else if (s.Type.equals("H")) {
					s.setColour(helixColor);
				} else {
					s.setColour(otherColor);
				}
			}
			return;
		}

		String[] bits = highlight.split("\\.");
		for (int i = 0; i < bits.length; i++) {
			int index = Integer.parseInt(bits[i]);
			SecStrucElement s = root.GetSSEByNumber(index);
			if (s.Type.equals("E")) {
				s.setColour(strandColor);
			} else if (s.Type.equals("H")) {
				s.setColour(helixColor);
			} else {
				s.setColour(otherColor);
			}
		}
	}

}
