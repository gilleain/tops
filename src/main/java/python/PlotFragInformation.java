package python;

public class PlotFragInformation {
    public static final int MAX_PLOT_FRAGS = 100; // XXX remove

    int NFrags;
    char[][] FragChainLims = new char[MAX_PLOT_FRAGS][2];
    int[][] FragResLims = new int[MAX_PLOT_FRAGS][2];
    int[] FragDomain = new int[MAX_PLOT_FRAGS];

}
