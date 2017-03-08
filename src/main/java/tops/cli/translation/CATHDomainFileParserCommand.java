package tops.cli.translation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.translation.CATHDomainFileParser;
import tops.translation.model.Domain;

public class CATHDomainFileParserCommand implements Command {

    @Override
    public String getDescription() {
        return "Parse a CATH domain file";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String domainFile = args[0]; 
        try {
            Map<String, Map<String, List<Domain>>> pdbChainDomainMap = 
                    CATHDomainFileParser.parseWholeFile(domainFile);
            for (String pdbID : pdbChainDomainMap.keySet()) {
                Map<String, List<Domain>> chainDomainMap = pdbChainDomainMap.get(pdbID);
                for (String chainID : chainDomainMap.keySet()) {
                    for (Domain domain : chainDomainMap.get(chainID)) {
                        System.out.println(pdbID + chainID + " " + domain);
                    }
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
