package tops.web.display.servlet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

public class PathParserTest {
	
	@Test
	public void fourPartPath() throws StringIndexOutOfBoundsException, IOException {
		String path = "cartoon/CATH/300x400/1.2.3/1a2pA0.gif";
		PathParser parser = new PathParser();
		Map<String, String> params = parser.parsePath(path);
		assertNotNull(params);
		System.out.println(params);
		assertNotNull(params.get("width"));
		assertNotNull(params.get("height"));
		assertNotNull(params.get("highlight"));
	}
	
	@Test
	public void threePartPath() throws StringIndexOutOfBoundsException, IOException {
		String path = "cartoon/CATH/300x400/1a2pA0.gif";
		PathParser parser = new PathParser();
		Map<String, String> params = parser.parsePath(path);
		assertNotNull(params);
		System.out.println(params);
		assertNotNull(params.get("width"));
		assertNotNull(params.get("height"));
		assertNull(params.get("highlight"));
	}
	
	@Test
	public void twoPartPath() throws StringIndexOutOfBoundsException, IOException {
		String path = "cartoon/CATH/1a2pA0.gif";
		PathParser parser = new PathParser();
		Map<String, String> params = parser.parsePath(path);
		assertNotNull(params);
		System.out.println(params);
		assertNotNull(params.get("width"));
		assertNotNull(params.get("height"));
		assertNull(params.get("highlight"));
	}

}
