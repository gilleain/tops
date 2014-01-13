package tops.translation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tops2String {

    private Matcher domain_num = (Pattern
//            .compile("DOMAIN_NUMBER\\s\\S+\\s(\\w+)\\s.*")).matcher("");
              .compile("DOMAIN_NUMBER\\s\\d+\\s(\\w+).*")).matcher("");

    private Matcher sse_type = (Pattern.compile("SecondaryStructureType\\s(.)"))
            .matcher("");

    private Matcher directions = (Pattern.compile("Direction\\s(.)"))
            .matcher("");

    private Matcher bridgeparts = (Pattern.compile("BridgePartner\\s(.*)"))
            .matcher("");

    private Matcher bridgetypes = (Pattern.compile("BridgePartnerType\\s(.*)"))
            .matcher("");

    private Matcher symbolnums = (Pattern.compile("SymbolNumber\\s(.*)"))
            .matcher("");

    private Matcher chirals = (Pattern.compile("Chirality.([-\\d]+)"))
            .matcher("");

    private Matcher fill = (Pattern.compile("Fill.+")).matcher("");

    private String topsDirectoryPath;

    public Tops2String(String topsDirectoryPath) {
        this.topsDirectoryPath = topsDirectoryPath;
    }

    public String[] convert(String tops_file_name, String replacement_pdbid,
            String classificationScheme) throws IOException {
        BufferedReader bufferedReader = 
            new BufferedReader(new FileReader(new File(this.topsDirectoryPath, tops_file_name)));
        String line = new String();
        HashMap<String, HashMap<String, HashMap<Integer, String>>> domains = new HashMap<String, HashMap<String, HashMap<Integer, String>>>();
        String current_domain = new String();
        int current_pos = 0;

        while ((line = bufferedReader.readLine()) != null) {
            this.domain_num.reset(line);
            this.sse_type.reset(line);
            this.directions.reset(line);
            this.bridgeparts.reset(line);
            this.bridgetypes.reset(line);
            this.symbolnums.reset(line);
            this.chirals.reset(line);
            this.fill.reset(line);

            if (this.domain_num.matches()) {
                current_domain = this.domain_num.group(1);
                current_pos = 0;
            }
            if (this.sse_type.matches()) {
                this.storeData(domains, current_domain, current_pos,
                        "SSE_TYPE", this.sse_type.group(1));
            }
            if (this.directions.matches()) {
                this.storeData(domains, current_domain, current_pos,
                        "DIRECTIONS", this.directions.group(1));
            }
            if (this.bridgeparts.matches()) {
                this.storeData(domains, current_domain, current_pos,
                        "BRIDGEPARTS", this.bridgeparts.group(1));
            }
            if (this.bridgetypes.matches()) {
                this.storeData(domains, current_domain, current_pos,
                        "BRIDGETYPES", this.bridgetypes.group(1));
            }
            if (this.symbolnums.matches()) {
                this.storeData(domains, current_domain, current_pos,
                        "SYMBOLNUMS", this.symbolnums.group(1));
            }
            if (this.chirals.matches()) {
                this.storeData(domains, current_domain, current_pos, "CHIRALS",
                        this.chirals.group(1));
            }
            if (this.fill.matches()) {
                current_pos++;
            }
        }
        bufferedReader.close();

        return this.getData(domains, replacement_pdbid, classificationScheme);
    }

    private String[] getData(HashMap<String, HashMap<String, HashMap<Integer, String>>> domains, String replacement_pdbid, String scheme) {
        // now, go through the map, getting the data
        Set<String> keys = domains.keySet();
        String[] domain_strings = new String[keys.size()];
        Iterator<String> itr = keys.iterator();

        // for stepping through the domain_strings array
        int k = 0;

        while (itr.hasNext()) {
            String domain_id = (String) itr.next();
//            System.err.println("domain id " + domain_id);

            // replace the pdbid with another name if requested
            String name = "";

            if (!replacement_pdbid.equals("")) {
                System.err.println(domain_id);
                name = replacement_pdbid + "_" + domain_id.substring(4, 6);
            } else {

                // depending on the classificationScheme string, choose the name
                if (scheme.equals("CATH")) {
                    // actually, the default is cath
                    name = domain_id;
                } else if (scheme.equals("SCOP")) {
                    char chain = domain_id.charAt(4);
                    char domID = domain_id.charAt(5);

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
                    char scopChain = (Character.isDigit(chain)) ? 
                            ((chain == '0') ? '_' : chain) : Character.toLowerCase(chain);
                    char scopDomID = (Character.isDigit(domID)) ? 
                            ((domID == '0') ? '_' : domID) : Character.toLowerCase(domID);
                    name = "d" + domain_id.substring(0, 4) + scopChain + scopDomID;
                } else {
                    System.err.println("Unknown scheme " + scheme
                            + " not 'CATH' or 'SCOP'");
                }
            }

            // convert the data
            HashMap<?, ?> domain_map = (HashMap<?, ?>) domains.get(domain_id);

            StringBuffer topsString = new StringBuffer();
            HashMap<Integer, HashMap<Integer, String>> bonds = new HashMap<Integer, HashMap<Integer, String>>();
            boolean lookingForChiralPartner = false;
            String last_chiral_flag = new String();
            char last_symbol = 'N';
            Integer lastVertex = new Integer(0);

            HashMap<?, ?> sse_type_map = (HashMap<?, ?>) domain_map.get("SSE_TYPE");
            // System.err.println(sse_type_map);
            HashMap<?, ?> directions_map = (HashMap<?, ?>) domain_map.get("DIRECTIONS");
            // System.err.println(directions_map);
            HashMap<?, ?> bridge_parts_map = (HashMap<?, ?>) domain_map.get("BRIDGEPARTS");
            // System.err.println(bridge_parts_map);
            HashMap<?, ?> bridge_types_map = (HashMap<?, ?>) domain_map.get("BRIDGETYPES");
            // System.err.println(bridge_types_map);
//            HashMap symbolnums_map = (HashMap) domain_map.get("SYMBOLNUMS");
            // System.err.println(symbolnums_map);
            HashMap<?, ?> chirals_map = (HashMap<?, ?>) domain_map.get("CHIRALS");
            // System.err.println(chirals_map);

            topsString.append(name).append(' ');

            for (int i = 0; i < sse_type_map.keySet().size(); i++) {
                Integer currentVertex = new Integer(i);
                String type = (String) sse_type_map.get(currentVertex);
                String dir = (String) directions_map.get(currentVertex);
                char type_as_char = type.charAt(0);
                char symbol = (dir.equals("D")) ? Character
                        .toLowerCase(type_as_char) : type_as_char;
                topsString.append(symbol);

                HashMap<Integer, String> partner_type = (HashMap<Integer, String>) bonds.get(currentVertex);
                if (partner_type == null)
                    partner_type = new HashMap<Integer, String>();

                if (bridge_parts_map != null) {
                    String bridge_parts_string = (String) bridge_parts_map.get(currentVertex);
                    String bridge_types_string = (String) bridge_types_map.get(currentVertex);

                    // do hbond partners
                    if (bridge_parts_string != null) {
                        StringTokenizer partsTokenizer = new StringTokenizer(bridge_parts_string);
                        StringTokenizer typesTokenizer = new StringTokenizer(bridge_types_string);

                        while ((partsTokenizer.hasMoreTokens())
                                && (typesTokenizer.hasMoreTokens())) {
                            Integer partner = new Integer(partsTokenizer.nextToken());
                            String edge_type = typesTokenizer.nextToken();
                            partner_type.put(partner, edge_type);
                        }
                        bonds.put(currentVertex, partner_type);
                    }
                }

                // do chirals
                String chiral_flag = (String) chirals_map.get(currentVertex);

                if ((lookingForChiralPartner) && (last_symbol == symbol)) {
                    String chiral_type = (last_chiral_flag.equals("-1")) ? "L" : "R";
                    HashMap<Integer, String> partners = (HashMap<Integer, String>) bonds.get(lastVertex);
                    if (partners == null)
                        partners = new HashMap<Integer, String>();
                    
                    //if it has an edge, it must be 'P'
                    if (partners.containsKey(currentVertex)) { 
                        String hbond = (String) partners.get(currentVertex);
                        if (hbond.equals("A")) {
                            System.err.println("problem : trying to add chiral to antiparallel edge!");
                        }
                        if (chiral_type.equals("L")) {
                            chiral_type = "X";
                        }
                        if (chiral_type.equals("R")) {
                            chiral_type = "Z";
                        }
                    }
                    partners.put(currentVertex, chiral_type);
                    bonds.put(lastVertex, partners);
                    lookingForChiralPartner = false;
                }

                // if we see a chiral flag, store the details of the vertex, and
                // start the search
                if (!chiral_flag.equals("0")) {
                    lastVertex = currentVertex;
                    last_chiral_flag = chiral_flag;
                    last_symbol = symbol;
                    lookingForChiralPartner = true;
                }
            }
            topsString.append(' '); // all-important space!

            // now, turn the bond map-map into an edge string

            // System.err.println(bonds);
            TreeSet<Integer> leftHandEnds = new TreeSet<Integer>(bonds.keySet());
            Iterator<Integer> lefts = leftHandEnds.iterator();
            while (lefts.hasNext()) {
                Integer leftHandEnd = (Integer) lefts.next();
                HashMap<Integer, String> otherEnds = bonds.get(leftHandEnd);

                TreeSet<Integer> rightHandEnds = new TreeSet<Integer>(otherEnds.keySet());
                for (Integer rightHandEnd : rightHandEnds) {
                    // only accept edges i:j where i < j
                    if (leftHandEnd.compareTo(rightHandEnd) < 0) { 
                        String e_type = (String) otherEnds.get(rightHandEnd);
                        topsString.append(leftHandEnd);
                        topsString.append(':');
                        topsString.append(rightHandEnd).append(e_type);
                    }
                }
            }

            domain_strings[k++] = topsString.toString();
        }
        return domain_strings;
    }

    public void storeData(HashMap<String, HashMap<String, HashMap<Integer, String>>> map, String domain, int pos, String key, String value) {
        // first, get the values map for a particular domain (create if !exists)
        HashMap<String, HashMap<Integer, String>> domainMap = (HashMap<String, HashMap<Integer, String>>) map.get(domain);
        if (domainMap == null) {
            domainMap = new HashMap<String, HashMap<Integer, String>>();
            map.put(domain, domainMap);
        }

        // now get the map of values for the key we want
        HashMap<Integer, String> subMap = (HashMap<Integer, String>) domainMap.get(key);
        if (subMap == null) {
            subMap = new HashMap<Integer, String>();
            domainMap.put(key, subMap);
        }

        // finally, put value into the map
        Integer position = new Integer(pos);
        subMap.put(position, value);
    }

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("-h")) {
            System.err.println(
            "Usage : java tops.translation.Tops2String <filename> <string_name> <CATH|SCOP>");
            System.err.println(
            "or    : java tops.translation.Tops2String -d <directoryname> <CATH|SCOP>");
            System.exit(0);
        }

        // 'directory' mode - convert every .tops file in a directory, no renaming
        if (args[0].equals("-d")) {
            if (args.length == 1) {
                System.err.println("java tops.translation.Tops2String -d <directoryname> <CATH|SCOP>");
                System.exit(0);
            }
            File topsDirectory = new File(args[1]);
            if (topsDirectory.isDirectory()) {
                Tops2String t2s = new Tops2String(args[1]);
                String[] fileList = topsDirectory.list();
                for (int i = 0; i < fileList.length; i++) {
                    String topsFileName = fileList[i];
                    try {
                        String[] results = t2s.convert(topsFileName, "", args[2]);
                        for (int j = 0; j < results.length; j++) {
                            System.out.println(results[j]);
                        }
                    } catch (IOException ioe) {
                        System.err.println(ioe);
                    }
                }
            } else {
                System.err.println("java tops.translation.Tops2String -d <directoryname>");
                System.exit(0);
            }

            // 'single' mode - convert only the file supplied as an argument,
            // renaming if the second argument is not "", scheme in args[2]
        } else {
            Tops2String t2s = new Tops2String(".");
            try {
                String[] results = t2s.convert(args[0], args[1], args[2]);
                for (int i = 0; i < results.length; i++) {
                    System.out.println(results[i]);
                }
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }
    }
}
