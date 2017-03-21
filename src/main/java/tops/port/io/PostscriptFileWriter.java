package tops.port.io;

import java.io.File;
import java.util.List;

import tops.port.Options;
import tops.port.model.Cartoon;
import tops.port.model.PlotFragInformation;
import tops.port.model.Protein;

public class PostscriptFileWriter {
    
    private final int DOMS_PER_PAGE = 2;
    
    private Options options;
    
    public PostscriptFileWriter(Options options) {
        this.options = options;
    }

    public void makePostscript(List<Cartoon> cartoons, Protein protein, PlotFragInformation plotFragInf) {
        String postscript = options.getPostscript();
        if (postscript != null) {
            int nDomainsToPlot = cartoons.size();
            if (options.isVerbose()) System.out.println("Writing postscript file\n");

            int npages = nDomainsToPlot / DOMS_PER_PAGE;
            int nLastPage = nDomainsToPlot % DOMS_PER_PAGE;
            npages += (nLastPage > 0 ? 1 : 0);  // add an extra page if necessary

            TopsPrint topsPrint = new TopsPrint();
            String proteinCode = protein.getProteinCode();
            for (int i = 0; i < npages; i++) {
                String psfile = getPSFile(postscript, i).getAbsolutePath();
                int nplot = DOMS_PER_PAGE;  // XXX regression?
                if ((i == (npages - 1)) && nLastPage > 0)
                    nplot = nLastPage;
                int pageStartIndex = i * DOMS_PER_PAGE;
                int pageEndIndex = pageStartIndex + DOMS_PER_PAGE;
                List<Cartoon> page = cartoons.subList(pageStartIndex, pageEndIndex);
                topsPrint.printCartoons(page, psfile, proteinCode, plotFragInf);
            }
        }
    }
    
    private File getPSFile(String filePrefix, int i) {
        String filename = filePrefix.substring(0, filePrefix.indexOf('.'));
        return new File(filename + "_" + i + ".ps");
    }
}
