package tops.translation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CATHDomainFileParser {

    private static Pattern domainPattern = Pattern
            .compile("((?:\\d)(?:\\s\\s[\\d\\w]\\s+\\d+\\s\\-\\s[\\w\\d]\\s+\\d+\\s\\-)+)+");

    private static Pattern segmentPattern = Pattern
            .compile("[\\d\\w]\\s+(\\d+)\\s\\-\\s[\\w\\d]\\s+(\\d+)\\s\\-");

    public static HashMap parseWholeFile(String filename) throws IOException {
        HashMap pdbChainDomainMap = new HashMap();

        String line;
        BufferedReader bufferer = new BufferedReader(new FileReader(filename));
        while ((line = bufferer.readLine()) != null) {
            if (line.substring(0, 1).equals("#")) {
                continue;
            }
            // analyze the line
            String pdbid = line.substring(0, 4);
            String chain = line.substring(4, 5);
            ArrayList domains = CATHDomainFileParser.parseLine(line);

            // store the result
            if (pdbChainDomainMap.containsKey(pdbid)) {
                HashMap chainDomainMap = (HashMap) pdbChainDomainMap.get(pdbid);
                chainDomainMap.put(chain, domains);
            } else {
                HashMap chainDomainMap = new HashMap();
                chainDomainMap.put(chain, domains);
                pdbChainDomainMap.put(pdbid, chainDomainMap);
            }
        }

        return pdbChainDomainMap;
    }

    public static HashMap parseUpToParticularID(String filename, String pdbid)
            throws IOException {
        HashMap chainDomainMap = new HashMap();

        String line;
        BufferedReader bufferer = new BufferedReader(new FileReader(filename));
        while ((line = bufferer.readLine()) != null) {
            if (line.substring(0, 1).equals("#")
                    || !line.substring(0, 4).equals(pdbid)) {
                continue;
            }
            // analyze the line
            String chain = line.substring(4, 5);
            ArrayList domains = CATHDomainFileParser.parseLine(line);

            // store the result
            chainDomainMap.put(chain, domains);
            // System.err.println("Storing " + domains.size() + " domains for "
            // + pdbid + chain);
        }

        return chainDomainMap;
    }

    public static ArrayList parseLine(String line) {
        ArrayList domains = new ArrayList();

        Matcher domainMatcher = CATHDomainFileParser.domainPattern.matcher(line.substring(14));
        int domainID = 1;
        while (domainMatcher.find()) {
            Domain domain = new Domain(domainID);
            String domainString = domainMatcher.group(0);
            Matcher segmentMatcher = CATHDomainFileParser.segmentPattern.matcher(domainString);
            int numberOfSegments = Integer.parseInt(domainString
                    .substring(0, 1));
            int segmentCount = 0;
            while (segmentMatcher.find() && segmentCount < numberOfSegments) {
                int start = Integer.parseInt(segmentMatcher.group(1));
                int end = Integer.parseInt(segmentMatcher.group(2));
                domain.addSegment(start, end);
                segmentCount++;
            }
            domains.add(domain);
            domainID++;
        }
        return domains;
    }

    public static void main(String[] args) {
        try {
            HashMap pdbChainDomainMap = CATHDomainFileParser
                    .parseWholeFile(args[0]);
            Iterator pdbidItr = pdbChainDomainMap.keySet().iterator();
            while (pdbidItr.hasNext()) {
                String pdbID = (String) pdbidItr.next();
                HashMap chainDomainMap = (HashMap) pdbChainDomainMap.get(pdbID);
                Iterator chainItr = chainDomainMap.keySet().iterator();
                while (chainItr.hasNext()) {
                    String chainID = (String) chainItr.next();
                    ArrayList domains = (ArrayList) chainDomainMap.get(chainID);
                    Iterator domainItr = domains.iterator();
                    while (domainItr.hasNext()) {
                        Domain domain = (Domain) domainItr.next();
                        System.out.println(pdbID + chainID + " " + domain);
                    }
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
    }
}
