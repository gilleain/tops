package tops.dssp;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import tops.translation.model.BackboneSegment;
import tops.translation.model.Chain;
import tops.translation.model.HBondSet;
import tops.translation.model.Helix;
import tops.translation.model.Protein;
import tops.translation.model.Strand;

public class TestDsspReaderFromFile {
    
    public static final String DATA_DIR = "data";
    
    @Test
    public void test1NOT() throws FileNotFoundException, IOException {
        Protein protein = new DSSPReader().read(stream("1not.dssp"));
        assertNotNull(protein);
        System.out.println(protein.iterator().next());
    }
    
    @Test
    public void test1QRE() throws FileNotFoundException, IOException {
//       test("1qre", SSEType.EXTENDED, -0.9);
        test("1qre", SSEType.S_BEND, -0.9);
    }
    
    @Test
    public void test1TGX() throws FileNotFoundException, IOException {
       test("1tgx", SSEType.EXTENDED, -0.1);
    }
    
    @Test
    public void test1WAP() throws FileNotFoundException, IOException {
        test("1wap", SSEType.EXTENDED, SSEType.EXTENDED, -0.9);
    }
    
    @Test
    public void test1SWU() throws FileNotFoundException, IOException {
       test("1swu", SSEType.EXTENDED, -0.9);
    }
    
    @Test
    public void test1IFC() throws FileNotFoundException, IOException {
       test("1ifc", null, -0.9);
    }
    
    @Test
    public void test1MOL() throws FileNotFoundException, IOException {
       test("1mol", SSEType.EXTENDED, -0.9);
    }
    
    @Test
    public void test1NAR() throws FileNotFoundException, IOException {
       test("1nar", SSEType.EXTENDED, -0.9);
    }
    
    @Test
    public void test2BOP() throws FileNotFoundException, IOException {
       test("2bop", SSEType.EXTENDED, -0.9);
    }
    
    private void test(String name) throws FileNotFoundException, IOException {
        test(name, SSEType.EXTENDED, -0.01);
    }
    
    private void test(String name, SSEType type, double minEnergy) throws FileNotFoundException, IOException {
        test(name, type, type, minEnergy);
    }
    
    private void test(String name, SSEType startType, SSEType endType, double minEnergy) throws FileNotFoundException, IOException {
        Protein protein = new DSSPReader(minEnergy).read(stream(name + ".dssp"));
        assertNotNull(protein);
        Chain chain = protein.iterator().next();
        printSSEByType(chain.getBackboneSegments(), startType);
        if (startType == endType) {
//            printHBondSets(chain, startType, null);
//            printHBondSets(chain, null, endType);
        } else {
            printHBondSets(chain, startType, endType);
        }
//        DiagramConverter converter = new DiagramConverter();
//        Graph graph = converter.toDiagram(protein);
//        System.out.println(converter.toTopsString(graph));
        for (String s : protein.toTopsChainStringArray()) {
            System.out.println(s);
        }
    }
    
    private void printHBondSets(Chain chain, SSEType startType, SSEType endType) {
        List<HBondSet> hBondSets = chain.getHBondSets();
//        Collections.sort(hBondSets, sort);
        for (HBondSet hBondSet : hBondSets) {
            BackboneSegment start = hBondSet.getStart();
            BackboneSegment end = hBondSet.getEnd();
            if ((startType == null || hasType(start, startType)) 
                    && (endType == null || hasType(end, endType))) {
                System.out.println(hBondSet);
            }
        }
    }
    
    private boolean hasType(BackboneSegment sse, SSEType type) {
        switch (type) {
            case ALPHA_HELIX: return sse instanceof Helix;
            case EXTENDED: return sse instanceof Strand;
            default: return false;
        }
    }
    
    private void printSSEByType(List<BackboneSegment> sses, SSEType type) {
        System.out.print("[ ");
        for (BackboneSegment sse : sses) {
            if (type == null || hasType(sse, type)) {
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
            int d = o1.getStart().getNumber() - o2.getStart().getNumber();
            if (d == 0) {
                return Integer.compare(o1.getEnd().getNumber(), o2.getEnd().getNumber());
            } else {
                return d;
            }
        }
        
    };

}
