package tops.port.model;

public class PlotFragInformation {
    public static final int MAX_PLOT_FRAGS = 100; // XXX remove

    int NFrags;
    char[][] FragChainLims = new char[MAX_PLOT_FRAGS][2];
    int[][] FragResLims = new int[MAX_PLOT_FRAGS][2];
    int[] FragDomain = new int[MAX_PLOT_FRAGS];

    public PlotFragInformation() {
        this.NFrags = 0;
        for (int i = 0; i < PlotFragInformation.MAX_PLOT_FRAGS; i++) {
            this.FragDomain[i] = -1;
            for (int j = 0; j < 2; j++) {
                this.FragChainLims[i][j] = '\0';
                this.FragResLims[i][j] = -1;
            }
        }
    }
}
