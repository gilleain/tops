package tops.engine;

public class CountingHandler implements MatchHandler {
	
	private int count;

	@Override
	public void handle(PatternI pattern, PatternI instance) {
		count++;
	}
	
	public int getCount() {
		return count;
	}

}
