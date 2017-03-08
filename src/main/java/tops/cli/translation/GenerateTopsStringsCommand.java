package tops.cli.translation;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.data.dssp.DSSPReader;
import tops.translation.model.Protein;
import tops.view.diagram.DiagramConverter;

public class GenerateTopsStringsCommand implements Command {
    
    private final double DEFAULT_MIN_ENERGY = -0.9;
    
    private final String directoryPath = "/Users/maclean/data/dssp/reps";

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
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
    
    private String getTopsString(File file) throws IOException {
        Protein protein = new DSSPReader(DEFAULT_MIN_ENERGY).read(file.getPath());
//      Chain chain = protein.getChains().get(0);
      DiagramConverter converter = new DiagramConverter();
//      Graph graph = converter.toDiagram(protein);
//      return converter.toTopsString(graph);
      return String.valueOf(protein.toTopsChainStringArray());  // FIXME
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
