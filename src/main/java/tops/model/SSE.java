package tops.model;

import java.util.ArrayList;
import java.util.List;

public class SSE {
    
    public enum Type {
        ALPHA_HELIX ("H"),
        HELIX_310 ("G"),
        PI_HELIX ("I"),
        EXTENDED ("E"),
        TURN ("T"),
        ISO_BRIDGE ("B"),
        S_BEND("S"),
        UNKNOWN ("U");
        
        private String code;
        
        private Type(String code) {
            this.code = code;
        }
        
        public static Type fromCode(String code) {
            for (Type type : Type.values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return Type.UNKNOWN;
        }
    }
    
    private Type type;
    
    private List<Residue> residues;
    
    public SSE(int pdbIndex, Type type) {
        this.type = type;
        this.residues = new ArrayList<Residue>();
    }
    
    public Type getType() {
        return this.type;
    }
    
    public void addResidue(Residue residue) {
        this.residues.add(residue);
    }
    
    public List<Residue> getResidues() {
        return this.residues;
    }

    public boolean containsResidue(int residueIndex) {
        for (Residue residue : residues) {
            if (residue.getResidueNumber() == residueIndex) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        return this.type.code + " [" + residues.get(0).getResidueNumber() + " - " 
                                     + residues.get(residues.size() - 1).getResidueNumber() + "]";
    }

    public int getStart() {
        return residues.get(0).getResidueNumber();
    }

}
