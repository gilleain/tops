package tops.port;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import tops.dw.io.TopsFileReader;
import tops.dw.protein.DomainDefinition;
import tops.dw.protein.SecStrucElement;
import tops.dw.protein.TopsFileFormatException;
import tops.port.calculate.Configure;
import tops.port.io.TopsFileWriter;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.view.cartoon.CartoonDrawer;

public class TestOptimise {
    
    @Test
    public void test1GSO() throws IOException {
        DsspReader dsspReader = new DsspReader();
        Protein protein = 
                dsspReader.readDsspFile("/Users/maclean/data/dssp/reps/1ifc.dssp");
        Chain chain = protein.getChains().get(0);
        Configure configure = new Configure();
        configure.configure(chain);
        
//        
        Optimise optimise = new Optimise();
        optimise.optimise(chain);
        
        draw("1ifc", protein, "test.png");  // XXX optimising chain but passing protein!!
//        for (SSE sse : chain.getSSEs()) {
//            System.out.println(sse.getSymbolNumber() + 
//                    String.format(" at (%s, %s) is %s", 
//                            sse.getCartoonX(), sse.getCartoonY(), sse.getDirection()));
//
//        }
//        System.out.println(chain.toTopsFile());
    }
    
    private void draw(String name, Protein protein, String outputFilepath) throws TopsFileFormatException, IOException {
//        tops.dw.protein.Protein dwProtein = convertOnDisk(name, chain);
        tops.dw.protein.Protein dwProtein = convertInMemory(name, protein);
        List<DomainDefinition> dd = dwProtein.getDomainDefs();
        List<SecStrucElement> ll = dwProtein.getLinkedLists();
        CartoonDrawer drawer = new CartoonDrawer();
        
        FileOutputStream fos = new FileOutputStream(outputFilepath);
        final int w = 300;
        final int h = 300;
        for (int i = 0; i < dd.size(); i++) {
            SecStrucElement root = (SecStrucElement) ll.get(i);
            System.out.println("drawing");
            drawer.draw(name, "IMG", w, h, root, fos);
        }
    }
    
    private tops.dw.protein.Protein convertOnDisk(String name, Protein protein) throws IOException {
        String filename = "tmp.tops";
        TopsFileWriter fileWriter = new TopsFileWriter();
        fileWriter.writeTOPSFile(filename, protein);
        
        TopsFileReader topsFileReader = new TopsFileReader();
        return topsFileReader.readTopsFile(new File("tmp.tops"));
    }
    
    private tops.dw.protein.Protein convertInMemory(String name, Protein protein) throws TopsFileFormatException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        TopsFileWriter fileWriter = new TopsFileWriter();
        fileWriter.writeTOPSFile(new PrintStream(baos), protein);
        
        TopsFileReader topsFileReader = new TopsFileReader();
        return topsFileReader.readTopsFile(new BufferedReader(new StringReader(baos.toString())));
    }

}
