package tops.web.display.servlet;

import static tops.port.model.SSEType.EXTENDED;
import static tops.port.model.SSEType.HELIX;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import tops.dw.io.TopsFileReader;
import tops.dw.protein.CathCode;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.port.model.DomainDefinition;

/**
 * Get the location for a protein-cartoon from a URI like "/cath/1.2/2bopA0.gif" or
 *  "/cath/2bopA0.gif" which leads to a set of parameters to find the data.
 *  
 * @author maclean
 *
 */
public class URICartoonDataSource implements CartoonDataSource {

	private Map<String, String> params;
	
	private String sourceDirectory;
	
	private Protein protein;

	public URICartoonDataSource(HttpServletRequest request, ServletConfig config) 
			throws StringIndexOutOfBoundsException, IOException {
		String uri = request.getPathInfo(); 
		PathParser pathParser = new PathParser();
		this.params = pathParser.parsePath(uri);
		
		// translate the group ID into a location for the data
		String pathToFiles = config.getInitParameter(params.get("group"));
        this.sourceDirectory = config.getServletContext().getRealPath(pathToFiles);
        System.out.println(pathToFiles + " -> " + sourceDirectory);
        
        if (params.get("group").equals("session")) {
			protein = (Protein) request.getSession().getAttribute("protein");	// ugh...
		} else {
			protein = getFromFile();
		}
	}

	public Map<String, String> getParams() {
		return this.params;
	}

	@Override
	public Protein getCartoon() throws IOException {

		// do highlights
		String highlight = params.get("highlight");
		if (highlight != null) {
			this.highlight(protein.getDomain(0), highlight);
		}

		return protein;
	}
	
	private Protein getFromFile() throws IOException {
		// first, try and get the data from the given source
		File f = new File(sourceDirectory, params.get("filename"));
		if (!f.canRead()) {
			throw new IOException("File not found " + params.get("filename"));
		}

		// assuming the tops.dw.protein can be created, determine what to return
		return this.getProtein(params.get("domain"), f);
	}

	private Protein getProtein(String domid, File f) throws IOException {
		Protein p = new TopsFileReader().readTopsFile(f);
//		System.out.println("Got protein " + p.getName());

		List<Cartoon> doms = p.getLinkedLists();
		int domainIndex = p.getDomainIndex(new CathCode(domid));
		if (domainIndex == -1) {
			return p;
		}

		Protein pp = new Protein();
		Cartoon s = doms.get(domainIndex);
		DomainDefinition d = p.getDomainDefs().get(domainIndex);
        pp.addTopsLinkedList(s, d);
//        System.out.println("Made protein " + pp.getName());
        return pp;
	}

	private void highlight(Cartoon cartoon, String highlight) {
		Color strandColor = Color.yellow;
		Color helixColor = Color.red;
		Color otherColor = Color.blue;

		// check for special cases - "none" and "all"
		if (highlight.equals("none")) {
			return;
		} else if (highlight.equals("all")) {
			for (SecStrucElement s : cartoon.getSSEs()) {
				if (s.getType() == EXTENDED) {
					s.setColour(strandColor);
				} else if (s.getType() == HELIX) {
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
			SecStrucElement s = cartoon.getSSEByNumber(index);
			if (s.getType().equals("E")) {
				s.setColour(strandColor);
			} else if (s.getType().equals("H")) {
				s.setColour(helixColor);
			} else {
				s.setColour(otherColor);
			}
		}
	}

}
