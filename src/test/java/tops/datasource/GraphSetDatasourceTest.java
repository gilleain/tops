package tops.datasource;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import tops.data.datasource.ArrayGraphDatasource;

public class GraphSetDatasourceTest {
	
	@Test
	public void testGet() {
		Map<String, String[]> data = new HashMap<String, String[]>();
		data.put("CATH", new String[] { "1aaa NEEEC 1:2P2:3P", "1bbb NEhE 1:3Z" });
		data.put("SCOP", new String[] { "1aa_ NEEEhC 1:2P2:3P", "1bb_ NEhE 1:3Z" });
		ArrayGraphDatasource arrayBackedSource = new ArrayGraphDatasource(data);
		for (String topsString : arrayBackedSource.getGraphSet("CATH")) {
			System.out.println(topsString);
		}
	}

}
