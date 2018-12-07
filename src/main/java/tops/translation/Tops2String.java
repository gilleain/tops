package tops.translation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tops2String {

    private Matcher domainNum = (Pattern
              .compile("DOMAIN_NUMBER\\s\\d+\\s(\\w+).*")).matcher("");

    private Matcher sseType = (Pattern.compile("SecondaryStructureType\\s(.)")).matcher("");

    private Matcher directions = (Pattern.compile("Direction\\s(.+)")).matcher("");

    private Matcher bridgeparts = (Pattern.compile("BridgePartner\\s(.*)")).matcher("");

    private Matcher bridgetypes = (Pattern.compile("BridgePartnerType\\s(.*)")).matcher("");

    private Matcher symbolnums = (Pattern.compile("SymbolNumber\\s(.*)")).matcher("");

    private Matcher chirals = (Pattern.compile("Chirality.([-\\d]+)")).matcher("");

    private Matcher fill = (Pattern.compile("Fill.+")).matcher("");

    private File topsDirectory;

    public Tops2String(String topsDirectoryPath) {
        this(new File(topsDirectoryPath));
    }
    
    public Tops2String(File topsDirectory) {
    	this.topsDirectory = topsDirectory;
    }

    public String[] convert(String topsFileName, String replacementPdbId, String classificationScheme) throws IOException {
        BufferedReader bufferedReader = 
            new BufferedReader(new FileReader(new File(this.topsDirectory, topsFileName)));
        String line = "";
        Map<String, Map<String, Map<Integer, String>>> domains = new HashMap<>();
        String currentDomain = "";
        int currentPos = 0;

        while ((line = bufferedReader.readLine()) != null) {
            this.domainNum.reset(line);
            this.sseType.reset(line);
            this.directions.reset(line);
            this.bridgeparts.reset(line);
            this.bridgetypes.reset(line);
            this.symbolnums.reset(line);
            this.chirals.reset(line);
            this.fill.reset(line);

            if (this.domainNum.matches()) {
                currentDomain = this.domainNum.group(1);
                currentPos = 0;
            }
            if (this.sseType.matches()) {
                this.storeData(domains, currentDomain, currentPos,
                        "SSE_TYPE", this.sseType.group(1));
            }
            if (this.directions.matches()) {
                this.storeData(domains, currentDomain, currentPos,
                        "DIRECTIONS", this.directions.group(1));
            }
            if (this.bridgeparts.matches()) {
                this.storeData(domains, currentDomain, currentPos,
                        "BRIDGEPARTS", this.bridgeparts.group(1));
            }
            if (this.bridgetypes.matches()) {
                this.storeData(domains, currentDomain, currentPos,
                        "BRIDGETYPES", this.bridgetypes.group(1));
            }
            if (this.symbolnums.matches()) {
                this.storeData(domains, currentDomain, currentPos,
                        "SYMBOLNUMS", this.symbolnums.group(1));
            }
            if (this.chirals.matches()) {
                this.storeData(domains, currentDomain, currentPos, "CHIRALS",
                        this.chirals.group(1));
            }
            if (this.fill.matches()) {
                currentPos++;
            }
        }
        bufferedReader.close();

        return this.getData(domains, replacementPdbId, classificationScheme);
    }

    private String[] getData(Map<String, Map<String, Map<Integer, String>>> domains, String replacementPdbId, String scheme) {
        // now, go through the map, getting the data
        Set<String> keys = domains.keySet();
        String[] domainStrings = new String[keys.size()];
        Iterator<String> itr = keys.iterator();

        // for stepping through the domain_strings array
        int k = 0;

        while (itr.hasNext()) {
            String domainId = itr.next();

            // replace the pdbid with another name if requested
            String name = "";

            if (!replacementPdbId.equals("")) {
                name = replacementPdbId + "_" + domainId.substring(4, 6);
            } else {

                // depending on the classificationScheme string, choose the name
                if (scheme.equals("CATH")) {
                    // actually, the default is cath
                    name = domainId;
                } else if (scheme.equals("SCOP")) {
                    char chain = domainId.charAt(4);
                    char domID = domainId.charAt(5);

                    /**
                     * BEHOLD AND TREMBLE, MORTAL! For this is the terrible
                     * logic of converting a cath id to a scop id. Note that: 1)
                     * There are 23 chains with the id '0' in scop1.69. These
                     * cannot be distinguished from those in cath that have been
                     * converted to '0' from ' '. 2) There are 981 domains with
                     * "." as the chain in scop1.69 - these are multi-chain
                     * domains. 3) Yes there are domains with letter ids -
                     * d1n7daa, d1g2c.b, and d1g2c.c - I blame The Russian.
                     * (oh..okay, it's because 1n7d has 9 other domains. never
                     * mind)
                     */
                    char chainChar = (chain == '0') ? '_' : chain;
                    char domainChar = (domID == '0') ? '_' : domID;
                    char scopChain = (Character.isDigit(chain)) ? chainChar : Character.toLowerCase(chain);
                    char scopDomID = (Character.isDigit(domID)) ? domainChar : Character.toLowerCase(domID);
                    name = "d" + domainId.substring(0, 4) + scopChain + scopDomID;
                } else {
                    System.err.println("Unknown scheme " + scheme
                            + " not 'CATH' or 'SCOP'");
                }
            }

            // convert the data
            Map<String, Map<Integer, String>> domainMap = domains.get(domainId);

            StringBuilder topsString = new StringBuilder();
            Map<Integer, Map<Integer, String>> bonds = new HashMap<>();
            boolean lookingForChiralPartner = false;
            String lastChiralFlag = "";
            char lastSymbol = 'N';
            Integer lastVertex = 0;

            Map<Integer, String> sseTypeMap = domainMap.get("SSE_TYPE");
            Map<Integer, String> directionsMap = domainMap.get("DIRECTIONS");
            Map<Integer, String> bridgePartMap = domainMap.get("BRIDGEPARTS");
            Map<Integer, String> bridgeTypesMap = domainMap.get("BRIDGETYPES");
            Map<Integer, String> chiralsMap = domainMap.get("CHIRALS");

            topsString.append(name).append(' ');

            for (int i = 0; i < sseTypeMap.keySet().size(); i++) {
                Integer currentVertex = i;
                String type = sseTypeMap.get(currentVertex);
                String dir = directionsMap.get(currentVertex);
                char typeAsChar = type.charAt(0);
                char symbol = (dir.equals("D")) ? Character.toLowerCase(typeAsChar) : typeAsChar;
                topsString.append(symbol);

                Map<Integer, String> partnerType = bonds.get(currentVertex);
                if (partnerType == null)
                    partnerType = new HashMap<>();

                if (bridgePartMap != null) {
                    String bridgePartsString = bridgePartMap.get(currentVertex);
                    String bridgeTypesString = bridgeTypesMap.get(currentVertex);

                    // do hbond partners
                    if (bridgePartsString != null) {
                        StringTokenizer partsTokenizer = new StringTokenizer(bridgePartsString);
                        StringTokenizer typesTokenizer = new StringTokenizer(bridgeTypesString);

                        while ((partsTokenizer.hasMoreTokens())
                                && (typesTokenizer.hasMoreTokens())) {
                            Integer partner = new Integer(partsTokenizer.nextToken());
                            partnerType.put(partner, typesTokenizer.nextToken());
                        }
                        bonds.put(currentVertex, partnerType);
                    }
                }

                // do chirals
                String chiralFlag = chiralsMap.get(currentVertex);

                if ((lookingForChiralPartner) && (lastSymbol == symbol)) {
                    String chiralType = (lastChiralFlag.equals("-1")) ? "L" : "R";
                    Map<Integer, String> partners = bonds.get(lastVertex);
                    if (partners == null)
                        partners = new HashMap<>();
                    
                    //if it has an edge, it must be 'P'
                    if (partners.containsKey(currentVertex)) { 
                        String hbond = partners.get(currentVertex);
                        if (hbond.equals("A")) {
                            System.err.println("problem : trying to add chiral to antiparallel edge!");
                        }
                        if (chiralType.equals("L")) {
                            chiralType = "X";
                        }
                        if (chiralType.equals("R")) {
                            chiralType = "Z";
                        }
                    }
                    partners.put(currentVertex, chiralType);
                    bonds.put(lastVertex, partners);
                    lookingForChiralPartner = false;
                }

                // if we see a chiral flag, store the details of the vertex, and
                // start the search
                if (!chiralFlag.equals("0")) {
                    lastVertex = currentVertex;
                    lastChiralFlag = chiralFlag;
                    lastSymbol = symbol;
                    lookingForChiralPartner = true;
                }
            }
            topsString.append(' '); // all-important space!

            // now, turn the bond map-map into an edge string

            TreeSet<Integer> leftHandEnds = new TreeSet<>(bonds.keySet());
            Iterator<Integer> lefts = leftHandEnds.iterator();
            while (lefts.hasNext()) {
                Integer leftHandEnd = lefts.next();
                Map<Integer, String> otherEnds = bonds.get(leftHandEnd);

                TreeSet<Integer> rightHandEnds = new TreeSet<>(otherEnds.keySet());
                for (Integer rightHandEnd : rightHandEnds) {
                    // only accept edges i:j where i < j
                    if (leftHandEnd.compareTo(rightHandEnd) < 0) { 
                        String eType = otherEnds.get(rightHandEnd);
                        topsString.append(leftHandEnd);
                        topsString.append(':');
                        topsString.append(rightHandEnd).append(eType);
                    }
                }
            }

            domainStrings[k++] = topsString.toString();
        }
        return domainStrings;
    }

    public void storeData(Map<String, Map<String, Map<Integer, String>>> map, String domain, int pos, String key, String value) {
        // first, get the values map for a particular domain (create if !exists)
        Map<String, Map<Integer, String>> domainMap = map.get(domain);
        if (domainMap == null) {
            domainMap = new HashMap<>();
            map.put(domain, domainMap);
        }

        // now get the map of values for the key we want
        Map<Integer, String> subMap = domainMap.get(key);
        if (subMap == null) {
            subMap = new HashMap<>();
            domainMap.put(key, subMap);
        }

        // finally, put value into the map
        subMap.put(pos, value);
    }
}
