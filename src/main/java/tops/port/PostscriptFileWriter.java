package tops.port;

import java.io.File;
import java.util.List;

import tops.port.model.Cartoon;
import tops.port.model.PlotFragInformation;
import tops.port.model.Protein;

public class PostscriptFileWriter {
    
    private final int DOMS_PER_PAGE = 2;

    public void makePostscript(
            List<Cartoon> cartoons, 
            Protein protein, 
            int nDomainsToPlot, 
            PlotFragInformation plotFragInf,
            Options options) {
        String postscript = options.getPostscript();
        if (postscript != null) {

            if (options.isVerbose()) System.out.println("Writing postscript file\n");

            int npages = nDomainsToPlot / DOMS_PER_PAGE;
            int nLastPage = nDomainsToPlot % DOMS_PER_PAGE;
            npages += (nLastPage > 0 ? 1 : 0);  // add an extra page if necessary

            for (int i = 0; i < npages; i++) {
                File psfile = getPSFile(postscript, i);
                int nplot = DOMS_PER_PAGE;
                if ((i == (npages - 1)) && nLastPage > 0)
                    nplot = nLastPage;
//                PrintCartoons(
//                   nplot, cartoons, DOMS_PER_PAGE * i, psfile, protein.getProteinCode(), PlotFragInf);
            }
        }
    }
    
    private File getPSFile(String filePrefix, int i) {
        String filename = filePrefix.substring(0, filePrefix.indexOf('.'));
        return new File(filename + "_" + i + ".ps");
    }
}
