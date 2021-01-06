package tops.model.classification;

import java.io.PrintStream;
import java.util.List;

/**
 * A fairly trivial interface to allow polymorphic use of classification trees
 * to get reps and print
 */

public interface ClassificationTree {

    /** Check that this tree contains a given domain ID */
    public boolean isDomainIDInTree(String domainID);

    /**
     * Given a domainID string, return a list of levels at which this domain is a rep
     */
    public List<LevelCode> getReps(String domainID);

    /**
     * Given a domainID string, return a the highest level at which this domain is a rep
     */
    public LevelCode getHighestRep(String domainID);

    /** Given a domainID string, return a classification number */
    public String getNumberForDomainID(String domainID);

    /** Print the entire tree to the specified stream */
    public void printToStream(PrintStream out);

    /** An optimisation tactic */
    public void removeDomainID(String domainID);

}
