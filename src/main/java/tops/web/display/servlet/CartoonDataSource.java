package tops.web.display.servlet;

import java.io.IOException;
import java.util.Map;

import tops.dw.protein.Protein;

/**
 * Temporary interface to allow different data sources (uploaded PDB file, tops file on disk, etc) 
 * to provide 'cartoon' objects - which are currently tops.dw.protein.Protein objects - for drawing.
 * 
 * In time, this should transition to a layout of a tops.translation.Protein object.
 *
 * @author maclean
 *
 */
public interface CartoonDataSource {

	public Protein getCartoon() throws IOException;
	
	public Map<String, String> getParams();
}
