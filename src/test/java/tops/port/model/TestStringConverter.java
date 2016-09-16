package tops.port.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;

public class TestStringConverter {
    
    @Test
    public void testSingleEdge() {
        Chain chain = StringConverter.convert("test NEeC 1:2A");
        assertEquals(4, chain.getSSEs().size());
        assertEquals(1, chain.getBridges().size());
    }
    
    @Test
    public void testDoubleEdge() {
        Chain chain = StringConverter.convert("test NEeEC 1:2A2:3A");
        assertEquals(5, chain.getSSEs().size());
        assertEquals(2, chain.getBridges().size());
        
        Bridge bridge1 = chain.getBridges().get(0);
        assertEquals(1, bridge1.getSseStart().getSymbolNumber());
        assertEquals(2, bridge1.getSseEnd().getSymbolNumber());
        
        Bridge bridge2 = chain.getBridges().get(1);
        assertEquals(2, bridge2.getSseStart().getSymbolNumber());
        assertEquals(3, bridge2.getSseEnd().getSymbolNumber());
    }

}
