package tops.cli.drawing;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.dw.editor.BatchAnnotater;

public class BatchAnnotationCommand implements Command {

    @Override
    public String getDescription() {
        return "Draw cartoons annotated by residue number";
    }

    @Override
    public String getHelp() {
        return "<inputFile> <outputDir> <inputDir>";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String inputFile = args[0];
        String outputDirectory = args[1];
        String inputDir = args[2];
        
        Map<String, List<Integer>> map = BatchAnnotater.parse(new File(inputFile));
        
        for (Entry<String, List<Integer>> entry : map.entrySet()) {
            String pdbid = entry.getKey();
            List<Integer> r = entry.getValue();
            int[] residuesToAnnotate = new int[r.size()];
            for (int i = 0; i < r.size(); i++) {
                residuesToAnnotate[i] = r.get(i);
            }
            File file = new File(inputDir, pdbid + ".tops");
            BatchAnnotater.annotateCartoon(file, outputDirectory, residuesToAnnotate);  
        }
    }
}
