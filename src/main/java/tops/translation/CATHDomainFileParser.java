package tops.translation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tops.translation.model.Domain;

public class CATHDomainFileParser {

    private static Pattern domainPattern = Pattern
            .compile("((?:\\d)(?:\\s\\s[\\d\\w]\\s+\\d+\\s\\-\\s[\\w\\d]\\s+\\d+\\s\\-)+)+");

    private static Pattern segmentPattern = Pattern
            .compile("[\\d\\w]\\s+(\\d+)\\s\\-\\s[\\w\\d]\\s+(\\d+)\\s\\-");

    public static Map<String, Map<String, List<Domain>>> parseWholeFile(String filename) throws IOException {
        Map<String, Map<String, List<Domain>>> pdbChainDomainMap = new HashMap<String, Map<String, List<Domain>>>();

        String line;
        BufferedReader bufferer = new BufferedReader(new FileReader(filename));
        while ((line = bufferer.readLine()) != null) {
            if (line.substring(0, 1).equals("#")) {
                continue;
            }
            // analyze the line
            String pdbid = line.substring(0, 4);
            String chain = line.substring(4, 5);
            List<Domain> domains = CATHDomainFileParser.parseLine(line);

            // store the result
            if (pdbChainDomainMap.containsKey(pdbid)) {
                Map<String, List<Domain>> chainDomainMap = pdbChainDomainMap.get(pdbid);
                chainDomainMap.put(chain, domains);
            } else {
                Map<String, List<Domain>> chainDomainMap = new HashMap<String, List<Domain>>();
                chainDomainMap.put(chain, domains);
                pdbChainDomainMap.put(pdbid, chainDomainMap);
            }
        }
        bufferer.close();

        return pdbChainDomainMap;
    }

    public static Map<String, List<Domain>> parseUpToParticularID(String filename, String pdbid) throws IOException {
        Map<String, List<Domain>> chainDomainMap = new HashMap<String, List<Domain>>();

        String line;
        BufferedReader bufferer = new BufferedReader(new FileReader(filename));
        while ((line = bufferer.readLine()) != null) {
            if (line.substring(0, 1).equals("#")
                    || !line.substring(0, 4).equals(pdbid)) {
                continue;
            }
            // analyze the line
            String chain = line.substring(4, 5);
            List<Domain> domains = CATHDomainFileParser.parseLine(line);

            // store the result
            chainDomainMap.put(chain, domains);
            // System.err.println("Storing " + domains.size() + " domains for "
            // + pdbid + chain);
        }
        bufferer.close();

        return chainDomainMap;
    }

    public static List<Domain> parseLine(String line) {
        List<Domain> domains = new ArrayList<Domain>();

        Matcher domainMatcher = CATHDomainFileParser.domainPattern.matcher(line.substring(14));
        int domainID = 1;
        while (domainMatcher.find()) {
            Domain domain = new Domain(domainID);
            String domainString = domainMatcher.group(0);
            Matcher segmentMatcher = CATHDomainFileParser.segmentPattern.matcher(domainString);
            int numberOfSegments = Integer.parseInt(domainString.substring(0, 1));
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
}
