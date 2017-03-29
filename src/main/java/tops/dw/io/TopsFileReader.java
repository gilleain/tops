package tops.dw.io;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;

import tops.dw.protein.CATHcode;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.dw.protein.TopsFileFormatException;
import tops.port.model.DomainDefinition;
import tops.port.model.DomainDefinition.DomainType;

public class TopsFileReader {

    public Protein readTopsFile(File TopsFile) throws IOException, TopsFileFormatException {

        FileInputStream fis = new FileInputStream(TopsFile);
        Protein protein = this.readTopsFile(fis);

        StringTokenizer st = new StringTokenizer(TopsFile.getName(), ".");

        if (st.hasMoreTokens())
            protein.setName(st.nextToken());

        return protein;
    }
    
    public Protein readTopsFile(InputStream InStream) throws IOException, TopsFileFormatException {
        return readTopsFile(new BufferedReader(new InputStreamReader(InStream)));
    }

    public Protein readTopsFile(BufferedReader br) throws IOException, TopsFileFormatException {
        Protein protein = new Protein();

        String line = null, FirstToken;
        StringTokenizer st;
        DomainDefinition ddef = null;
        CATHcode ccode = null;

        int[] tmp_int = new int[3];

        int countss = 0;
        int nTokens;
        int x, y, r;
        int j;

        SecStrucElement LastSS = null, CurrentSS = null;

        int i;
        // int headerSize = 1; // XXX tmp - was 5
        int headerSize = 1;
        for (i = 1; i < headerSize; i++) {
            line = br.readLine();
            if (line == null)
                throw new TopsFileFormatException();
            /*
             * if (i == 2) { if (!line.equals(
             * "## TOPS: tops.dw.protein topology information file")) throw new
             * TopsFileFormatException(); }
             */
        }
        line = br.readLine();
        while (line != null) {

            st = new StringTokenizer(line);
            nTokens = st.countTokens();
            // System.out.println(st.countTokens() + " " + line);

            if (nTokens > 0) {

                FirstToken = st.nextToken();
                // System.out.println(line.trim() + " Token: " + FirstToken);

                if (FirstToken.equals("DOMAIN_NUMBER")) {
                    if (nTokens < 3)
                        throw new TopsFileFormatException();
                    st.nextToken();
                    ccode = new CATHcode(st.nextToken());
                    ddef = new DomainDefinition(ccode, DomainType.CHAIN_SET);   // XXX? CHAIN or SEGMENT?
                    while (st.hasMoreTokens()) {
                        for (j = 0; j < 3; j++) {
                            if (st.hasMoreTokens()) {
                                try {
                                    tmp_int[j] = Integer
                                            .parseInt(st.nextToken());
                                } catch (NumberFormatException e) {
                                    throw new TopsFileFormatException();
                                }
                            } else {
                                throw new TopsFileFormatException();
                            }
                        }
                        // XXX currently throwing away the seq frag start
                        ddef.addSegment(ccode.getChain(), tmp_int[1], tmp_int[2]);
                    }

                    countss = 0;
                    LastSS = null;

                } else if (FirstToken.equals("SecondaryStructureType")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    countss++;
                    CurrentSS = new SecStrucElement();

                    if (countss == 1) {
                        protein.addTopsLinkedList(new Cartoon(CurrentSS), ddef);
                    }

                    CurrentSS.SetFrom(LastSS);
                    if (LastSS != null)
                        LastSS.SetTo(CurrentSS);
                    LastSS = CurrentSS;

                    CurrentSS.setType(st.nextToken());
                    CurrentSS.setSymbolNumber(countss - 1);

                } else if (FirstToken.equals("Direction")) {
                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    CurrentSS.setDirection(st.nextToken());
                } else if (FirstToken.equals("Label")) {
                    if (nTokens > 1) {
                        CurrentSS.setLabel(st.nextToken());
                    } else {
                        CurrentSS.setLabel(null);
                    }
                } else if (FirstToken.equals("Colour")) {
                    for (j = 0; j < 3; j++) {
                        tmp_int[j] = 255;
                        if (st.hasMoreTokens()) {
                            try {
                                tmp_int[j] = Integer.parseInt(st.nextToken());
                            } catch (NumberFormatException e) {
                            }
                        }
                        if (tmp_int[j] < 0)
                            tmp_int[j] = 0;
                        if (tmp_int[j] > 255)
                            tmp_int[j] = 255;
                    }
                    CurrentSS.setColour(
                            new Color(tmp_int[0], tmp_int[1], tmp_int[2]));
                } else if (FirstToken.equals("Fixed")) {
                    int fs;
                    if (nTokens > 1) {
                        fs = -1;
                        try {
                            fs = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException e) {
                            throw new TopsFileFormatException();
                        }
                    } else {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetFixedIndex(fs);
                } else if (FirstToken.equals("Next")) {
                    int ns;
                    if (nTokens > 1) {
                        ns = -1;
                        try {
                            ns = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException e) {
                            throw new TopsFileFormatException();
                        }
                    } else {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetNextIndex(ns);
                } else if (FirstToken.equals("FixedType")) {
                    if (nTokens > 1) {
                        CurrentSS.SetFixedType(st.nextToken());
                    } else {
                        CurrentSS.SetFixedType("UNKNOWN");
                    }
                } else if (FirstToken.equals("BridgePartner")) {
                    int bp;
                    while (st.hasMoreTokens()) {
                        try {
                            bp = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                        CurrentSS.AddBridgePartner(bp);
                    }
                } else if (FirstToken.equals("BridgePartnerSide")) {
                    while (st.hasMoreTokens()) {
                        CurrentSS.AddBridgePartnerSide(st.nextToken());
                    }
                } else if (FirstToken.equals("BridgePartnerType")) {
                    while (st.hasMoreTokens()) {
                        CurrentSS.AddBridgePartnerType(st.nextToken());
                    }
                } else if (FirstToken.equals("Neighbour")) {
                    int nb;
                    while (st.hasMoreTokens()) {
                        try {
                            nb = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                        CurrentSS.AddNeighbour(nb);
                    }
                } else if (FirstToken.equals("SeqStartResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    int res;
                    try {
                        res = Integer.parseInt(st.nextToken());
                    } catch (NumberFormatException nfe) {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetSeqStartResidue(res);

                } else if (FirstToken.equals("SeqFinishResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    int res;
                    try {
                        res = Integer.parseInt(st.nextToken());
                    } catch (NumberFormatException nfe) {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetSeqFinishResidue(res);

                } else if (FirstToken.equals("Chirality")) {

                    int chi = 0;
                    if (nTokens > 1) {
                        try {
                            chi = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    CurrentSS.SetChirality(chi);

                } else if (FirstToken.equals("AxesStartPoint")) {
                    if (nTokens != 4)
                        throw new TopsFileFormatException();
                    float sp[] = new float[3];
                    int ct;
                    for (ct = 0; st.hasMoreTokens() && ct < 3; ct++) {
                        sp[ct] = 0.0f;
                        try {
                            sp[ct] = Float.valueOf(st.nextToken()).floatValue();
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    CurrentSS.SetAxesStartPoint(sp[0], sp[1], sp[2]);
                } else if (FirstToken.equals("AxesFinishPoint")) {
                    if (nTokens != 4)
                        throw new TopsFileFormatException();
                    float fp[] = new float[3];
                    int ct;
                    for (ct = 0; st.hasMoreTokens() && ct < 3; ct++) {
                        fp[ct] = 0.0f;
                        try {
                            fp[ct] = Float.valueOf(st.nextToken()).floatValue();
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    CurrentSS.SetAxesFinishPoint(fp[0], fp[1], fp[2]);
                } else if (FirstToken.equals("AxisLength")) {
                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    float al;
                    try {
                        al = Float.valueOf(st.nextToken()).floatValue();
                    } catch (NumberFormatException nfe) {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetAxisLength(al);
                } else if (FirstToken.equals("CartoonX")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    x = Integer.parseInt(st.nextToken());
                    CurrentSS.PlaceElementX(x);

                } else if (FirstToken.equals("CartoonY")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    y = Integer.parseInt(st.nextToken());
                    CurrentSS.PlaceElementY(y);

                } else if (FirstToken.equals("SymbolRadius")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    r = Integer.parseInt(st.nextToken());
                    CurrentSS.SetSymbolRadius(r);

                } else if (FirstToken.equals("ConnectionTo")) {

                    if ((nTokens % 2) != 1)
                        throw new TopsFileFormatException();

                    while (st.hasMoreTokens()) {
                        x = Float.valueOf(st.nextToken()).intValue();
                        y = Float.valueOf(st.nextToken()).intValue();
                        CurrentSS.AddConnectionTo(x, y);
                    }

                } else if (FirstToken.equals("PDBStartResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    CurrentSS.setPDBStartResidue(
                            Integer.parseInt(st.nextToken()));

                } else if (FirstToken.equals("PDBFinishResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    CurrentSS.setPDBFinishResidue(
                            Integer.parseInt(st.nextToken()));

                } else if (FirstToken.equals("Chain")) {
                    if (st.hasMoreTokens()) {
                        CurrentSS.setChain(st.nextToken());
                    } else {
                        CurrentSS.setChain(" ");
                    }
                } else if (FirstToken.equals("Fill")) {

                    int f = 0;
                    if (nTokens > 1) {
                        try {
                            f = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    CurrentSS.SetFill(f);
                }
            }

            line = br.readLine();

        }

        List<Cartoon> lls = protein.getLinkedLists();
        for (Cartoon ll : lls) {
            protein.fixedFromFixedIndex(ll.getRoot());
            protein.nextFromNextIndex(ll.getRoot());
        }

        return protein;
    }

}
