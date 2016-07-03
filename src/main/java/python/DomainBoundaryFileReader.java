package python;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class DomainBoundaryFileReader {

    public void readDomBoundaryFile(String DomBoundaryFile, Protein protein) throws IOException {

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
                    return;
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
                        return;
                    }

                    DomsFrom = CountDoms;
                    CountDoms += NDoms;

                    protein.numberOfDomains = CountDoms;

                    // read number of fragments listed on line /
                    CurrentToken = tokenizer.nextToken();

                    // read all the domains /
                    for (int i = DomsFrom; i < CountDoms; i++) {

                        DomainDefinition domain = new DomainDefinition(DomainDefinition.DomainType.SEGMENT_SET);
                        protein.addDomain(domain);
                        domain.domainCATHCode = String.format("%s%s%d",
                                protein.proteinCode, CodeChain,
                                (i - DomsFrom + 1) % 10);

                        CurrentToken = tokenizer.nextToken();
                        try {
                            nsegs = Integer.parseInt(CurrentToken);
                        } catch (Exception e) {
                            System.err.print(String.format("Error: reading domain boundary file %s\n", DomBoundaryFile));
                            bufferedReader.close();
                            return;
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
                                return;
                            }

                            CurrentToken = tokenizer.nextToken();
                            try {
                                res = Integer.parseInt(CurrentToken);
                            } catch (Exception e) {
                                System.err.print(String.format(
                                        "Error: reading domain boundary file %s\n",
                                        DomBoundaryFile));
                                bufferedReader.close();
                                return;
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
                                return;
                            }

                            CurrentToken = tokenizer.nextToken();
                            try {
                                res = Integer.parseInt(CurrentToken);
                            } catch (Exception e) {
                                System.err.print(String.format(
                                        "Error: reading domain boundary file %s\n",
                                        DomBoundaryFile));
                                bufferedReader.close();
                                return;
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
    }

}
