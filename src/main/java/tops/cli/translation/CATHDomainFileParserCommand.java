package tops.cli.translation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.translation.CATHDomainFileParser;
import tops.translation.model.Domain;

public class CATHDomainFileParserCommand extends BaseCommand {
    
    private Logger log = Logger.getLogger(CATHDomainFileParserCommand.class.getName());

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
            for (Entry<String, Map<String, List<Domain>>> entry : pdbChainDomainMap.entrySet()) {
                String pdbID = entry.getKey();
                Map<String, List<Domain>> chainDomainMap = entry.getValue();
                for (Entry<String, List<Domain>> entry2 : chainDomainMap.entrySet()) {
                    String chainID = entry2.getKey();
                    for (Domain domain : entry2.getValue()) {
                        output(pdbID + chainID + " " + domain);
                    }
                }
            }
        } catch (IOException ioe) {
            log.warning(ioe.toString());
        }
    }

    @Override
    public String getHelp() {
        return "<domainFile>";
    }

}
