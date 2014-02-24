package tops.web.display.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse the URI path to a tops cartoon. An example might be:
 * 
 *  /cartoon/cath/300x300/1.3.5/2bopA0.gif
 *  
 *  where :
 *  - 'cartoon' is the name of the servlet (defined in web.xml)
 *  - 'cath' is the domain classification scheme
 *  - '300x300' is the width and height of the image to return
 *  - '1.3.5' is the list of highlights to apply to the sses
 *  - '2bopA0.gif' is the name of the file, with the extension defining the return type 
 *
 * @author maclean
 *
 */
public class PathParser {
	
	private final int DEFAULT_WIDTH;
	private final int DEFAULT_HEIGHT;
	
	public PathParser() {
		this(200, 200);
	}
	
	public PathParser(int defaultWidth, int defaultHeight) {
		this.DEFAULT_WIDTH = defaultWidth;
		this.DEFAULT_HEIGHT = defaultHeight;
	}
	
	public Map<String, String> parsePath(String path) throws IOException, StringIndexOutOfBoundsException {
		Map<String, String> params = new HashMap<String, String>();
		if (path == null) {
			throw new IOException("Directory not found");
		}

		path = path.substring(0); // crude way to chomp off the first character
		String[] bits = path.split("/"); // get the bits

		if (bits.length == 0) {
			throw new IOException("No file specified");
		}

		String size = null;
		String group = bits[1]; // eg 'cath'
		params.put("group", group);

		String file = null;
		String highlight = null;
		if (bits.length == 5) { // size, highlight AND name
			size = bits[2];
			highlight = bits[3];
			file = bits[4];
		} else if (bits.length == 4) {
			if (bits[2].indexOf('x') != -1) { // test to see if the string has
												// an 'x' in it!
				size = bits[2]; // eg '200x200'
			} else {
				highlight = bits[2]; // eg '1.2.5'
			}

			file = bits[3]; // eg '2bopA0.gif'
		} else {
			file = bits[2];
		}
		params.put("highlight", highlight);

		String wStr;
		String hStr;
		if (size != null) {
			int xPos = size.indexOf('x');
			wStr = size.substring(0, xPos);
			hStr = size.substring(xPos + 1);
		} else {
			wStr = String.valueOf(DEFAULT_WIDTH);
			hStr = String.valueOf(DEFAULT_HEIGHT);
		}
		params.put("width", wStr);
		params.put("height", hStr);

		int dot = file.indexOf("."); // get start of filetype
		String fileType = file.substring(dot + 1); // get extension
		params.put("fileType", fileType);

		String domain = file.substring(0, dot); // eg '2bopA0'
		params.put("domain", domain);

		String chain = domain.substring(0, 5); // eg '2bopA'
		params.put("chain", chain);

		String filename = chain + ".tops"; // fixed, but hey
		params.put("filename", filename);

		return params;
	}
}
