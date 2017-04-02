package tops.dw.io;

import java.awt.Color;
import java.awt.Point;
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

                String chain = String.valueOf(dd.getCATHcode().getChain());
                for (SecStrucElement s : lls.get(0).getSSEs()) {
                    PrintAsText(chain, s, pw);
                    pw.print("\n");
                }

            }
        }

    }
    

    public void PrintAsText(String chain, SecStrucElement s, PrintWriter ps) {

        ps.println("SecondaryStructureType " + s.getType().getOneLetterName());
        ps.println("Direction " + s.getDirection());
        if (s.getLabel() != null) {
            ps.println("Label " + s.getLabel());
        } else { 
            ps.println("Label");
        }

        Color c = s.getColour();
        if (c == null) {
            c = Color.white;
        }
        ps.println("Colour " + c.getRed() + " " + c.getGreen() + " " + c.getBlue());

        int n = -1;
//        if (s.getNext() != null) { // TODO
//            n = s.getNext().getSymbolNumber();
//        }
//        ps.println("Next " + n);

        int f = -1;
//        if (s.getFixed() != null)
//            f = this.Fixed.SymbolNumber;
        ps.println("Fixed " + f);   // TODO

//        if (s.getFixedType() != null) { TODO
//            ps.println("FixedType " + s.getFixedType());
//        } else {
//            ps.println("FixedType UNKNOWN");
//        }

        ps.print("BridgePartner");
        for (Integer bridgePartner : s.getBridgePartner()) {
            ps.print(" " + bridgePartner);
        }
        ps.print("\n");

        ps.print("BridgePartnerSide");
        for (String bridgePartnerSide : s.getBridgePartnerSide()) {
            ps.print(" " + bridgePartnerSide);
        }
        ps.print("\n");

        ps.print("BridgePartnerType");
        for (String bridgePartnerType : s.getBridgePartnerType()) {
            ps.print(" " + bridgePartnerType);
        }
        ps.print("\n");

        ps.print("Neighbour");
        for (Integer neighbour : s.getNeighbour()) {
            ps.print(" " + neighbour);
        }
        ps.print("\n");

        ps.println("SeqStartResidue " + s.getSeqStartResidue());
        ps.println("SeqFinishResidue " + s.getSeqFinishResidue());

        ps.println("PDBStartResidue " + s.getPDBStartResidue());
        ps.println("PDBFinishResidue " + s.getPDBFinishResidue());

        ps.println("SymbolNumber " + s.getSymbolNumber());
        ps.println("Chain " + chain);

        ps.println("Chirality " + s.getChirality());

        Point p = s.getPosition();
        if (p == null) {
            p = new Point();
        }
        ps.println("CartoonX " + p.x);
        ps.println("CartoonY " + p.y);

        ps.println("AxesStartPoint " + s.getAxesStartPoint()[0] + " "
                + s.getAxesStartPoint()[1] + " " + s.getAxesStartPoint()[2]);
        ps.println("AxesFinishPoint " + s.getAxesFinishPoint()[0] + " "
                + s.getAxesFinishPoint()[1] + " " + s.getAxesFinishPoint()[2]);

        ps.println("SymbolRadius " + s.getSymbolRadius());

        ps.println("AxisLength " + s.getAxisLength());

        List<Point> ct = s.getConnectionTo();
        ps.println("NConnectionPoints " + ct.size());
        ps.print("ConnectionTo");
        for (Point cp : ct) {
            ps.print(" " + cp.x + " " + cp.y);
        }
        ps.print("\n");

        ps.println("Fill " + s.getFill());

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
