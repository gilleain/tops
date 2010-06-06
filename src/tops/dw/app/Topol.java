package tops.dw.app;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import tops.dw.editor.PostscriptPrinter;
import tops.dw.editor.TopsEditor;
import tops.dw.protein.Protein;
import tops.dw.protein.TopsFileFormatException;

import java.net.*;

import javax.swing.JFrame;

/**
 * the applet to use for viewing diagrams in the TOPS atlas
 * 
 * @author Daniel Hatton, updated by David Westhead, converted to JDK1.1
 * @version 3.00 10 Sept. 1997
 */

public class Topol extends Applet implements ActionListener, ImagePrinter,
        PostscriptPrinter {

    /* START class variables */

    static final String CATHBaseURL = "http://www.biochem.ucl.ac.uk/bsm/pdbsum/";

    static final String PDBBaseURL = "http://www2.ebi.ac.uk/pdb/tops.dw.cgi-bin/opdbshort?oPDBid=";

    static final String MMDBBaseURL = "http://www3.ncbi.nlm.nih.gov/htbin-post/Entrez/query?form=6&db=t&Dopt=s&uid=";

    static final int CATH = 0;

    static final int PDB = 1;

    static final int MMDB = 2;

    static final int LargeFontSize = 12;

    static final int SmallFontSize = 12;

    static final String EditorHelpFile = "EditorHelp11.html";

    /* END class variables */

    /* START instance variables */

    private PChooser chooser;

    private PDBPanel out;

    private Vector TopsEds;

    private boolean InfoFoundState = false;

    private String host;

    private int port;

    private static final String cgi_query_prog = "TOPSQuery.cgi";

    private static final String cgi_print_prog = "TOPSPrint.cgi";

    private static final String cgi_ps_print_prog = "TOPS_PS_Print.cgi";

    private String cgi_prog_path;

    private Integer CurrentDiagID = new Integer(0);

    private Vector p_desc = null;

    private Vector rep_desc = null;

    private String rep_code = null;

    private StringBuffer chains = null;

    private boolean found_other_chains = false;

    private String pcode = null;

    private String pchain = null;

    private URL EdHelpURL = null;
    
    private Button showbutton;

    private Button clearbutton;

    /* END instance variables */
    
    /*
     * jacked in by gmt
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("APP");
        Topol editor = new Topol();
        editor.init();
        frame.add(editor);
        frame.setSize(600, 600);
        
        frame.setLocation(800, 100);    // TMP XXX FIXME!
        
        frame.setVisible(true);
    }

    @Override
    public void init() {

        this.setFont(new Font("TimesRoman", Font.PLAIN, Topol.LargeFontSize));

        this.setLayout(new BorderLayout());

        Panel p1 = new Panel();
        p1.setLayout(new BorderLayout());

        this.chooser = new PChooser(true);
        this.chooser.go.addActionListener(this);
        p1.add("Center", this.chooser);

        Panel viewer = new TopsShow(false);
        this.setBackground(Color.white);
        this.setLayout(new GridLayout(1, 2));
        
        this.showbutton = new Button("View topology cartoon");
        this.showbutton.setBackground(Color.lightGray);
        this.showbutton.setForeground(Color.blue);
        viewer.add(this.showbutton);

        this.clearbutton = new Button("Clear information");
        this.clearbutton.setBackground(Color.lightGray);
        this.clearbutton.setForeground(Color.blue);
        viewer.add(this.clearbutton);
        this.clearbutton.setEnabled(true);
        
    	this.showbutton.addActionListener(this);
    	this.clearbutton.addActionListener(this);

    	this.showbutton.setEnabled(true);
        p1.add("South", viewer);

        this.out = new PDBPanel();
        this.out.addActionListener(this);

        this.add("Center", p1);
        this.add("South", this.out);

        this.setInfoFoundState(false);

        this.TopsEds = new Vector();

        URL DocBase = this.getDocumentBase();
        this.host = DocBase.getHost();
        this.port = DocBase.getPort();
        if (this.port <= 0)
            this.port = 80;

        String dcb_file = DocBase.getFile();
        StringBuffer path = new StringBuffer();

        boolean copy;
        int i;
        for (i = (dcb_file.length() - 1), copy = false; i >= 0; i--) {
            if (dcb_file.charAt(i) == '/')
                copy = true;
            if (copy) {
                path.insert(0, dcb_file.charAt(i));
            }
        }

        try {
            this.EdHelpURL = new URL("http", this.host, this.port, path.toString()
                    + Topol.EditorHelpFile);
        } catch (MalformedURLException mue) {
            this.EdHelpURL = null;
        }

        path.append("tops.dw.cgi-bin/");
        this.cgi_prog_path = path.toString();

    }

    @Override
    public void start() {
        this.chooser.repaint();
        this.out.repaint();
    }

    @Override
    public void stop() {

        // just dispose of any windows created
        if (this.TopsEds == null)
            return;

        int i;
        for (i = 0; i < this.TopsEds.size(); i++) {
            Object obj = this.TopsEds.elementAt(i);
            if ((obj != null) && (obj instanceof TopsEditor)) {
                TopsEditor te = (TopsEditor) obj;
                te.quit();
            }
        }

        this.TopsEds = null;

        return;

    }

    @Override
    public void destroy() {

        // just dispose of any windows created
        if (this.TopsEds == null)
            return;

        int i;
        for (i = 0; i < this.TopsEds.size(); i++) {
            Object obj = this.TopsEds.elementAt(i);
            if ((obj != null) && (obj instanceof TopsEditor)) {
                TopsEditor te = (TopsEditor) obj;
                te.quit();
            }
        }

        this.TopsEds = null;

        return;

    }

    void setInfoFoundState(boolean state) {

        this.InfoFoundState = state;
        if (state) {
            this.showbutton.setEnabled(true);
            this.chooser.setEnabled(false);
        } else {
            this.showbutton.setEnabled(false);
            this.chooser.setEnabled(true);
        }

    }

    private String GetPCode(String PCodeText) {

        if (PCodeText == null)
            return null;

        StringBuffer buff = new StringBuffer();

        int i = PCodeText.lastIndexOf(' ');

        for (int j = 0; (i < PCodeText.length()) && (j < 4); j++, i++) {
            buff.append(PCodeText.charAt(i));
        }

        return buff.toString().toLowerCase();

    }

    private String GetPChain(String PChainText) {

        if (PChainText == null)
            return "0";

        StringBuffer buff = new StringBuffer();
        int i, j;

        for (i = 0; (i < PChainText.length()) && (PChainText.charAt(i) == ' '); i++)
            ;

        for (j = 0; (i < PChainText.length()) && (j < 1); j++, i++) {
            buff.append(PChainText.charAt(i));
        }

        String ch = buff.toString();
        if ((ch == null) || ch.equals("") || ch.substring(0, 1).equals(" "))
            ch = "0";

        return ch.toUpperCase();

    }

    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("Get information")) {

            if (!this.InfoFoundState) {

                this.chooser.pdesc.setText("Please wait");
                this.chooser.cdesc.setText("");
                this.chooser.ccode.setText("");
                this.chooser.cchain.setText("");

                this.pcode = this.GetPCode(this.chooser.pdbcode.getText());
                this.pchain = this.GetPChain(this.chooser.pchain.getText());

                this.QueryTOPSDatabase(this.pcode, this.pchain);

                /*
                 * a fully successful query sets CurrentDiagID, otherwise it's
                 * set to null
                 */
                if (this.CurrentDiagID != null) {

                    String rpc = (this.rep_code.length() >= 4) ? this.rep_code.substring(
                            0, 4) : "UNK";
                    String pc = (this.rep_code.length() >= 5) ? this.rep_code.substring(
                            4, 5) : "U";

                    this.chooser.setInfo(rpc, pc, this.pcode, this.p_desc, this.rep_desc);
                    this.setInfoFoundState(true);

                } else {
                    this.PopUpSearchResultsInfo();
                    this.chooser.pdesc
                            .setText("Unable to find a tops diagram for this code");
                    this.showStatus("Unable to find a tops diagram for this code");
                }

            }
        } else if (e.getActionCommand().equals("BPDB")) {
            this.gotoProteinURL(Topol.PDB);
        } else if (e.getActionCommand().equals("UCL")) {
            this.gotoProteinURL(Topol.CATH);
        } else if (e.getActionCommand().equals("MMDB")) {
            this.gotoProteinURL(Topol.MMDB);
        } else if (e.getActionCommand().equals("Clear information")) {
            this.chooser.pdesc.setText("");
            this.chooser.pdbcode.setText("");
            this.chooser.pchain.setText("");
            this.chooser.cdesc.setText("");
            this.chooser.ccode.setText("");
            this.chooser.cchain.setText("");

            this.setInfoFoundState(false);
        } else if (e.getActionCommand().equals("View topology cartoon")) {

            if (this.InfoFoundState) {

                TopsEditor ed;
                Protein p = this.QueryTOPSDatabase(this.CurrentDiagID);
                if (p != null) {
                    ed = new TopsEditor(this, this.EdHelpURL);
                    ed.addProtein(p);
                    if (ed != null) {
                        if (this.TopsEds == null)
                            this.TopsEds = new Vector();
                        this.TopsEds.addElement(ed);
                    } else {
                        this.showStatus("An internal error occurred: unable to display diagram");
                    }

                    this.setInfoFoundState(false);
                } else {
                    this.showStatus("A database error occurred: unable to find diagram");
                }

            }

        }

    }

    /* printing methods */
    public void printImage(Image img) {

        AppletImagePrinter aip = new AppletImagePrinter(img, this.host, this.port,
                this.cgi_prog_path, Topol.cgi_print_prog);

        URL printurl = aip.doPrint();

        if (printurl != null) {
            this.getAppletContext().showDocument(printurl, "_blank");
        } else {
            System.out.println(aip.getErrorString());
            this.showStatus("Sorry, unable to print for some reason");
        }

    }

    public void printPostscript(Vector ps) {

        AppletPSPrinter apsp = new AppletPSPrinter(ps, this.host, this.port,
                this.cgi_prog_path, Topol.cgi_ps_print_prog);

        // !ALTERED on 2/10/2003 by GMT : AppletPSPrinter does not have a
        // doPrint() that returns a URL
        URL printurl = null;
        try {
            apsp.doPrint();
        } catch (IOException ioe) {
            System.out.println(apsp.getErrorString());
            this.showStatus("Sorry, unable to print for some reason");
        }

        if (printurl != null) {
            this.getAppletContext().showDocument(printurl, "_blank");
        } else {
            System.out.println(apsp.getErrorString());
            this.showStatus("Sorry, unable to print for some reason");
        }
    }

    /*
     * This private method queries the TOPS database given a tops.dw.protein
     * code and chain it determines the tops.dw.protein description for this
     * code (p_desc), the tops.dw.protein code of its representative TOPS
     * diagram ( rep_code ), the description of the representative
     * tops.dw.protein. If the code and chain are not found, it returns
     * found_other_chains = true if the code was fgound with different chains
     * and stores the found chains in the StringBuffer chains.
     * 
     * Method used is not elegant - java calling a CGI script on the server
     * Might replace with a more elegant solution ( JDBC, or java/CORBA ) at a
     * later date.
     */
    private void QueryTOPSDatabase(String pcode, String pchain) {

        /* reset the variables determined by the query */
        this.p_desc = new Vector();
        this.rep_desc = new Vector();
        this.rep_code = new String();
        this.chains = new StringBuffer();
        this.found_other_chains = false;
        this.CurrentDiagID = null;

        String query = "name=" + pcode + pchain;
        CGIrequest cgir = new CGIrequest(this.host, this.port, "GET", this.cgi_prog_path
                + Topol.cgi_query_prog, query);
        InputStream cgi_response = cgir.doRequest();

        if (cgi_response == null) {
            System.out.println(cgir.getErrorString());
            this.showStatus("Unable to connect to database");
            return;
        } else {

            try {

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        cgi_response));
                String line;

                /* two possible non-error starts - */
                /* either find a DESCRIPTION_FOLLOWS or a CHAINS_FOUND_FOLLOW */
                line = br.readLine();
                while ((line != null) && (!line.equals("DESCRIPTION_FOLLOWS"))
                        && (!line.equals("CHAINS_FOUND_FOLLOW"))) {
                    line = br.readLine();
                }

                if ((line != null) && line.equals("DESCRIPTION_FOLLOWS")) {

                    while ((line != null) && (!line.equals("REP_CODE_FOLLOWS"))) {
                        line = br.readLine();
                        if ((line != null)
                                && (!line.equals("REP_CODE_FOLLOWS")))
                            this.p_desc.addElement(line);
                    }
                    while ((line != null)
                            && (!line.equals("REP_DESCRIPTION_FOLLOWS"))) {
                        line = br.readLine();
                        if ((line != null)
                                && (!line.equals("REP_DESCRIPTION_FOLLOWS")))
                            this.rep_code = line;
                    }
                    while ((line != null) && (!line.equals("DIAGRAM_ID"))) {
                        line = br.readLine();
                        if ((line != null) && (!line.equals("DIAGRAM_ID")))
                            this.rep_desc.addElement(line);
                    }
                    while ((line != null) && (!line.equals("END_OF_DATA"))) {
                        line = br.readLine();
                        if ((line != null) && (!line.equals("END_OF_DATA"))) {
                            try {
                                this.CurrentDiagID = Integer.valueOf(line);
                            } catch (NumberFormatException nfe) {
                                this.CurrentDiagID = null;
                            }
                        }
                    }
                } else if ((line != null) && line.equals("CHAINS_FOUND_FOLLOW")) {
                    this.found_other_chains = true;
                    while ((line != null) && (!line.equals("END_OF_DATA"))) {
                        line = br.readLine();
                        if ((line != null) && (!line.equals("END_OF_DATA")))
                            this.chains.append(line + "\n");
                    }
                }

            } catch (IOException ioe) {
                System.out
                        .println("An IOException was caught in QueryTOPSDatabase");
            }

        }
        try {
            cgir.Close();
        } catch (IOException ioe) {
        }

        return;

    }

    /*
     * This private method queries the TOPS database given a diagram id number
     * it returns a Protein object or null on error
     * 
     * Method used is not elegant - java calling a CGI script on the server
     * Might replace with a more elegant solution ( JDBC, or java/CORBA ) at a
     * later date.
     */
    private Protein QueryTOPSDatabase(Integer diagram_id) {

        Protein p = null;

        if (diagram_id == null)
            return null;

        String query = "diagram=" + diagram_id.toString();

        CGIrequest cgir = new CGIrequest(this.host, this.port, "GET", this.cgi_prog_path
                + Topol.cgi_query_prog, query);
        InputStream cgi_response = cgir.doRequest();

        if (cgi_response == null) {
            System.out.println(cgir.getErrorString());
            this.showStatus("Unable to connect to database");
        } else {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        cgi_response));
                String line = br.readLine();
                while ((line != null) && (!line.equals("TOPS_DIAG_FOLLOWS"))) {
                    line = br.readLine();
                }
                if (line != null) {
                    p = new Protein(br);
                } else {
                    this.showStatus("Unable to get the diagram from the database");
                }
            } catch (TopsFileFormatException tffe) {
                p = null;
                System.out
                        .println("A TopsFileFormatException was caught in topol.QueryTOPSDatabase");
            } catch (IOException ioe) {
                p = null;
                System.out
                        .println("An IOException was caught in topol.QueryTOPSDatabase");
            }

        }

        try {
            cgir.Close();
        } catch (IOException ioe) {
        }

        return p;

    }

    private void PopUpSearchResultsInfo() {

        StringBuffer message = new StringBuffer("The code " + this.pcode + " ");
        if (!this.pchain.equals("0"))
            message.append("with chain " + this.pchain);
        else
            message.append("with chain 0 (or blank)");
        message.append(" was not found in the database\n");

        if (this.found_other_chains) {
            message.append("The following chains were found:\n"
                    + this.chains.toString());
            message.append("Please try again, specifying one of these");
        } else {
            message.append("Please check your pdb code");
        }

        AppInfoFrame aif = new AppInfoFrame(message.toString());
        aif.setVisible(true);

    }

    private void gotoProteinURL(int site) {

        StringBuffer url_st = new StringBuffer();
        String text = this.GetPCode(this.chooser.pdbcode.getText());

        switch (site) {
            case CATH:
                url_st.append(Topol.CATHBaseURL);
                url_st.append(text.toLowerCase());
                url_st.append("/main.html");
                break;
            case PDB:
                url_st.append(Topol.PDBBaseURL);
                url_st.append(text.toUpperCase());
                break;
            case MMDB:
                url_st.append(Topol.MMDBBaseURL);
                url_st.append(text.toUpperCase());
                break;
            default:
                return;
        }

        try {
            URL go = new URL(url_st.toString());
            this.getAppletContext().showDocument(go, "_blank");
        } catch (MalformedURLException m) {
            this.showStatus("Entry not found");
        }

        return;

    }

}
