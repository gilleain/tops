package tops.dssp;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import tops.model.Chain;
import tops.model.Protein;
import tops.view.tops2D.diagram.DiagramConverter;
import tops.view.tops2D.diagram.Graph;

/**
 * Not a unit test class, but a transformation utility.
 * 
 * @author maclean
 *
 */
public class GenerateTopsStrings {
    
    private final double DEFAULT_MIN_ENERGY = -0.9;
    
    private final String directoryPath = "/Users/maclean/data/dssp/reps";
    
    @Test
    public void generate() throws IOException {
        File directory = new File(directoryPath);
        for (File file : directory.listFiles()) {
            if (!file.getName().endsWith("dssp")) continue;
//            System.out.println(file);
            String name = file.getName().substring(0, 4);
            try {
                System.out.println(name + " " + getTopsString(file));
            } catch (Exception e) {
                System.out.println("Error for " + name);
                e.printStackTrace();
            }
        }
    }
    
    @Test
    public void test() {
        test("1bte");
    }
    
    
    private void test(String name) {
        try {
            System.out.println(getTopsString(new File(directoryPath, name + ".dssp")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String getTopsString(File file) throws IOException {
        Protein protein = new DSSPReader(DEFAULT_MIN_ENERGY).read(file.getPath());
//      Chain chain = protein.getChains().get(0);
      DiagramConverter converter = new DiagramConverter();
      Graph graph = converter.toDiagram(protein);
      return converter.toTopsString(graph);
    }

}
