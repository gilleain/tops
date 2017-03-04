package tops.data.datasource;

import java.util.List;

/**
 * Source for a set of graphs, retrieved by name/ID of the set.
 * 
 * @author maclean
 *
 */
public interface GraphSetDatasource {

	List<String> getGraphSet(String setID);
}
