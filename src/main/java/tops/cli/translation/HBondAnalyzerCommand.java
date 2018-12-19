package tops.cli.translation;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.translation.HBondAnalyser;
import tops.translation.PDBReader;
import tops.translation.PropertyError;
import tops.translation.model.Protein;

public class HBondAnalyzerCommand extends BaseCommand {

    @Override
    public String getDescription() {
        return "Analyze hbonds";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String pdbFilename = args[0];
        String propertiesFilename = args[1];

        try {
            HBondAnalyser hBondAnalyser = new HBondAnalyser();
            hBondAnalyser.loadProperties(new FileInputStream(propertiesFilename));
            hBondAnalyser.storeProperties(System.err);

            Protein protein = PDBReader.read(pdbFilename);

            hBondAnalyser.analyse(protein);
            output(protein.toString());
        } catch (IOException ioe) {
            error(ioe);
        } catch (PropertyError pe) {
            error(pe);
        }
    }

    @Override
    public String getHelp() {
        return "<pdbFilename> <propertiesFilename>";
    }

}
