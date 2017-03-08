package tops.cli.translation;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.translation.HBondAnalyser;
import tops.translation.PDBReader;
import tops.translation.PropertyError;
import tops.translation.model.Protein;

public class HBondAnalyzerCommand implements Command {

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
            System.out.println(protein);
        } catch (IOException ioe) {
            System.err.println(ioe);
        } catch (PropertyError pe) {
            System.err.println(pe);
        }
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
