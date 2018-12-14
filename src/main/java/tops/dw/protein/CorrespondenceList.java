package tops.dw.protein;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maclean
 *
 * A list of lists of matching sses.
 * Note that an instance of this class is only relevant to a particular pattern.
 */
public class CorrespondenceList {
	
	private List<String> names;
	private List<int[]> correspondences;
	
	public CorrespondenceList() {
		this.names = new ArrayList<>();
		this.correspondences = new ArrayList<>();
	}
	
	/**
	 * Add a correspondence to the list. 
	 * 
	 * @param name the domain id or name of the structure.
	 * @param sseIndices an array of ints, one for each pattern sse.
	 */
	public void addCorrespondence(String name, int[] sseIndices) {
		this.names.add(name);
		this.correspondences.add(sseIndices);
	}
	
	/**
	 * Look up a correspondence using the name as a key.
	 * 
	 * @param name The unique id of the protein domain.
	 * @return An array of ints, one for each pattern sse.
	 */
	public int[] getCorrespondence(String name) {
		int index = this.names.indexOf(name);
		return (int[]) this.correspondences.get(index);
	}

}
