package tops.engine;

public interface MatcherI {
	
	public boolean matches(PatternI pattern, PatternI instance);
	
	public void match(PatternI pattern, PatternI instance, MatchHandler matchHandler);

}
