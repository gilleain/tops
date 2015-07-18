package tops.engine;

public interface PatternI {

	public int esize();
	
	public String getName();

	public Edge getEdge(int k);

	public Vertex getVertex(int i);

	public int vsize();

	public boolean subSequenceCompare(
			int i, int right, int j, int right2, PatternI d, boolean b);

	public void setMovedUpTo(int k);

	public boolean noEdges();

	public int getLastEdgeVertexPosition();

	public int indexOfFirstUnmatchedEdge();
	
	public boolean preProcess(PatternI target);
	
	public String[] getInsertStringArr(PatternI p, boolean flip);
	
	public String getInsertString(PatternI diagram);
	
	public boolean stringMatch(PatternI diagram, boolean flip);

	public String getVertexString(int last, int i, boolean flip);

	public int[] getMatches();

	public String getOutsertC(boolean b);

	public boolean verticesIncrease();

}
