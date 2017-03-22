package tops.port;

import static tops.port.model.DomainDefinition.DomainType.SEGMENT_SET;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import tops.port.model.DomainDefinition;
import tops.port.model.DomainDefinition.DomainType;
import tops.port.model.Protein;

public class DomainBoundaryFileReader {

    public List<DomainDefinition> readDomBoundaryFile(String domBoundaryFile, Protein protein) throws IOException {

        List<DomainDefinition> domains = new ArrayList<DomainDefinition>();
        
        int nsegs, res;
        char codeChain, chain;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(domBoundaryFile));

        int countDoms = 0;
        int nDoms = 0;
        int domsFrom = 0;

        String line = bufferedReader.readLine();
        while (line != null) {

            if ((line.charAt(0) == '#') || (line.charAt(0) == ' ') || (line.charAt(0) == '\n')) {
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line);
            try {
                String currentToken = tokenizer.nextToken();

                if (currentToken.length() < 6) {
                    System.err.print(String.format("Error: reading domain boundary file %s\n", domBoundaryFile));
                    bufferedReader.close();
                    return domains;
                }

                if (currentToken.substring(0, 4).equals(protein.getProteinCode())) {

                    codeChain = currentToken.charAt(4);

                    // read number of domains listed on line /
                    currentToken = tokenizer.nextToken();
                    nDoms = Integer.parseInt(currentToken);

                    domsFrom = countDoms;
                    countDoms += nDoms;

                    // read number of fragments listed on line /
                    currentToken = tokenizer.nextToken();

                    // read all the domains /
                    String proteinCode = protein.getProteinCode();
                    for (int i = domsFrom; i < countDoms; i++) {
                        String code = String.format("%s%s%d", proteinCode, codeChain, (i - domsFrom + 1) % 10);
                        DomainDefinition domain = new DomainDefinition(code, SEGMENT_SET);

                        currentToken = tokenizer.nextToken();
                        nsegs = Integer.parseInt(currentToken);

                        for (int j = 0; j < nsegs; j++) {
                            char chain1 = tokenizer.nextToken().charAt(0);
                            int res1 = Integer.parseInt(tokenizer.nextToken());
                            char chain2 = tokenizer.nextToken().charAt(0);
                            int res2 = Integer.parseInt(tokenizer.nextToken());
                            
                            domain.addSegment(chain1, res1, chain2, res2);
                        }
                        domains.add(domain);
                    }
                }
            } catch (NoSuchElementException nse) {
                continue;
            } catch (NumberFormatException nfe) {
                System.err.print(String.format("Error: reading domain boundary file %s\n", domBoundaryFile));
                bufferedReader.close();
                return domains;
            }
        }
        bufferedReader.close();
        
        return domains;
    }

}
