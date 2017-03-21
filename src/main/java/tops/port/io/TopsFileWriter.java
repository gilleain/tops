package tops.port.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import tops.port.model.BridgePartner;
import tops.port.model.Cartoon;
import tops.port.model.Chain;
import tops.port.model.DomainDefinition;
import tops.port.model.Neighbour;
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
             Protein protein, List<DomainDefinition> domains, List<Integer> domainsToPlot) throws FileNotFoundException {

        int i, j;
        PrintStream out = new PrintStream(new FileOutputStream(filename));
        DomainDefinition dompt;

        writeTOPSHeader(out, protein.getProteinCode(), cartoons.size());

        for (i = 0; i < cartoons.size(); i++) {
            dompt = domains.get(domainsToPlot.get(i));
            out.print(String.format("DOMAIN_NUMBER %d %s", i, dompt.domainCATHCode));
            for (j = 0; j < dompt.numberOfSegments; j++)
                out.print(
                  String.format(" %d %d %d", 
                          dompt.segmentStartIndex[j], dompt.segmentIndices[0][j], dompt.segmentIndices[1][j])
                );
            out.println();
            appendLinkedList(out, cartoons.get(i));
            out.println();
            out.println();
        }

        out.close();
    }
    
    private void appendLinkedList(PrintStream out, Cartoon cartoon) {
        for (SSE sse : cartoon.getSSEs()) {
            print(out, "\n");
            writeSecStr(cartoon, sse, out);
//            p.toTopsFile(chain);  XXX
        }
        return;
    }
    
    
    private void writeSecStr(Chain chain, SSE sse, PrintStream out) {
        print(out, "SecondaryStructureType %c\n", sse.getSSEType());
        print(out, "Direction %c\n", sse.getDirection());
        print(out, "Label %s\n", sse.getLabel());
        print(out, "Colour %d %d %d\n", 
                sse.getCartoonSymbol().getColor()[0], 
                sse.getCartoonSymbol().getColor()[1], 
                sse.getCartoonSymbol().getColor()[2]);

        if (sse.Next != null) {
            print(out, "Next %d\n", sse.Next.getSymbolNumber());
        } else {
            print(out, "Next -1\n");
        }

        if (sse.getFixed() != null) {
            print(out, "Fixed %d\n", sse.getFixed().getSymbolNumber());
        } else {
            print(out, "Fixed -1\n");
        }

        switch (sse.getFixedType()) {
            case BARREL:
                print(out, "FixedType BARREL\n");
                break;
            case SHEET:
                print(out, "FixedType SHEET\n");
                break;
            case CURVED_SHEET:
                print(out, "FixedType CURVED_SHEET\n");
                break;
            case V_CURVED_SHEET:
                print(out, "FixedType V_CURVED_SHEET\n");
                break;
            case SANDWICH:
                print(out, "FixedType SANDWICH\n");
                break;
            case TEMPLATE:
                print(out, "FixedType TEMPLATE\n");
                break;
            case UNKNOWN:
                print(out, "FixedType UNKNOWN\n");
                break;
            default:
                print(out, "FixedType UNKNOWN\n");
                break;
        }

        print(out, "BridgePartner");
        for (BridgePartner BridgePartner : sse.getBridgePartners())
            print(out, " %d", BridgePartner.partner.getSymbolNumber());
        print(out, "\n");

        print(out, "BridgePartnerSide");
        for (BridgePartner BridgePartner : sse.getBridgePartners())
            print(out, " %c", BridgePartner.side);
        print(out, "\n");

        print(out, "BridgePartnerType");
        for (BridgePartner BridgePartner : sse.getBridgePartners()) {
            switch (BridgePartner.bridgeType) {
            case ANTI_PARALLEL_BRIDGE:
                print(out, " %c", 'A');
                break;
            case PARALLEL_BRIDGE:
                print(out, " %c", 'P');
                break;
            case UNK_BRIDGE_TYPE:
                print(out, " %c", 'U');
                break;
            default:
                print(out, " %c", 'U');
                break;
            }
        }
        print(out, "\n");

        print(out, "Neighbour");
        for (Neighbour neighbour : sse.getNeighbours()) {
            print(out, " %d", neighbour.sse.getSymbolNumber());
        }
        print(out, "\n");

        print(out, "SeqStartResidue %d\n", sse.getSseData().SeqStartResidue);
        print(out, "SeqFinishResidue %d\n", sse.getSseData().SeqFinishResidue);

        print(out, "PDBStartResidue %d\n", sse.getSseData().PDBStartResidue);
        print(out, "PDBFinishResidue %d\n", sse.getSseData().PDBFinishResidue);

        print(out, "SymbolNumber %d\n", sse.getSymbolNumber());

        print(out, "Chain %c\n", chain.getName());

        print(out, "Chirality %d\n", sse.getChirality());

        print(out, "CartoonX %d\n", sse.getCartoonX());
        print(out, "CartoonY %d\n", sse.getCartoonY());

        print(out, "AxesStartPoint");
        print(out, " %s", sse.getAxis().AxisStartPoint);
        print(out, "\n");

        print(out, "AxesFinishPoint");
        print(out, " %s", sse.getAxis().AxisFinishPoint);
        print(out, "\n");

        print(out, "SymbolRadius %d\n", sse.getCartoonSymbol().getSymbolRadius());
        print(out, "AxisLength %f\n", sse.getAxis().getLength());
        print(out, "NConnectionPoints %d\n", sse.getNConnectionPoints());
        print(out, "ConnectionTo %s\n", sse.Connections());
        print(out, "Fill %d\n", sse.getFill());
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
