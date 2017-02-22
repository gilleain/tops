package tops.dssp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tops.data.dssp.DsspModel;

public class TestDsspModel {
    
    @Test
    public void parseLine() {
        String lineString = "   12   12   S  B      A    2   0A   0    -10,-2.6   -10,-2.8    -2,-0.0    -2,-0.0  -0.742 360.0 360.0-159.2 104.2   -0.2    2.5   -3.9";
        DsspModel model = new DsspModel();
        DsspModel.Line line = model.addLine(lineString);
        assertEquals("12", line.dsspNumber);
        assertEquals("12", line.pdbNumber);
        assertEquals("S", line.aminoAcidName);
        assertEquals("B", line.sseType);
        assertEquals("A", line.structure);
        assertEquals("2", line.bridgePartner1);
        assertEquals("0", line.bridgePartner2);
        assertEquals("A", line.sheetName);
        assertEquals("0", line.acc);
        assertEquals("-10,-2.6", line.nho1);
        assertEquals("-10,-2.8", line.ohn1);
        assertEquals("-2,-0.0", line.nho2);
        assertEquals("-2,-0.0", line.ohn2);
        assertEquals("-0.742", line.tco);
        assertEquals("360.0", line.kappa);
        assertEquals("360.0", line.alpha);
        assertEquals("-159.2", line.phi);
        assertEquals("104.2", line.psi);
        assertEquals("-0.2", line.xca);
        assertEquals("2.5", line.yca);
        assertEquals("-3.9", line.zca);
    }

}
