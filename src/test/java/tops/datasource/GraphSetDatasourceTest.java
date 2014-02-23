package tops.datasource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class GraphSetDatasourceTest {
	
	private class ArrayBackedDataSource implements GraphSetDatasource {
		private final Map<String, String[]> graphSets = new HashMap<String, String[]>();
		
		public ArrayBackedDataSource() {
			graphSets.put("CATH", new String[] { "1aaa NEEEC 1:2P2:3P", "1bbb NEhE 1:3Z" });
			graphSets.put("SCOP", new String[] { "1aa_ NEEEhC 1:2P2:3P", "1bb_ NEhE 1:3Z" });
		}
		
		@Override
		public List<String> getGraphSet(String setID) {
			return Arrays.asList(graphSets.get(setID));
		}
	}
	
	@Test
	public void testGet() {
		GraphSetDatasource arrayBackedSource = new ArrayBackedDataSource();
		for (String topsString : arrayBackedSource.getGraphSet("CATH")) {
			System.out.println(topsString);
		}
	}

}
