package tops.cli.classification;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.model.classification.Level;

public class LevelIteratorCommand extends BaseCommand {

    @Override
    public String getDescription() {
        return "Describe a CATH hierarchy file in terms of its levels";
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
            output("Classes : ");
            while (cLevelIterator.hasNext()) {
                Level classLevel = cLevelIterator.next();
                output(Level.levelName(classLevel.depth) + " " + classLevel.getFullCode());
            }

            Iterator<Level> aLevelIterator = root.getSubLevelIterator(Level.A);
            output("Architectures : ");
            while (aLevelIterator.hasNext()) {
                Level architectureLevel = aLevelIterator.next();
                output(Level.levelName(architectureLevel.depth) + " " + architectureLevel.getFullCode());
            }

            Iterator<Level> tLevelIterator = root.getSubLevelIterator(Level.T);
            output("Topologies : ");
            while (tLevelIterator.hasNext()) {
                Level topologyLevel = tLevelIterator.next();
                output(Level.levelName(topologyLevel.depth) + " " + topologyLevel.getFullCode());
            }

            Iterator<Level> hLevelIterator = root.getSubLevelIterator(Level.H);
            output("Homologous Superfamilies : ");
            while (hLevelIterator.hasNext()) {
                Level homologyLevel = hLevelIterator.next();
                output(Level.levelName(homologyLevel.depth) + " " + homologyLevel.getFullCode());
            }

        } catch (IOException ioe) {
            error(ioe);
        }
        
    }

}
