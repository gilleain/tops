package tops.cli.translation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.translation.CATHDomainFileParser;
import tops.translation.FoldAnalyser;
import tops.translation.PDBReader;
import tops.translation.PropertyError;
import tops.translation.model.Domain;
import tops.translation.model.Protein;

public class FoldAnalyzerCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String pdbFilename = args[0];
        String cathFilename = args[1];

        try {
            Protein protein = PDBReader.read(pdbFilename);

            FoldAnalyser foldAnalyser = new FoldAnalyser();
            foldAnalyser.analyse(protein);

            System.out.println(protein);

            Map<String, List<Domain>> cathChainDomainMap = 
                    CATHDomainFileParser.parseUpToParticularID(
                            cathFilename, protein.getID());
            Map<String, Map<String, String>> chainDomainStringMap = 
                    protein.toTopsDomainStrings(cathChainDomainMap);

            for (String chainID : chainDomainStringMap.keySet()) {
                Map<String, String> domainStrings = chainDomainStringMap.get(chainID);
                for (String domainString : domainStrings.keySet()) {
                    System.out.println(protein.getID() + domainString);
                }
            }

        } catch (IOException ioe) {
            System.err.println(ioe);
        } catch (PropertyError pe) {
            System.err.println(pe);
        }
    }

}
