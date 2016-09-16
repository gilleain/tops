package tops.engine;

import org.junit.Test;

public class TestTParser {
    
    @Test
    public void testEdges() {
        String topsString = "HEAD NEEC 1:2A";
        TParser parser = new TParser(topsString);
        for (String edge : parser.getEdgesAsStrings()) {
            System.out.println(edge);
        }
    }

}
