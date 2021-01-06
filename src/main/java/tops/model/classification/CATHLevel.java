package tops.model.classification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CATHLevel {

    /** The level name (C, A, T, H, etc) for this level */
    private CathLevelCode name;

    /** The level name for the children */
    private CathLevelCode childLevelName;

    /** The code for this level - the code for the T level for 1.10.20 is 20 */
    private int code;

    /** The domain ID of the representative of this level */
    private String repName;

    /** A list of children **/
    private List<CATHLevel> children;
    
    private List<String> domainIds;

    /**
     * Create a new level.
     * 
     * @param name
     *            the flag for this level type (C, A, T, H, etc)
     * @param code
     *            the numerical id for this level (10, 100, 530, etc)
     * @param repName
     *            the domain id of the level representative
     */

    public CATHLevel(CathLevelCode name, int code, String repName) {
        this.name = name;
        this.code = code;
        this.repName = repName;
        this.children = new ArrayList<>();

        // determine the name of the next level
        this.childLevelName = CathLevelCode.D;
        if (this.name != CathLevelCode.D) {
        	int index = name.ordinal();
            this.childLevelName = CathLevelCode.values()[index + 1];
        }
    }
    
    public CathLevelCode getName() {
    	return this.name;
    }

    /**
     * Recursively add the data from the cathNumber into this level and its
     * children.
     * 
     * @param cathNumber
     *            the data to be added
     */

    public void addCATHNumber(CATHNumber cathNumber) {

        // adding the domain name is the last step, stop recursing
        if (this.childLevelName == CathLevelCode.D) {
        	if (domainIds == null) {
        		domainIds = new ArrayList<>();
        	}
            this.domainIds.add(cathNumber.getDomainID());
            return;
        }

        // try to find a child level with the appropriate code
        int childLevelCode = cathNumber.getCodeForName(this.childLevelName);
        CATHLevel childLevel = this.getChildLevel(childLevelCode);

        // create new levels as appropriate, making this cathNumber the rep
        // (assumes an ordering on inputs)
        if (childLevel == null) {
            childLevel = new CATHLevel(
            		this.childLevelName, childLevelCode, cathNumber.getDomainID());
            this.children.add(childLevel);
        }

        // now continue to traverse the tree
        childLevel.addCATHNumber(cathNumber);
    }

    /**
     * Run through the children, trying to find one with the specified code.
     * 
     * @param code
     *            the desired code.
     * @return a level, or null if none is found with a code like
     *         <code>code</code>.
     */

    public CATHLevel getChildLevel(int code) {
        for (int i = 0; i < this.children.size(); i++) {
            CATHLevel child = (CATHLevel) this.children.get(i);
            if (child.hasCode(code)) {
                return child;
            }
        }

        return null;
    }
    
    public List<CATHLevel> getChildren() {
    	return this.children;
    }

    /**
     * Check this level's code against the supplied code
     * 
     * @param code
     *            a code to check
     * @return true if the code matches this code
     */
    public boolean hasCode(int code) {
        return this.code == code;
    }

    /**
     * Lookup a domain in the level's children to find out its rep status.
     * 
     * @param cathNumber
     *            the domain we are searching for.
     */
    public CathLevelCode getHighestRep(CATHNumber cathNumber) {
        // the first level we reach with this domainID as a rep, we return
        if (this.name != CathLevelCode.R && this.repName.equals(cathNumber.getDomainID())) {
            return this.name;
        }

        // no more reps
        if (this.childLevelName == CathLevelCode.D) {
            return CathLevelCode.D;
        }

        // we are making the dangerous assumption that the tree actually
        // contains the domain we are searching for!
        int childLevelCode = cathNumber.getCodeForName(this.childLevelName);
        CATHLevel childLevel = this.getChildLevel(childLevelCode);

        // continue to search
        if (childLevel == null) {
            System.err.println(cathNumber + " " + this);
            return CathLevelCode.R;	// XXX
        } else {
            return childLevel.getHighestRep(cathNumber);
        }
    }

    /**
     * Lookup a domain in the level's children to find out its rep status.
     * 
     * @param cathNumber the domain we are searching for.
     * @param repInts the list of ints we are assembling.
     */

    public void getReps(CATHNumber cathNumber, List<LevelCode> repInts) {
        // add a rep to the list if necessary
        if (this.name != CathLevelCode.R && this.repName.equals(cathNumber.getDomainID())) {
            repInts.add(this.name);
        }

        // no more reps
        if (this.childLevelName == CathLevelCode.D) {
            return;
        }

        // we are making the dangerous assumption that the tree actually
        // contains the domain we are searching for!
        int childLevelCode = cathNumber.getCodeForName(this.childLevelName);
        CATHLevel childLevel = this.getChildLevel(childLevelCode);

        // continue to search
        if (childLevel == null) {
            System.err.println(cathNumber + " " + this);
        } else {
            childLevel.getReps(cathNumber, repInts);
        }
    }

    /**
     * Print out the level and its children to the stream <code>out</code>.
     * 
     * @param out the PrintStream to write to (eg: System.out)
     */
    public void printToStream(PrintStream out) {
        // pretty-print the level by tabbing
        for (int l = 0; l < this.name.ordinal(); l++) {
            out.print("\t");
        }

        if (this.childLevelName != CathLevelCode.D) {
            out.println(this);
            for (int i = 0; i < this.children.size(); i++) {
                ((CATHLevel) this.children.get(i)).printToStream(out);
            }
        } else {
            out.println(this + " " + this.domainIds);
        }
    }
    
    public static CATHLevel fromFile(String filename, CathLevelCode cathLevelConstant, String levelName) throws IOException {
        String line;
        // XXX what is the code here?
        int code = 0;
        CATHLevel level = new CATHLevel(cathLevelConstant, code, levelName);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            while ((line = bufferedReader.readLine()) != null) {
                Rep rep = new Rep(levelName, line);
                level.addRep(rep);
            }
        }
        return level;
    }

    @Override
    public String toString() {
        if (this.name != CathLevelCode.R) {
            return this.name.getName() + " " + this.code + " rep : " + this.repName;
        }
        return "ROOT";
    }

	public boolean isSingleton() {
		return children.size() == 1;
	}
	
	 public Iterator<CATHLevel> getSubLevelIterator(int subLevelDepth) {
		 return new LevelIterator(this.name.getLevel(), subLevelDepth, this.children);
	 }
	 
	 public Iterator<CATHLevel> getSubLevelIterator(CathLevelCode subLevelCode) {
		 return new LevelIterator(this.name.getLevel(), subLevelCode.getLevel(), this.children);
	 }
	 
	 public RepSet getRepSet() {
		 // TODO
		 return null;
	 }
	 
	 public void addRep(Rep rep) {
		  String code = rep.getCode();
		  String[] bits = code.split("\\.");
	        // if we are above the 'leaf' level
		  if (this.name.getLevel() < bits.length) {
			  int subLevelPosition = this.name.getLevel();
			  String subLevelCode = bits[subLevelPosition];
			  CATHLevel existingLevel = null;	// TODO
			  // no subLevel found
			  if (existingLevel == null) {
				  StringBuilder fullCodeBuilder = new StringBuilder();
				  for (int i = 0; i <= subLevelPosition; i++) {
					  fullCodeBuilder.append(bits[i]).append(".");
				  }
				  CathLevelCode subLevel = CathLevelCode.values()[this.name.getLevel() + 1];
				  int subLevelCodeIntStuff = 0;	// TODO
				  existingLevel = new CATHLevel(subLevel, subLevelCodeIntStuff, fullCodeBuilder.toString());
//				  this.addSubLevel(existingLevel);	// TODO
			  }
			  existingLevel.addRep(rep);
		  } else if (bits.length == this.name.getLevel()) {
//			  this.repSet.addRep(rep); // TODO
		  } else {
			  System.err.println("gone beyond the level " + this.name.getLevel() + " " + code);
		  }
	 }
	 
	 public String getFullCode() {
		 return "";	// TODO
	 }
}
