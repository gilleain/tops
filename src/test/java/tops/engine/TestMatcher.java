package tops.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import tops.engine.drg.Matcher;
import tops.engine.drg.Pattern;

public class TestMatcher {
	
	/**
	 * A topology graph matches itself.
	 */
	@Test
	public void testSelfMatch() throws TopsStringFormatException {
		Pattern pattern = new Pattern("pattern NEEC 1:2P");
		Pattern instance = new Pattern("instance NEEC 1:2P");
		Matcher matcher = new Matcher();
		String result = matcher.match(pattern, instance);
		assertNotEquals("Identical should match", "", result);
	}
	
	/**
	 * A topology graph matches flipped.
	 */
	@Test
	public void testSelfFlippedMatch() throws TopsStringFormatException {
		Pattern pattern = new Pattern("pattern NEEC 1:2P");
		Pattern instance = new Pattern("instance NeeC 1:2P");
		Matcher matcher = new Matcher();
		String result = matcher.match(pattern, instance);
		assertNotEquals("Flipped should match", "", result);
	}
	
	/**
	 * Different edges types do not match.
	 */
	@Test
	public void testSimpleFail() throws TopsStringFormatException {
		Pattern pattern = new Pattern("pattern NEEC 1:2P");
		Pattern instance = new Pattern("instance NEeC 1:2A");
		Matcher matcher = new Matcher();
		String result = matcher.match(pattern, instance);
		assertEquals("Different should not match", "", result);
	}

}
