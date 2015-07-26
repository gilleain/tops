package tops.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import tops.engine.drg.Pattern;

public class TestLinearMatcher {

    /**
     * A topology graph matches itself.
     */
    @Test
    public void testSelfMatch() throws TopsStringFormatException {
        Pattern pattern = new Pattern("pattern NEEC 1:2P");
        Pattern instance = new Pattern("instance NEEC 1:2P");
        LinearMatcher matcher = new LinearMatcher();
        assertTrue("Identical should match", matcher.matches(pattern, instance));
    }
    
    /**
     * A topology graph matches flipped.
     */
    @Test
    public void testSelfFlippedMatch() throws TopsStringFormatException {
        Pattern pattern = new Pattern("pattern NEEC 1:2P");
        Pattern instance = new Pattern("instance NeeC 1:2P");
        LinearMatcher matcher = new LinearMatcher();
        assertTrue("Flipped should match", matcher.matches(pattern, instance));
    }
    
    /**
     * Different edges types do not match.
     */
    @Test
    public void testSimpleFail() throws TopsStringFormatException {
        Pattern pattern = new Pattern("pattern NEEC 1:2P");
        Pattern instance = new Pattern("instance NEeC 1:2A");
        LinearMatcher matcher = new LinearMatcher();
        assertFalse("Different should not match", matcher.matches(pattern, instance));
    }
}
