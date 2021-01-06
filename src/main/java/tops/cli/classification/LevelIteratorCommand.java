package tops.cli.classification;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.model.classification.CATHLevel;
import tops.model.classification.CathLevelCode;

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
            CATHLevel root = CATHLevel.fromFile(filename, CathLevelCode.R, "test");

            Iterator<CATHLevel> cLevelIterator = root.getSubLevelIterator(CathLevelCode.C);
            output("Classes : ");
            while (cLevelIterator.hasNext()) {
            	CATHLevel classLevel = cLevelIterator.next();
                output(classLevel.getName().getName() + " " + classLevel.getFullCode());
            }

            Iterator<CATHLevel> aLevelIterator = root.getSubLevelIterator(CathLevelCode.A);
            output("Architectures : ");
            while (aLevelIterator.hasNext()) {
            	CATHLevel architectureLevel = aLevelIterator.next();
                output(architectureLevel.getName().getName() + " " + architectureLevel.getFullCode());
            }

            Iterator<CATHLevel> tLevelIterator = root.getSubLevelIterator(CathLevelCode.T);
            output("Topologies : ");
            while (tLevelIterator.hasNext()) {
            	CATHLevel topologyLevel = tLevelIterator.next();
                output(topologyLevel.getName().getName() + " " + topologyLevel.getFullCode());
            }

            Iterator<CATHLevel> hLevelIterator = root.getSubLevelIterator(CathLevelCode.H);
            output("Homologous Superfamilies : ");
            while (hLevelIterator.hasNext()) {
            	CATHLevel homologyLevel = hLevelIterator.next();
                output(homologyLevel.getName().getName() + " " + homologyLevel.getFullCode());
            }

        } catch (IOException ioe) {
            error(ioe);
        }
        
    }

}
