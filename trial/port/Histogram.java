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
    }
    
    private List<Bin> bins;
    
    public Histogram(int numberOfBins) {
        this.bins = new ArrayList<>();
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

}
