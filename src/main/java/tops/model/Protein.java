package tops.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A protein is a named sse graph that has one or more subgraphs (chains) 
 * as well as potentially hbond sets between these chains.
 * @author maclean
 *
 */
public class Protein extends SSEGraph {
    
    private String name;
    
    private final List<Chain> chains;
    
    public Protein() {
        this("");
    }
    
    public Protein(String name) {
        this.name = name;
        this.chains = new ArrayList<Chain>();
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public List<Chain> getChains() {
        return chains;
    }

    public void addChain(Chain currentChain) {
        this.chains.add(currentChain);
    }
    

}
