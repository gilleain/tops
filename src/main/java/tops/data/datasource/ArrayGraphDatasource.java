package tops.data.datasource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayGraphDatasource implements GraphSetDatasource {
	
	private final Map<String, String[]> graphSets = new HashMap<String, String[]>();
	
	public ArrayGraphDatasource(Map<String, String[]> graphSets) {
		this.graphSets.putAll(graphSets);
	}
	
	public void addSet(String key, String[] members) {
		graphSets.put(key, members);
	}
	
	@Override
	public List<String> getGraphSet(String setID) {
		return Arrays.asList(graphSets.get(setID));
	}
}
