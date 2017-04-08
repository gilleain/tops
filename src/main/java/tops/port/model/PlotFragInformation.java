package tops.port.model;

public class PlotFragInformation {
    public static final int MAX_PLOT_FRAGS = 100; // XXX remove

    private int numberOfFragments;
    private char[][] fragmentChainLimits = new char[MAX_PLOT_FRAGS][2];
    private int[][] fragmentResidueLimits = new int[MAX_PLOT_FRAGS][2];
    private int[] fragmentDomains = new int[MAX_PLOT_FRAGS];

    public PlotFragInformation() {
        this.numberOfFragments = 0;
        for (int i = 0; i < PlotFragInformation.MAX_PLOT_FRAGS; i++) {
            this.fragmentDomains[i] = -1;
            for (int j = 0; j < 2; j++) {
                this.fragmentChainLimits[i][j] = '\0';
                this.fragmentResidueLimits[i][j] = -1;
            }
        }
    }
    
    public char getStartFragmentChainLimit(int index) {
        return fragmentChainLimits[index][0];
    }
    
    public char getEndFragmentChainLimit(int index) {
        return fragmentChainLimits[index][1];
    }
    
    public int getStartFragmentResidueLimit(int index) {
        return fragmentResidueLimits[index][0];
    }
    
    public int getEndFragmentResidueLimit(int index) {
        return fragmentResidueLimits[index][1];
    }
    
    public void setFragDomain(int index, int domain) {
        this.fragmentDomains[index] = domain;
    }
    
    public void setStartChainLim(int index, char chain) {
        this.fragmentChainLimits[index][0] = chain;
    }
    
    public void setEndChainLim(int index, char chain) {
        this.fragmentChainLimits[index][1] = chain;
    }
    
    public void setStartResLim(int index, int residueNumber) {
        this.fragmentResidueLimits[index][0] = residueNumber;
    }
    
    public void setEndResLim(int index, int residueNumber) {
        this.fragmentResidueLimits[index][1] = residueNumber;
    }
    
    public void setNumberOfFragments(int numberOfFragments) {
        this.numberOfFragments = numberOfFragments;
    }
    
    public int getNumberOfFragments() {
        return numberOfFragments;
    }
}
