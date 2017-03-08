package tops.cli.classification;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.model.classification.Level;

public class LevelIteratorCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHelp() {
        return "<filename>";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String filename = args[0];
        
        try {
            Level root = Level.fromFile(filename, Level.ROOT, "test");

            Iterator<Level> cLevelIterator = root.getSubLevelIterator(Level.C);
            System.out.println("Classes : ");
            while (cLevelIterator.hasNext()) {
                Level classLevel = cLevelIterator.next();
                System.out.println(Level.levelName(classLevel.depth) + " "
                        + classLevel.getFullCode());
            }

            Iterator<Level> aLevelIterator = root.getSubLevelIterator(Level.A);
            System.out.println("Architectures : ");
            while (aLevelIterator.hasNext()) {
                Level architectureLevel = aLevelIterator.next();
                System.out.println(Level.levelName(architectureLevel.depth)
                        + " " + architectureLevel.getFullCode());
            }

            Iterator<Level> tLevelIterator = root.getSubLevelIterator(Level.T);
            System.out.println("Topologies : ");
            while (tLevelIterator.hasNext()) {
                Level topologyLevel = tLevelIterator.next();
                System.out.println(Level.levelName(topologyLevel.depth) + " "
                        + topologyLevel.getFullCode());
            }

            Iterator<Level> hLevelIterator = root.getSubLevelIterator(Level.H);
            System.out.println("Homologous Superfamilies : ");
            while (hLevelIterator.hasNext()) {
                Level homologyLevel = hLevelIterator.next();
                System.out.println(Level.levelName(homologyLevel.depth) + " "
                        + homologyLevel.getFullCode());
            }

        } catch (IOException ioe) {
            System.out.println(ioe);
        }
        
    }

}
