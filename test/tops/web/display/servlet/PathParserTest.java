package tops.web.display.servlet;

import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class PathParserTest {
	
	@Test
	public void fourPartPath() throws StringIndexOutOfBoundsException, IOException {
		String path = "cartoon/CATH/300x400/1.2.3/1a2pA0.gif";
		PathParser parser = new PathParser();
		Map<String, String> params = parser.parsePath(path);
		Assert.assertNotNull(params);
		System.out.println(params);
		Assert.assertNotNull(params.get("width"));
		Assert.assertNotNull(params.get("height"));
		Assert.assertNotNull(params.get("highlight"));
	}
	
	@Test
	public void threePartPath() throws StringIndexOutOfBoundsException, IOException {
		String path = "cartoon/CATH/300x400/1a2pA0.gif";
		PathParser parser = new PathParser();
		Map<String, String> params = parser.parsePath(path);
		Assert.assertNotNull(params);
		System.out.println(params);
		Assert.assertNotNull(params.get("width"));
		Assert.assertNotNull(params.get("height"));
		Assert.assertNull(params.get("highlight"));
	}
	
	@Test
	public void twoPartPath() throws StringIndexOutOfBoundsException, IOException {
		String path = "cartoon/CATH/1a2pA0.gif";
		PathParser parser = new PathParser();
		Map<String, String> params = parser.parsePath(path);
		Assert.assertNotNull(params);
		System.out.println(params);
		Assert.assertNotNull(params.get("width"));
		Assert.assertNotNull(params.get("height"));
		Assert.assertNull(params.get("highlight"));
	}

}
