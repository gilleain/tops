package tops.port;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import tops.port.model.DomainDefinition;
import tops.port.model.Protein;

public class DomainBoundaryFileReader {

    public List<DomainDefinition> readDomBoundaryFile(String DomBoundaryFile, Protein protein) throws IOException {

        List<DomainDefinition> domains = new ArrayList<DomainDefinition>();
        
        int nsegs, res;
        int NDoms, CountDoms, DomsFrom;
        char CodeChain, chain;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(DomBoundaryFile));

        String CurrentToken;

        CountDoms = 0;
        NDoms = 0;
        DomsFrom = 0;

        String line = bufferedReader.readLine();
        while (line != null) {

            if ((line.charAt(0) == '#') || (line.charAt(0) == ' ') || (line.charAt(0) == '\n')) {
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line);
            try {
                CurrentToken = tokenizer.nextToken();

                if (CurrentToken.length() < 6) {
                    System.err.print(String.format(
                            "Error: reading domain boundary file %s\n",
                            DomBoundaryFile));
                    bufferedReader.close();
                    return domains;
                }

                if (CurrentToken.substring(0, 4).equals(protein.getProteinCode())) {

                    CodeChain = CurrentToken.charAt(4);

                    // read number of domains listed on line /
                    CurrentToken = tokenizer.nextToken();
                    try {
                        NDoms = Integer.parseInt(CurrentToken);
                    } catch (Exception e) {
                        System.err.print(String.format("Error: reading domain boundary file %s\n", DomBoundaryFile));
                        bufferedReader.close();
                        return domains;
                    }

                    DomsFrom = CountDoms;
                    CountDoms += NDoms;

                    protein.numberOfDomains = CountDoms;

                    // read number of fragments listed on line /
                    CurrentToken = tokenizer.nextToken();

                    // read all the domains /
                    for (int i = DomsFrom; i < CountDoms; i++) {

                        DomainDefinition domain = new DomainDefinition(DomainDefinition.DomainType.SEGMENT_SET);
                        domains.add(domain);
                        domain.domainCATHCode = String.format("%s%s%d",
                                protein.getProteinCode(), CodeChain,
                                (i - DomsFrom + 1) % 10);

                        CurrentToken = tokenizer.nextToken();
                        try {
                            nsegs = Integer.parseInt(CurrentToken);
                        } catch (Exception e) {
                            System.err.print(String.format("Error: reading domain boundary file %s\n", DomBoundaryFile));
                            bufferedReader.close();
                            return domains;
                        }

                        domain.numberOfSegments = nsegs;

                        for (int j = 0; j < nsegs; j++) {

                            CurrentToken = tokenizer.nextToken();
                            try {
                                chain = CurrentToken.charAt(0);
                            } catch (Exception e) {
                                System.err.print(String.format(
                                        "Error: reading domain boundary file %s\n",
                                        DomBoundaryFile));
                                bufferedReader.close();
                                return domains;
                            }

                            CurrentToken = tokenizer.nextToken();
                            try {
                                res = Integer.parseInt(CurrentToken);
                            } catch (Exception e) {
                                System.err.print(String.format(
                                        "Error: reading domain boundary file %s\n",
                                        DomBoundaryFile));
                                bufferedReader.close();
                                return domains;
                            }

                            domain.segmentChains[0][j] = chain;
                            domain.segmentIndices[0][j] = res;

                            CurrentToken = tokenizer.nextToken();
                            try {
                                chain = CurrentToken.charAt(0);
                            } catch (Exception e) {
                                System.err.print(String.format(
                                        "Error: reading domain boundary file %s\n",
                                        DomBoundaryFile));
                                bufferedReader.close();
                                return domains;
                            }

                            CurrentToken = tokenizer.nextToken();
                            try {
                                res = Integer.parseInt(CurrentToken);
                            } catch (Exception e) {
                                System.err.print(String.format(
                                        "Error: reading domain boundary file %s\n",
                                        DomBoundaryFile));
                                bufferedReader.close();
                                return domains;
                            }
                            domain.segmentChains[1][j] = chain;
                            domain.segmentIndices[1][j] = res;
                        }
                    }
                }
            } catch (NoSuchElementException n) {
                continue;
            }
        }
        bufferedReader.close();
        
        return domains;
    }

}
