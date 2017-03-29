package tops.dw.io;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.port.model.DomainDefinition;
import tops.port.model.Segment;

public class TopsFileWriter {
    

    public void writeTopsFile(Protein protein, OutputStream os) {

        if (os == null)
            return;

        PrintWriter pw = new PrintWriter(os, true);

        this.writeTopsHeader(protein, pw);

        if ((protein.getDomainDefs() != null) && (protein.getLinkedLists() != null)) {

            List<DomainDefinition> ddefs = protein.getDomainDefs();
            List<Cartoon> lls = protein.getLinkedLists();

            int i;
            for (i = 0; i < ddefs.size(); i++) {

                DomainDefinition dd = ddefs.get(i);
                pw.print("DOMAIN_NUMBER " + i + " " + dd.getCATHcode());
                for (Segment segment : dd.getSegments()) {
                    // XXX currently printing a 0 fragment index
                    pw.print(" " + 0 + " " + segment.startIndex + " " + segment.endIndex);
                }
                pw.print("\n\n");

                SecStrucElement s;
                for (s = lls.get(0).getRoot(); s != null; s = s.GetTo()) {
                    s.PrintAsText(pw);
                    pw.print("\n");
                }

            }
        }

    }

    private void writeTopsHeader(Protein protein, PrintWriter pw) {

        pw.println("##");
        pw.println("## TOPS: tops.dw.protein topology information file");
        pw.println("##");
        pw.println("## Protein code " + protein.getName());

        pw.println("## Number of domains " + protein.numberDomains());
        pw.println("##");
        pw.print("\n");

    }


}
