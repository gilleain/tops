package port;

import java.util.ArrayList;
import java.util.List;

public class Histogram {
    
    private class Bin {
        public Bin(double min, double max) {
            this.min = min;
            this.max = max;
        }
        
        public double min;
        public double max;
        public List<String> values = new ArrayList<>();
        public String toString() {
            return String.format("(%2.2f, %2.2f) %s %s", min, max, values.size(), values);
        }
        public boolean contains(double value) {
            return value > min && value < max;
        }
        
        public String shortForm() {
//            return String.format("%2.2f:%2.2f:%s", min, max, values.size());
            return String.format("%2.2f:%2.2f:%s", min, max, values);
        }
    }
    
    private List<Bin> bins;
    
    public Histogram() {
        this.bins = new ArrayList<>();
    }
    
    public Histogram(int numberOfBins) {
        this();
        double binSize = 1.0/numberOfBins;
        double min = 0;
        double max = binSize;
        for (int binCount = 0; binCount < numberOfBins; binCount++) {
            bins.add(new Bin(min, max));
            min = max;
            max += binSize;
        }
    }
    
    public void add(String key, double value) {
        for (Bin bin : bins) {
            if (bin.contains(value)) {
                bin.values.add(key);
                return;
            }
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Bin bin : bins) {
            sb.append(bin).append("\n");
        }
        return sb.toString();
    }
    
    public String toShortString() {
        StringBuilder sb = new StringBuilder("|");
        for (Bin bin : bins) {
            sb.append(bin.shortForm()).append("|");
        }
        return sb.toString();
    }
    
    /**
     * Compare this histogram ('from') to another 'to' by
     *  - For each key
     *  -- Find the bin index in the 'from'
     *  -- Find the bin index in the 'to'
     *  -- Diff those
     *  - Sum those differences
     *  
     * @param other
     * @return
     */
    public int compareShifts(Histogram to) {
        int fromBinIndex = 1;
        int shift = 0;
        for (Bin bin : bins) {
            for (String key : bin.values) {
                int toBinIndex = to.getBinIndex(key);
                shift += toBinIndex - fromBinIndex;
            }
            fromBinIndex++;
        }
        return shift;
    }
    
    public int getBinIndex(String key) {
        int binIndex = 1;
        for (Bin bin : bins) {
            if (bin.values.contains(key)) {
                return binIndex;
            }
            binIndex++;
        }
        return 0;   // could throw error...
    }
    
    public static Histogram fromString(String stringForm) {
        String[] parts = stringForm.substring(1, stringForm.length() - 1).split("\\|");
        Histogram h = new Histogram();
        for (String part : parts) {
            String[] subParts = part.split(":");
            Bin bin = h.new Bin(Double.parseDouble(subParts[0]),
                              Double.parseDouble(subParts[1]));
            String valueString = subParts[2].substring(1, subParts[2].length() - 1);
            if (valueString.length() > 0) {
                for (String value : valueString.split(",")) {
                   bin.values.add(value);
                }
            }
            h.bins.add(bin);
        }
        return h;
    }

}
