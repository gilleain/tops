package tops.cli.translation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.translation.CATHDomainFileParser;
import tops.translation.FoldAnalyser;
import tops.translation.PDBReader;
import tops.translation.PropertyError;
import tops.translation.model.Domain;
import tops.translation.model.Protein;

public class FoldAnalyzerCommand extends BaseCommand {

    @Override
    public String getDescription() {
        return "Analyse folds";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String pdbFilename = args[0];
        String cathFilename = args[1];

        try {
            Protein protein = PDBReader.read(pdbFilename);

            FoldAnalyser foldAnalyser = new FoldAnalyser();
            foldAnalyser.analyse(protein);

            output(protein.toString()); // TODO - do we really want to print the whole protein

            Map<String, List<Domain>> cathChainDomainMap = 
                    CATHDomainFileParser.parseUpToParticularID(
                            cathFilename, protein.getID());
            Map<String, Map<String, String>> chainDomainStringMap = 
                    protein.toTopsDomainStrings(cathChainDomainMap);

            for (Entry<String, Map<String, String>> entry : chainDomainStringMap.entrySet()) {
                Map<String, String> domainStrings = entry.getValue();
                for (String domainString : domainStrings.keySet()) {
                    output(protein.getID() + domainString);
                }
            }

        } catch (IOException ioe) {
            error(ioe);
        } catch (PropertyError pe) {
            error(pe);
        }
    }

    @Override
    public String getHelp() {
        return "<pdbFilename> <cathFilename>";
    }

}
