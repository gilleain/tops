package tops.dw.io;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import tops.dw.protein.CATHcode;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.dw.protein.TopsFileFormatException;
import tops.port.model.Direction;
import tops.port.model.DomainDefinition;
import tops.port.model.DomainDefinition.DomainType;
import tops.port.model.SSEType;

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

        String line = null, firstToken;
        StringTokenizer st;

        int countss = 0;
        int nTokens;
        int x, y, r;
        int j;

        SecStrucElement currentSS = null;
        DomainDefinition ddef = null;

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
        Cartoon cartoon = null;
        while (line != null) {

            st = new StringTokenizer(line);
            nTokens = st.countTokens();
            // System.out.println(st.countTokens() + " " + line);

            if (nTokens > 0) {

                firstToken = st.nextToken();
                // System.out.println(line.trim() + " Token: " + firstToken);

                if (firstToken.equals("DOMAIN_NUMBER")) {
                    ddef = handleDomainNumber(st, nTokens);
                    cartoon = new Cartoon();
                    countss = 0;
                } else if (firstToken.equals("SecondaryStructureType")) {
                    currentSS = handleSecStrucType(st, nTokens);
                    currentSS.setSymbolNumber(countss);
                    cartoon.addSSE(currentSS);
                    countss++;
                    if (countss == 1) {
                        protein.addTopsLinkedList(cartoon, ddef);
                    }
                } else if (firstToken.equals("Direction")) {
                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    currentSS.setDirection(Direction.fromString(st.nextToken()));
                } else if (firstToken.equals("Label")) {
                    if (nTokens > 1) {
                        currentSS.setLabel(st.nextToken());
                    } else {
                        currentSS.setLabel(null);
                    }
                } else if (firstToken.equals("Colour")) {
                    int[] tmp_int = new int[3];
                    for (j = 0; j < 3; j++) {
                        tmp_int[j] = 255;
                        if (st.hasMoreTokens()) {
                            try {
                                tmp_int[j] = Integer.parseInt(st.nextToken());
                            } catch (NumberFormatException e) {
                            }
                        }
                        if (tmp_int[j] < 0) {
                            tmp_int[j] = 0;
                        }
                        if (tmp_int[j] > 255) {
                            tmp_int[j] = 255;
                        }
                    }
                    currentSS.setColour(
                            new Color(tmp_int[0], tmp_int[1], tmp_int[2]));
                } else if (firstToken.equals("Fixed")) {
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
//                    currentSS.setFixedIndex(fs); TODO
                } else if (firstToken.equals("Next")) {
//                    int ns; // TODO
//                    if (nTokens > 1) {
//                        ns = -1;
//                        try {
//                            ns = Integer.parseInt(st.nextToken());
//                        } catch (NumberFormatException e) {
//                            throw new TopsFileFormatException();
//                        }
//                    } else {
//                        throw new TopsFileFormatException();
//                    }
//                    currentSS.setNextIndex(ns);
                } else if (firstToken.equals("FixedType")) {
                    // TODO
//                    if (nTokens > 1) {
//                        currentSS.setFixedType(st.nextToken());
//                    } else {
//                        currentSS.setFixedType("UNKNOWN");
//                    }
                } else if (firstToken.equals("BridgePartner")) {
                    int bp;
                    while (st.hasMoreTokens()) {
                        try {
                            bp = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                        currentSS.addBridgePartner(bp);
                    }
                } else if (firstToken.equals("BridgePartnerSide")) {
                    while (st.hasMoreTokens()) {
                        currentSS.addBridgePartnerSide(st.nextToken());
                    }
                } else if (firstToken.equals("BridgePartnerType")) {
                    while (st.hasMoreTokens()) {
                        currentSS.addBridgePartnerType(st.nextToken());
                    }
                } else if (firstToken.equals("Neighbour")) {
                    int nb;
                    while (st.hasMoreTokens()) {
                        try {
                            nb = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                        currentSS.addNeighbour(nb);
                    }
                } else if (firstToken.equals("SeqStartResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    int res;
                    try {
                        res = Integer.parseInt(st.nextToken());
                    } catch (NumberFormatException nfe) {
                        throw new TopsFileFormatException();
                    }
                    currentSS.setSeqStartResidue(res);

                } else if (firstToken.equals("SeqFinishResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    int res;
                    try {
                        res = Integer.parseInt(st.nextToken());
                    } catch (NumberFormatException nfe) {
                        throw new TopsFileFormatException();
                    }
                    currentSS.setSeqFinishResidue(res);

                } else if (firstToken.equals("Chirality")) {

                    int chi = 0;
                    if (nTokens > 1) {
                        try {
                            chi = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    currentSS.setChirality(chi);

                } else if (firstToken.equals("AxesStartPoint")) {
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
                    currentSS.setAxesStartPoint(sp[0], sp[1], sp[2]);
                } else if (firstToken.equals("AxesFinishPoint")) {
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
                    currentSS.setAxesFinishPoint(fp[0], fp[1], fp[2]);
                } else if (firstToken.equals("AxisLength")) {
//                    if (nTokens != 2) TODO - why would we set this?
//                        throw new TopsFileFormatException();
//                    float al;
//                    try {
//                        al = Float.valueOf(st.nextToken()).floatValue();
//                    } catch (NumberFormatException nfe) {
//                        throw new TopsFileFormatException();
//                    }
//                    currentSS.setAxisLength(al);
                } else if (firstToken.equals("CartoonX")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    x = Integer.parseInt(st.nextToken());
                    currentSS.placeElementX(x);

                } else if (firstToken.equals("CartoonY")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    y = Integer.parseInt(st.nextToken());
                    currentSS.placeElementY(y);

                } else if (firstToken.equals("SymbolRadius")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    r = Integer.parseInt(st.nextToken());
                    currentSS.setSymbolRadius(r);

                } else if (firstToken.equals("ConnectionTo")) {

                    if ((nTokens % 2) != 1)
                        throw new TopsFileFormatException();

                    while (st.hasMoreTokens()) {
                        x = Float.valueOf(st.nextToken()).intValue();
                        y = Float.valueOf(st.nextToken()).intValue();
                        currentSS.addConnectionTo(x, y);
                    }

                } else if (firstToken.equals("PDBStartResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    currentSS.setPDBStartResidue(
                            Integer.parseInt(st.nextToken()));

                } else if (firstToken.equals("PDBFinishResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    currentSS.setPDBFinishResidue(
                            Integer.parseInt(st.nextToken()));

                } else if (firstToken.equals("Chain")) {
                    // TODO - domains could have sses in different chains?
                } else if (firstToken.equals("Fill")) {

                    int f = 0;
                    if (nTokens > 1) {
                        try {
                            f = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    currentSS.setFill(f);
                }
            }

            line = br.readLine();

        }

        return protein;
    }
    
    private DomainDefinition handleDomainNumber(StringTokenizer st, int nTokens) throws TopsFileFormatException {
        if (nTokens < 3) {
            throw new TopsFileFormatException();
        }
        st.nextToken();
        
        int[] tmp_int = new int[3];
        CATHcode ccode = new CATHcode(st.nextToken());
        
        // XXX? CHAIN or SEGMENT?
        DomainDefinition ddef = 
                new DomainDefinition(ccode, DomainType.CHAIN_SET);   
        while (st.hasMoreTokens()) {
            for (int j = 0; j < 3; j++) {
                if (st.hasMoreTokens()) {
                    try {
                        tmp_int[j] = Integer.parseInt(st.nextToken());
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
        return ddef;
    }
    
    private SecStrucElement handleSecStrucType(StringTokenizer st, int nTokens) throws TopsFileFormatException {
        if (nTokens != 2) {
            throw new TopsFileFormatException();
        }
        SecStrucElement currentSS = new SecStrucElement();
        currentSS.setType(SSEType.fromCode(st.nextToken()));
        return currentSS;
    }

}
