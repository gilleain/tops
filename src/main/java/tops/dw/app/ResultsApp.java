package tops.dw.app;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import tops.dw.editor.PostscriptPrinter;
import tops.dw.editor.TopsEditor;
import tops.dw.io.TopsFileReader;
import tops.dw.protein.Protein;

/**
 * the applet to use for viewing diagrams generated by the server
 * 
 * @author David Westhead
 * @version 1.00 07 Oct. 1997
 */
public class ResultsApp extends Applet implements ActionListener, PostscriptPrinter {

    static final int LARGE_FONT_SIZE = 12;

    static final int SMALL_FONT_SIZE = 12;

    static final String EDITOR_HELP_FILE = "EditorHelp11.html";

    static final String TOPS_FILE_URL_BASE = "http://www.bioinformatics.leeds.ac.uk/tops_server_files/TOPS";

    private static final String CGI_PROG_PATH = "http://www.bioinformatics.leeds.ac.uk/tops.dw.cgi-bin/tops/";

    private static final String CGI_PS_PRINT_PROG = "TOPS_PS_Print.cgi";

    private TextField diagIdInput = null;

    private TextField magicInput = null;

    private TextArea errorTextArea = null;

    private String diagId = null;

    private String magic = null;

    private URL edHelpURL = null;

    private TopsEditor editor = null;

    private String host;

    private int port;

    /* END instance variables */

    @Override
    public void init() {

        this.setFont(new Font("TimesRoman", Font.BOLD, ResultsApp.LARGE_FONT_SIZE));
        this.setLayout(new GridLayout(1, 2));
        this.setBackground(Color.white);

        Panel p = new Panel();
        p.setLayout(new GridLayout(5, 1));
        p.setBackground(Color.white);
        Label l = new Label("Enter your four letter diagram identifier:");
        l.setAlignment(Label.LEFT);
        p.add(l);

        this.diagIdInput = new TextField(20);
        this.diagIdInput.setEditable(true);
        p.add(this.diagIdInput);

        l = new Label("Enter your magic number:");
        l.setAlignment(Label.LEFT);
        p.add(l);

        this.magicInput = new TextField(20);
        this.magicInput.setEditable(true);
        p.add(this.magicInput);

        Button viewb = new Button("View cartoon");
        p.add(viewb);
        viewb.addActionListener(this);

        this.add(p);

        this.errorTextArea = new TextArea("Messages from the Tops Server\n");
        this.errorTextArea.setEditable(false);

        this.add(this.errorTextArea);

        /* set up host and port */
        URL docBase = this.getDocumentBase();
        this.host = docBase.getHost();
        this.port = docBase.getPort();
        if (this.port <= 0)
            this.port = 80;

        String dcbFile = docBase.getFile();
        StringBuilder path = new StringBuilder();

        boolean copy;
        int i;
        for (i = (dcbFile.length() - 1), copy = false; i >= 0; i--) {
            if (dcbFile.charAt(i) == '/')
                copy = true;
            if (copy) {
                path.insert(0, dcbFile.charAt(i));
            }
        }

        try {
            this.edHelpURL = new URL("http", this.host, this.port, path.toString()
                    + ResultsApp.EDITOR_HELP_FILE);
        } catch (MalformedURLException mue) {
            this.edHelpURL = null;
        }

    }

    @Override
    public void stop() {
        clean();
    }

    @Override
    public void destroy() {
        clean();
    }
    
    private void clean() {
        // just dispose of any windows created
        if (this.editor == null)
            return;
        this.editor.quit();
        this.editor = null;        
    }

