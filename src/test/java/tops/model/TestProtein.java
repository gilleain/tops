package tops.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestProtein {
    
    @Test
    public void testEmptyProtein() {
        String name = "2bop";
        Protein protein = new Protein(name);
        assertEquals(name, protein.getName());
        assertEquals(0, protein.getChains().size());
    }

}
