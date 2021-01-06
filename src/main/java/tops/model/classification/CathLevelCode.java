package tops.model.classification;

public enum CathLevelCode implements LevelCode {
	R("Root", -1),
	C("Class", 0),
	A("Architecture", 1),
	T("Topology", 2),
	H("Homologous Superfamily", 3),
	S("Sequence Family (S35)", 4),
	O("Orthologous Family (S60)", 5),
	L("Like Domain (S95)", 6),
	I("Identical domain (S100)", 7),
	D("Domain counter", 8);
	
	private final String name;
	private final int level;
	
	private CathLevelCode(String name, int level) {
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
