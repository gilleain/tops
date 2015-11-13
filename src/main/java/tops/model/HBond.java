package tops.model;

public class HBond {
    
    private int aminoPos;
    
    private int carboxylPos;
    
    public HBond(int aminoPos, int carboxylPos) {
        this.aminoPos = aminoPos;
        this.carboxylPos = carboxylPos;
    }

    public int getAminoPos() {
        return aminoPos;
    }

    public int getCarboxylPos() {
        return carboxylPos;
    }
    
    public int max() {
        return Math.max(aminoPos, carboxylPos);
    }
    
    public int min() {
        return Math.min(aminoPos, carboxylPos);
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof HBond) {
            HBond other = (HBond) obj;
            return this.aminoPos == other.aminoPos 
                && this.carboxylPos == other.carboxylPos;
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return Integer.valueOf(aminoPos).hashCode() 
             * Integer.valueOf(carboxylPos).hashCode();
    }

    public String toString() {
        return String.format("N%d->O%d", aminoPos, carboxylPos);
    }

}
