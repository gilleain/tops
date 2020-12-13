package tops.cli.translation;

import java.io.File;

import org.junit.Test;

public class TestRunTops {
    
//    @Test	// comment out after use!
    public void convert() {
        String topsHome = "/home/gilleain/Code/eclipse/ctops/Debug/";
        String topsDir = topsHome + "Tops";
//        String dsspDir = "/home/gilleain/Data/dsspfiles";
        String dsspDir = "dsspfiles";
        String outputDir = "/home/gilleain/Data/topsfiles";
        RunTops runTops = new RunTops(topsDir, dsspDir, outputDir, topsHome);
        runTops.addEnvironmentVariable("TOPS_HOME", topsHome);
        File inputDir = new File(topsHome, dsspDir);
        String[] files = inputDir.list((dir, name) -> name.endsWith(".dssp"));
        String domainFilePath = ""; // TODO - can this be null?
//        int debug = 0;
//        for (int i = 0; i < files.length && debug < 10; i++) {
        for (int i = 0; i < files.length; i++) {
            try {
//                String pdbid = files[i].substring(0, files[i].length() - 5);
                String pdbid = files[i].substring(0, 4);
                String chain = files[i].substring(4, 5);
                System.out.println(pdbid + " " + chain);
                runTops.convert(pdbid, "", pdbid, domainFilePath);
//                debug++;
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

}
