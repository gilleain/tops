package io;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import tops.data.dssp.DSSPReader;
import tops.data.dssp.SSEType;
import tops.port.calculate.DsspDirectory;
import tops.translation.model.BackboneSegment;
import tops.translation.model.Chain;
import tops.translation.model.HBondSet;
import tops.translation.model.Helix;
import tops.translation.model.Protein;
import tops.translation.model.Strand;

public class TestDsspReaderFromFile {
    
    private static final String PATH = DsspDirectory.DIR;
    
    @Test
    public void test1NOT() throws IOException {
        Protein protein = new DSSPReader().read(stream("1notH.dssp"));
        assertNotNull(protein);
        System.out.println(protein.iterator().next());
    }
    
    @Test
    public void test1QRE() throws IOException {
        test("1qreH", SSEType.S_BEND, -0.9);
    }
    
    @Test
    public void test1TGX() throws IOException {
       test("1tgxAH", SSEType.EXTENDED, -0.1);
    }
    
    @Test
    public void test1WAP() throws IOException {
        test("1wapBH", SSEType.EXTENDED, SSEType.EXTENDED, -0.9);
    }
    
    @Test
    public void test1SWU() throws IOException {
       test("1swuBH", SSEType.EXTENDED, -0.9);
    }
    
    @Test
    public void test1IFC() throws IOException {
       test("1ifcH", null, -0.9);
    }
    
    @Test
    public void test1MOL() throws IOException {
       test("1molAH", SSEType.EXTENDED, -0.9);
    }
    
    @Test
    public void test1NAR() throws IOException {
       test("1narH", SSEType.EXTENDED, -0.9);
    }
    
    @Test
    public void test2BOP() throws IOException {
       test("2bopAH", null, -0.9);
    }
    
    private void test(String name, SSEType type, double minEnergy) throws IOException {
        test(name, type, type, minEnergy);
    }
    
    private void test(String name, SSEType startType, SSEType endType, double minEnergy) throws IOException {
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
                System.out.println(sse);
//                System.out.print(" ");
            }
        }
        System.out.println(" ]");
        
    }
    
    private Reader stream(String filename) throws FileNotFoundException {
        InputStream resource = getClass().getResourceAsStream(PATH + "/" + filename);
        if (resource == null) {
            return new FileReader(PATH + "/" + filename);
        } else {
            return new InputStreamReader(resource);
        }
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
