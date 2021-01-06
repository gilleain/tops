package tops.model.classification;

/**
 * Interface for levels of a hierarchy. So, for example CATH is C=1, A=2, T=3, H=4 ... 
 * 
 * @author gilleain
 *
 */
public interface LevelCode {
	
	/**
	 * @return the position of the level in the hierarchy
	 */
	int getLevel();
	
	/**
	 * @return the name of the level
	 */
	String getName();

}
