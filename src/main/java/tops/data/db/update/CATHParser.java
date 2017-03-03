package tops.data.db.update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CATHParser implements ClassificationParser {

    @Override
    public Map<String, String> parseDomainList(List<String> lines) {
        Map<String, String> data = new HashMap<String, String>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.substring(0, 1).equals("#")) {
                continue;
            } else {
                String[] bits = line.split("\\s+");
                String domainID = bits[0];

                // ignore superseded entries
                String resolution = bits[9];
                if (resolution.equals("1000.000")) {
                    continue;
                }

                // concatenate the CATH number
                String CATHNumber = "";
                for (int j = 1; j < 8; j++) {
                    CATHNumber += bits[j];
                    CATHNumber += ".";
                }
                data.put(domainID, CATHNumber);
            }
        }
        return data;
    }

}
