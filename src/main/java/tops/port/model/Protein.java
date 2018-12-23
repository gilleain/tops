package tops.port.model;

import java.util.ArrayList;
import java.util.List;

public class Protein {
    
    private List<Chain> chains;
    private String proteinCode;
    
    public Protein(String proteinCode) {
        this.proteinCode = proteinCode;
        this.chains = new ArrayList<>();
    }
    
    public void setCode(String name) {
        this.proteinCode = name;
    }

    public String getProteinCode() {
        return this.proteinCode;
    }
    
    public void addChain(Chain chain) {
        this.chains.add(chain);
    }
    
    public List<Chain> getChains() {
        return this.chains;
    }
        
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Chain chain : this.chains) {
            sb.append(this.proteinCode + chain.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    
    ///////////////////////

    public int getSequenceNumber(int pdbIndex, char chainCode) {
        for (Chain chain : chains) {
            if (chain.getName() == chainCode) {
                chain.getPDBIndex(pdbIndex);    /// XXX TODO - is this right?
            }
        }
        return -1;
    }

}