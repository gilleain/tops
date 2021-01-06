package tops.model.classification;

public enum ScopLevelCode implements LevelCode {
	
    ROOT("ROOT", -1),
    CL("CLass", 0),
    CF("Fold", 1),
    SF("Superfamily", 2),
    FA("Family", 3),
    DM("Protein", 4),
    SP("Species", 5),
    PX("Domain", 6);
	
	private final String name;
	private final int level;
	
	private ScopLevelCode(String name, int level) {
		this.name = name;
		this.level = level;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getLevel() {
		return this.level;
	}

}
