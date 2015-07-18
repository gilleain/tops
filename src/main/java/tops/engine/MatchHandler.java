package tops.engine;

/**
 * Handle a match between a pattern and an instance.
 * 
 * @author maclean
 *
 */
public interface MatchHandler {
	
	public void handle(PatternI pattern, PatternI instance);

}
