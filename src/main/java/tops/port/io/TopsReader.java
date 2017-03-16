package tops.port.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tops.port.model.Chain;
import tops.port.model.Protein;
import tops.port.model.SSE;
import tops.port.model.SSE.SSEType;

public class TopsReader {
    
    public static Protein createFromTopsFile(String filename) throws IOException {
        File topsFile = new File(filename);
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(topsFile));
        String lineBuffer = reader.readLine();
        while (lineBuffer != null) {
            lines.add(lineBuffer);
            lineBuffer = reader.readLine();
        }
        reader.close();

        String pdbid = filename.substring(0, 4);
        Protein protein = new Protein(pdbid);
        SSE sse = null;
        Chain chain = null;
        for (String line : lines) {
            if (line.charAt(0) == '#' || line.length() < 2) continue;
            String[] bits = line.split("\\s+"); 
            String keyword= bits[0];
            List<String> values = new ArrayList<String>();
            for (int i = 1; i < bits.length; i++) { values.add(bits[i]); }
            // print "keyword = [%s], value(s) = [%s]" % (keyword, values)
            if (values.isEmpty()) values = null;
            if (keyword.equals("DOMAIN_NUMBER")) {
                String cathcode = values.get(1);
                protein.setCode(cathcode.substring(0, 4));
                char chain_id = cathcode.charAt(4);
                char domain_id = cathcode.charAt(5);
                chain = new Chain(chain_id);
                protein.addChain(chain);
            } else if (keyword.equals("SecondaryStructureType")) {
                if (sse != null) chain.addSSE(sse);
                sse = new SSE(fromChar(values.get(0).charAt(0)));
            } else if (keyword.equals("FixedType") && values.size() > 0) {
                sse.setFixedType(values.get(0));
                //print "setting fixed type: %s to %i" % (values[0], sse.FixedType)
            } else if (keyword.equals("AxisStartPoint") || keyword.equals("AxisFinishPoint")) {
//                if (values == null) { 
//                    sse.__dict__[keyword] = null
//                    continue;
//                }
//                sse.__dict__[keyword] = [float(v) for v in values]
            } else {
                if (values != null && values.size() == 1 && !keyword.contains("BridgePartner") && !keyword.equals("Neighbour")) {
//                    value = values[0];
//                    if (value.isalnum() && !value.isdigit()) {
//                        sse.__dict__[keyword] = value;
//                    } else {
//                        try {
//                            sse.__dict__[keyword] = int(value);
//                        } catch (ValueError v) {
//                            sse.__dict__[keyword] = float(value);
//                        }
//                    }
                } else {
//                    sse.__dict__[keyword] = values;
                }
            }
        }
        chain.addSSE(sse);    // add the last one
        return protein;
    }
    
    // XXX TODO - alter the enum to add this?
    private static SSEType fromChar(char type) {
        switch (type) {
            case 'N': return SSEType.NTERMINUS;
            case 'C': return SSEType.CTERMINUS;
            case 'H': return SSEType.HELIX;
            case 'E': return SSEType.EXTENDED;
        }
        return SSEType.COIL;    // XXX?
    }

}
