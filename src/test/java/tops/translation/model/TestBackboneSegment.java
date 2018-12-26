package tops.translation.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestBackboneSegment {
    
    @Test
    public void testEmptySegmentsAreEqual() {
        Helix helix = new Helix();
        Helix otherHelix = new Helix();
        assertTrue(helix.equals(otherHelix));
    }

}
