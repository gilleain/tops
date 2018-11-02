package transform;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import tops.port.calculate.Configure;
import tops.port.calculate.DsspDirectory;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.tse.BaseTSE;
import tops.port.model.tse.Sandwich;

public class RunAll {
    
    private static final String PATH = DsspDirectory.DIR;
    
    private interface Callback {
        public void handle(Protein protein, Chain chain);
    }
    
    @Test
    public void runBasic() {
        run(new Callback() {

            @Override
            public void handle(Protein protein, Chain chain) {
                System.out.println(protein.getProteinCode() + chain.toString());                
            }
            
        });
    }
    
    @Test
    public void runSandwiches() {
        run(new Callback() {

            @Override
            public void handle(Protein protein, Chain chain) {
                StringBuffer sb = new StringBuffer();
                boolean hasSandwich = false;
                int sandwichIndex = 0;
                for (BaseTSE tse : chain.getTSEs()) {
                    if (tse instanceof Sandwich) {
                        hasSandwich = true;
                        sandwichIndex++;
                        sb.append("\n").append(sandwichIndex).append(" ").append(tse.toString());
                    }
                }
                if (hasSandwich) {
                    System.out.println(
                            protein.getProteinCode() + 
                            chain.toString() +
                            " " + sb.toString());
                }
            }
            
        });
    }
    
    public void run(Callback callback) {
        Logger.getLogger("").setLevel(Level.OFF);
        File dirFile = new File(PATH);
        DsspReader dsspReader = new DsspReader();
        Configure configure = new Configure();
        for (File file : dirFile.listFiles()) {
            if (!file.getName().endsWith(".dssp")) continue;
            try {
                Protein protein = dsspReader.readDsspFile(file.getAbsolutePath());
                Chain chain = protein.getChains().get(0);
                configure.configure(chain);
                callback.handle(protein, chain);
            } catch (Exception e) {
                System.out.println("Failed " + file.getName() + " " + e.getStackTrace()[0]);
            }
        }
    }

}
