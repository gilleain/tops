package tops.port.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import tops.port.model.Cartoon;
import tops.port.model.DomainDefinition;
import tops.port.model.Protein;
import tops.port.model.SSE;

public class TopsFileWriter {
    
    public void writeTOPSFile(String filename, Protein protein) {
        // TODO
    }
    
    public void writeTOPSFile(PrintStream out, Protein protein) {
        // TODO
    }
    
    public void writeTOPSFile(String filename, List<Cartoon> cartoons,
             Protein protein, List<Integer> domainsToPlot) throws FileNotFoundException {

        int i, j;
        PrintStream out = new PrintStream(new FileOutputStream(filename));
        DomainDefinition dompt;

        writeTOPSHeader(out, protein.getProteinCode(), cartoons.size());

        for (i = 0; i < cartoons.size(); i++) {
            dompt = protein.getDomain(domainsToPlot.get(i));
            out.print(String.format("DOMAIN_NUMBER %d %s", i, dompt.domainCATHCode));
            for (j = 0; j < dompt.numberOfSegments; j++)
                out.print(
                  String.format(" %d %d %d", 
                          dompt.segmentStartIndex[j], dompt.segmentIndices[0][j], dompt.segmentIndices[1][j])
                );
            out.println();
            // XXX ugly, but due to the difference between a pointer and an array/list
            appendLinkedList(out, cartoons.get(i).getSSEs().get(0));
            out.println();
            out.println();
        }

        out.close();
    }
    
    private void appendLinkedList(PrintStream out, SSE p) {
        for (; p != null; p = p.To) {
            print(out, "\n");
            p.WriteSecStr(out);
//            p.toTopsFile(chain);  XXX
        }
        return;
    }
    
    private void writeTOPSHeader(PrintStream out, String pcode, int NDomains) {
        print(out,
                "##\n## TOPS: protein topology information file\n##\n## Protein code %s\n## Number of domains %d\n##\n\n",
                pcode, NDomains);
    }
    
    private void print(PrintStream out, String s, Object... args) {
        out.print(String.format(s, args));
    }
}