    /* this class only listens for action events from the magic input field */
    public void actionPerformed(ActionEvent e) {

        this.diagId = this.diagIdInput.getText().trim();
        this.magic = this.magicInput.getText().trim();

        if (this.editor != null) {
            this.editor.quit();
            this.editor = null;
        }

        String status = null;

        try {
            status = this.getTOPSstatus(this.diagId, this.magic);
        } catch (MalformedURLException mue) {
            this.errorTextArea.append("There is a problem with the status URL.\n");
            this.errorTextArea
                    .append("This may be a server bug, please contact the administrator.\n");
            return;
        } catch (IOException ioe) {
            this.errorTextArea
                    .append("There is a problem reading your cartoon status.\n");
            this.errorTextArea.append("Your magic number may be wrong.\n");
            return;
        }

        if (status == null) {
            this.errorTextArea
                    .append("There is a problem reading your cartoon status.\n");
            this.errorTextArea.append("Your magic number may be wrong.\n");
            return;
        }

        Protein p = null;
        if (status.equals("Successful")) {
            try {
                p = this.getTOPSdiag(this.diagId, this.magic);
            } catch (MalformedURLException mue) {
                this.errorTextArea.append("There is a problem with the calculated cartoon URL.\n");
                this.errorTextArea.append("This may be a server bug, please contact the administrator.\n");
                return;
            } catch (IOException ioe) {
                this.errorTextArea.append("There is a problem reading your cartoon.\n");
                this.errorTextArea.append("Your cartoon identifier might not match your magic number.\n");
                return;
            }

            if (p != null) {
                this.errorTextArea.append("Cartoon found!\n");
                this.errorTextArea.append("Setting up viewer and editor!\n");
                this.editor = new TopsEditor(this, this.edHelpURL);
                this.editor.addProtein(p);
            } else {
                this.errorTextArea.append("There is a problem reading your cartoon.\n");
                this.errorTextArea.append("Your cartoon identifier might not match your magic number.\n");
            }
        } else if (status.equals("Processing")) {
            this.errorTextArea.append("Your cartoon is not yet ready.\n");
            this.errorTextArea.append("Please try again later.\n");
        } else {
            this.errorTextArea.append("Your cartoon generation failed.\n");
            this.errorTextArea.append("Please check the format of your PDB file.\n");
            this.errorTextArea.append("If the format is OK, then contact us.\n");
        }

    }

    public void printPostscript(List<String> ps) {

        StringBuilder urlbase = new StringBuilder(ResultsApp.TOPS_FILE_URL_BASE);
        urlbase.append(this.magic);
        urlbase.append("/");
        urlbase.append(this.diagId);

        AppletPSPrinter apsp = new AppletPSPrinter(ps, this.host, this.port,
                ResultsApp.CGI_PROG_PATH, ResultsApp.CGI_PS_PRINT_PROG, urlbase.toString());

        try {
            apsp.doPrint();
            urlbase.append(".pdf");
            URL printURL = new URL(urlbase.toString());
            this.getAppletContext().showDocument(printURL, "_blank");
        } catch (IOException ioe) {
            this.errorTextArea.append("Error encoutered while trying to print\n");
            this.errorTextArea.append(ioe.getMessage());
            this.errorTextArea.append("\n");
        }

    }

    private Protein getTOPSdiag(String diagramId, String magic) throws IOException {
        StringBuilder topsfileUrlSb = new StringBuilder(ResultsApp.TOPS_FILE_URL_BASE);
        topsfileUrlSb.append(magic);
        topsfileUrlSb.append("/");
        topsfileUrlSb.append(diagramId);
        topsfileUrlSb.append(".tops");

        URL topsfileUrl = new URL(topsfileUrlSb.toString());
        InputStream tis = topsfileUrl.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(tis));
        TopsFileReader topsFileReader = new TopsFileReader();
        return topsFileReader.readTopsFile(br);
    }

    private String getTOPSstatus(String diagramId, String magic) throws IOException {

        StringBuilder topsfileUrlSb = new StringBuilder(ResultsApp.TOPS_FILE_URL_BASE);
        topsfileUrlSb.append(magic);
        topsfileUrlSb.append("/");
        topsfileUrlSb.append("status");

        URL topsfileUrl = new URL(topsfileUrlSb.toString());
        InputStream tis = topsfileUrl.openStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(tis))) {
            return br.readLine();
        }
    }

}
