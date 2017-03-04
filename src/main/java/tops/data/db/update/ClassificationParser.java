package tops.data.db.update;

import java.util.List;
import java.util.Map;

/**
 * Parse a domain classification file.
 * 
 * @author maclean
 *
 */
public interface ClassificationParser {

    /**
     * @param lines the lines of the file
     * @return a map between domain ids and the classification identifiers
     */
    public Map<String, String> parseDomainList(List<String> lines);
    
}
