package tops.dssp;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import tops.model.Chain;
import tops.model.HBondSet;
import tops.model.Protein;
import tops.model.SSE;

public class TestDsspReaderFromFile {
    
    public static final String DATA_DIR = "data";
    
    @Test
    public void test1NOT() throws FileNotFoundException, IOException {
        Protein protein = new DSSPReader().read(stream("1not.dssp"));
        assertNotNull(protein);
        System.out.println(protein.getChains().get(0).getSSES());
    }
    
    @Test
    public void test1QRE() throws FileNotFoundException, IOException {
//       test("1qre", SSE.Type.EXTENDED, -0.9);
        test("1qre", SSE.Type.S_BEND, -0.9);
    }
    
    @Test
    public void test1TGX() throws FileNotFoundException, IOException {
       test("1tgx", SSE.Type.EXTENDED, -0.1);
    }
    
    @Test
    public void test1WAP() throws FileNotFoundException, IOException {
        test("1wap", SSE.Type.EXTENDED, -0.9);
    }
    
    @Test
    public void test1SWU() throws FileNotFoundException, IOException {
       test("1swu", SSE.Type.EXTENDED, -0.9);
    }
    
    @Test
    public void test1IFC() throws FileNotFoundException, IOException {
       test("1ifc", SSE.Type.EXTENDED, -0.9);
    }
    
    @Test
    public void test1MOL() throws FileNotFoundException, IOException {
       test("1mol", SSE.Type.EXTENDED, -0.9);
    }
    
    @Test
    public void test1NAR() throws FileNotFoundException, IOException {
       test("1nar", SSE.Type.EXTENDED, -0.9);
    }
    
    private void test(String name) throws FileNotFoundException, IOException {
        test(name, SSE.Type.EXTENDED, -0.01);
    }
    
    private void test(String name, SSE.Type type, double minEnergy) throws FileNotFoundException, IOException {
        test(name, type, type, minEnergy);
    }
    
    private void test(String name, SSE.Type startType, SSE.Type endType, double minEnergy) throws FileNotFoundException, IOException {
        Protein protein = new DSSPReader(minEnergy).read(stream(name + ".dssp"));
        assertNotNull(protein);
        Chain chain = protein.getChains().get(0);
        printSSEByType(chain.getSSES(), startType);
        if (startType == endType) {
            printHBondSets(chain, startType, null);
            printHBondSets(chain, null, endType);
        } else {
            printHBondSets(chain, startType, endType);
        }
    }
    
    private void printHBondSets(Chain chain, SSE.Type startType, SSE.Type endType) {
        List<HBondSet> hBondSets = chain.getHBondSets();
        Collections.sort(hBondSets, sort);
        for (HBondSet hBondSet : hBondSets) {
            SSE start = hBondSet.getStart();
            SSE end = hBondSet.getEnd();
            if ((startType == null || start.getType() == startType) 
                    && (endType == null || end.getType() == endType)) {
                System.out.println(hBondSet);
            }
        }
    }
    
    private void printSSEByType(List<SSE> sses, SSE.Type type) {
        System.out.print("[ ");
        for (SSE sse : sses) {
            if (type == null || sse.getType() == type) {
                System.out.print(sse);
                System.out.print(" ");
            }
        }
        System.out.println(" ]");
        
    }
    
    private Reader stream(String filename) throws FileNotFoundException {
        return new InputStreamReader(getClass().getResourceAsStream(DATA_DIR + "/" + filename));
    }
    
    private Comparator<HBondSet> sort = new Comparator<HBondSet>() {

        @Override
        public int compare(HBondSet o1, HBondSet o2) {
            int d = o1.getStart().getStart() - o2.getStart().getStart();
            if (d == 0) {
                return Integer.compare(o1.getEnd().getStart(), o2.getEnd().getStart());
            } else {
                return d;
            }
        }
        
    };

}
