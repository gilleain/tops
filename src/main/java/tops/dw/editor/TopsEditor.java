package tops.dw.editor;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PrintJob;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import tops.dw.app.ImagePrinter;
import tops.dw.io.TopsFileReader;
import tops.dw.io.TopsFileWriter;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.ProteinChoice;
import tops.dw.protein.SecStrucElement;
import tops.dw.protein.TopsFileFormatException;
import tops.dw.protein.TopsLinkedListException;
import tops.port.model.DomainDefinition;
import tops.web.display.applet.TopsDrawCanvas;

/**
 * the Tops diagram editor (controls various java bean components which do
 * display and editing of Tops diagrams)
 * 
 * @author David Westhead
 * @version 1.00 21 Apr. 1997
 */
public class TopsEditor implements ActionListener {

    private static Properties printprefs = new Properties();

    private Frame f;

    private TopsDisplayScroll topsDisplay;

    private DomainInfoScroll domainInfo;

    private ColourChoice colourChoice;

    private FileList filelist = null;

    private Vector<Protein> proteins;

    private boolean appletMode = false;

    private Applet controlApplet = null;

    private URL helpURL = null;

    static final String READ_TOPS = "READ_TOPS";

    static final String READ_DSSP = "READ_DSSP";

    static final String WRITE_TOPS = "WRITE_TOPS";

    static final String QUIT = "QUIT";

    static final String SET_NEW_FILE_LIST = "SET_NEW_FILE_LIST";

    static final String NEXT_FILE = "NEXT_FILE";

    static final String PREVIOUS_FILE = "PREVIOUS_FILE";

    static final String CLEAR_DISPLAY = "CLEAR_DISPLAY";

    static final String SCALE_DISPLAY = "SCALE_DISPLAY";

    static final String SHOW_COLOUR_CHOICE = "SHOW_COLOUR_CHOICE";

    static final String HIDE_COLOUR_CHOICE = "HIDE_COLOUR_CHOICE";

    static final String SHOW_PRINT_DIALOG = "SHOW_PRINT_DIALOG";

    static final String WRITE_EPS = "WRITE_EPS";

    static final String WRITE_PS = "WRITE_PS";

    static final String ORIENT_CARTOONS = "ORIENT_CARTOONS";

    static final String COLOUR_ALIGN = "COLOUR_ALIGN";

    static final String HELP_EDITOR = "HELP_EDITOR";

    /**
     * basic constructor
     */
    public TopsEditor(boolean AppMode, String[] argv) {

        this.appletMode = AppMode;
        

        MenuItem mi;

        this.f = new Frame("Tops diagram viewer and editor");
        this.f.setLayout(new BorderLayout());

        this.f.addWindowListener(new WindowAdapter() {
        	public void windowClosed(WindowEvent e) {
        		quit();
        	}
        });

        this.topsDisplay = new TopsDisplayScroll();
        this.f.add("Center", this.topsDisplay);

        Menu FileMenu = new Menu("File");
        mi = new MenuItem("Read tops file");
        mi.setActionCommand(TopsEditor.READ_TOPS);
        mi.addActionListener(this);
        if (!this.appletMode)
            FileMenu.add(mi);

        /*
         * this section commented out for release (June 1998) add back in again
         * for use as GUI to Tops C code
         */ 
         mi = new MenuItem("Read DSSP file");
         mi.setActionCommand(TopsEditor.READ_DSSP); 
         mi.addActionListener(this); 
         if ( !appletMode )
        	 FileMenu.add(mi);

        mi = new MenuItem("Write tops file");
        mi.setActionCommand(TopsEditor.WRITE_TOPS);
        mi.addActionListener(this);
        if (!this.appletMode)
            FileMenu.add(mi);
        mi = new MenuItem("Quit");
        mi.setActionCommand(TopsEditor.QUIT);
        mi.addActionListener(this);
        FileMenu.add(mi);

        Menu FileListMenu = new Menu("FileList");

        /*
         * this section commented out for release (June 1998) not yet
         * implemented 
         */
        mi = new MenuItem("Set new file list");
//        mi.setEnabled(false); 
        mi.setActionCommand(TopsEditor.SET_NEW_FILE_LIST ); 
        mi.addActionListener(this);
        FileListMenu.add(mi);

        mi = new MenuItem("Next tops file");
        mi.setActionCommand(TopsEditor.NEXT_FILE);
        mi.addActionListener(this);
        FileListMenu.add(mi);
        if (this.filelist == null)
            mi.setEnabled(false);
        mi = new MenuItem("Previous tops file");
        mi.setActionCommand(TopsEditor.PREVIOUS_FILE);
        mi.addActionListener(this);
        FileListMenu.add(mi);
        if (this.filelist == null)
            mi.setEnabled(false);

        Menu DisplayMenu = new Menu("Display");
        mi = new MenuItem("Clear display");
        mi.setActionCommand(TopsEditor.CLEAR_DISPLAY);
        mi.addActionListener(this);
        DisplayMenu.add(mi);

        /*
         * this section commented out for release (June 1998) not yet
         * implemented*/
        mi = new MenuItem("Scale display"); 
        //mi.setEnabled(false);
        mi.setActionCommand(TopsEditor.SCALE_DISPLAY);
        mi.addActionListener(this); 
        DisplayMenu.add(mi);

        mi = new MenuItem("Show colour choice");
        mi.setActionCommand(TopsEditor.SHOW_COLOUR_CHOICE);
        mi.addActionListener(this);
        
        DisplayMenu.add(mi);
        mi = new MenuItem("Hide colour choice");
        mi.setActionCommand(TopsEditor.HIDE_COLOUR_CHOICE);
        mi.addActionListener(this);
        DisplayMenu.add(mi);

        Menu PrintMenu = new Menu("Print");

        /*
         * this section commented out for release (June 1998) less confusing for
         * users if only one way to get postscript */ 
         mi = new MenuItem("Show print dialog"); 
         mi.setActionCommand(TopsEditor.SHOW_PRINT_DIALOG);
         mi.addActionListener(this); 
         if ( !appletMode ) PrintMenu.add(mi);

        if (!this.appletMode) {
            mi = new MenuItem(
                    "Write Encapsulated Postscript file (for single domain/cartoon)");
            mi.setActionCommand(TopsEditor.WRITE_EPS);
            mi.addActionListener(this);
            PrintMenu.add(mi);
            mi = new MenuItem("Write Postscript file (for entire display)");
            mi.setActionCommand(TopsEditor.WRITE_PS);
            mi.addActionListener(this);
            PrintMenu.add(mi);
        } else {
            mi = new MenuItem("Produce downloadable PDF file of the display");
            mi.setActionCommand(TopsEditor.WRITE_PS);
            mi.addActionListener(this);
            PrintMenu.add(mi);
        }

        Menu AlignMenu = new Menu("Alignment");
        mi = new MenuItem("Orient cartoons according to alignment");
        if (this.appletMode)
            mi.setEnabled(false);
        mi.setActionCommand(TopsEditor.ORIENT_CARTOONS);
        mi.addActionListener(this);
        AlignMenu.add(mi);
        mi = new MenuItem("Colour cartoons according to alignment");
        if (this.appletMode)
            mi.setEnabled(false);
        mi.setActionCommand(TopsEditor.COLOUR_ALIGN);
        mi.addActionListener(this);
        AlignMenu.add(mi);

        Menu HelpMenu = new Menu("Help");
        mi = new MenuItem("Help on editor functions");
        if (!this.appletMode)
            mi.setEnabled(false);
        mi.setActionCommand(TopsEditor.HELP_EDITOR);
        mi.addActionListener(this);
        HelpMenu.add(mi);

        MenuBar menubar = new MenuBar();
        menubar.setFont(new Font("TimesRoman", Font.BOLD + Font.ITALIC, 12));
        menubar.add(FileMenu);
        if (!this.appletMode)
            menubar.add(FileListMenu);
        menubar.add(DisplayMenu);
        menubar.add(PrintMenu);
        menubar.add(AlignMenu);
        menubar.add(HelpMenu);
        menubar.setHelpMenu(HelpMenu);

        this.f.setMenuBar(menubar);

        this.domainInfo = new DomainInfoScroll(this);
        this.f.add("South", this.domainInfo);

        this.f.pack();
        this.f.setVisible(true);
        this.proteins = new Vector<Protein>();

        this.colourChoice = new ColourChoice();
        this.colourChoice.setVisible(false);
        
        this.filelist = this.handleArgv(argv);
    }

    public TopsEditor(Applet applt, URL help) {
        this(true, null);
        this.controlApplet = applt;
        this.helpURL = help;
    }

	public void addProtein(Protein p) {
        this.proteins.addElement(p);
        this.topsDisplay.addDiagrams(p);
        this.domainInfo.addProtein(p);
        this.colourChoice.addColourChangeListeners(
        		(Vector<? extends PropertyChangeListener>)this.topsDisplay.GetDrawCanvases());

        this.f.pack();
    }

    public void clearDisplay() {
        this.proteins = new Vector<Protein>();
        this.topsDisplay.clear();
        this.domainInfo.Clear();
    }

    public void scaleDisplay() {
        int scale = 100;

        IntegerInDialog iid = new IntegerInDialog(this.f, "Input scale",
                "Please input a scale value as percentage", 100);
        iid.setVisible(true);
        scale = iid.getInput();
        if ((scale <= 0) || (scale > 100))
            scale = 100;

        this.topsDisplay.scaleDisplay(scale);
    }

    /**
     * set the visibility of the ColourChoice
     */
    public void setColChoiceVisible(boolean vis) {
        if (this.colourChoice != null)
            this.colourChoice.setVisible(vis);
    }

    /**
     * close the application
     */
    public void quit() {

        if (this.f != null) {
            this.f.dispose();
            this.f = null;
        }
        if (this.colourChoice != null) {
            this.colourChoice.dispose();
            this.colourChoice = null;
        }

        if (!this.appletMode) {
            Runtime rt = Runtime.getRuntime();
            try {
                rt.exit(0);
            } catch (SecurityException e) {
            }
        }

    }

    /**
     * display help information
     */
    public void help() {
        if (this.appletMode) {

            if ((this.controlApplet != null) && (this.helpURL != null)) {
                this.controlApplet.getAppletContext()
                        .showDocument(this.helpURL, "_blank");
            } else {
            	this.error("Sorry, no help is available at the moment");
            }
        } else {
        	this.error("Sorry, no help is available at the moment");
        }
    }
    
    public void error(String error) {
    	TopsErrorFrame tef = new TopsErrorFrame(error);
    	System.out.println(error);
    	tef.setVisible(true);
    }

    /**
     * the command that gets a file dialog and reads a tops file
     */
    public void readTopsFile() {

        FileDialog fd = new FileDialog(this.f, "Read Tops file", FileDialog.LOAD);
        fd.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        fd.setFilenameFilter(new TopsFileFilter());
        fd.setVisible(true);
        

        String filename = fd.getFile();
        if (filename == null) {
            return;
        }
        
        String directoryname = fd.getDirectory();
        File file = new File(directoryname, filename);

        Protein p = this.readTopsFile(file);
        if (p != null)
            this.addProtein(p);

    }
    
    public void setNewFileList() {
    	FileDialog fd = new FileDialog(this.f, "Set Tops File List", FileDialog.LOAD);
    	fd.setFont(new Font("TimesRoman", Font.PLAIN, 18));
    	fd.setVisible(true);
    	
    	String filename = fd.getFile();
        if (filename != null) {
            return;
        }
        
        String directoryname = fd.getDirectory();
        File file = new File(directoryname);
        
        this.filelist = new FileList(file.list());
    }

    /**
     * the command that moves to the next tops file on the list
     */
    public void nextFile() {
        Protein p = null;
        File f = null;

        if (this.filelist != null)
            f = this.filelist.getNextFile();
        if (f != null)
            p = this.readTopsFile(f);

        if (p != null) {
            this.clearDisplay();
            this.addProtein(p);
        }

    }

    /**
     * the command that moves to the previous tops file on the list
     */
    public void previousFile() {
        Protein p = null;
        File f = null;

        if (this.filelist != null)
            f = this.filelist.getPreviousFile();
        if (f != null)
            p = this.readTopsFile(f);

        if (p != null) {
            this.clearDisplay();
            this.addProtein(p);
        }
    }

    /**
     * the command that writes a .tops format file
     */
    public void writeTopsFile() {

        ProteinChoice pc = new ProteinChoice(this.f,
                "Choose the tops.dw.protein to write to tops file", this.proteins);
        pc.setVisible(true);
        Protein wr_prot = pc.getChoice();

        if (wr_prot != null) {

            FileDialog fd = new FileDialog(this.f, "Write Tops file",
                    FileDialog.SAVE);
            fd.setFont(new Font("TimesRoman", Font.PLAIN, 18));
            fd.setVisible(true);

            String file = fd.getFile();
            if (file == null)
                return;

            String dir = fd.getDirectory();

            FileOutputStream fos;
            try {
                fos = new FileOutputStream(dir + file);
            } catch (IOException e) {
                this.error("Problem writing file " + dir + file);
                return;
            }

            if (this.topsDisplay != null) {
                Vector<tops.web.display.applet.TopsDrawCanvas> dcs = this.topsDisplay.GetDrawCanvases();
                if (dcs != null) {
                    Enumeration<tops.web.display.applet.TopsDrawCanvas> dcenum = dcs.elements();
                    tops.web.display.applet.TopsDrawCanvas dc;
                    while (dcenum.hasMoreElements()) {
                        dc =  dcenum.nextElement();
                        dc.SetCCodeCoordinates();
                    }
                    if (wr_prot != null) {
                        TopsFileWriter topsFileWriter = new TopsFileWriter();
                        topsFileWriter.writeTopsFile(wr_prot, fos);
                    }
                    dcenum = dcs.elements();
                    while (dcenum.hasMoreElements()) {
                        dc = dcenum.nextElement();
                        dc.SetCanvasCoordinates();
                    }

                }
            }
        }

    }

    public void writeEPSFile() {
        if (this.proteins == null)
            this.proteins = new Vector<Protein>();

        List<String> strs = new ArrayList<String>();
        List<Cartoon> diags = new ArrayList<Cartoon>();

        Enumeration<Protein> prots = this.proteins.elements();
        while (prots.hasMoreElements()) {
            Protein p = prots.nextElement();
            List<DomainDefinition> doms = p.getDomainDefs();
            List<Cartoon> lls = p.getLinkedLists();
            for (int i = 0; i < doms.size(); i++) {
                strs.add(doms.get(i).toString());
                diags.add(lls.get(i));
            }
        }

        StringChoice stc = new StringChoice(this.f, "Select domain", strs);
        stc.setVisible(true);
        String Chosen = stc.getChoice();
        int ChosenNum = stc.getChoiceNumber();

        if (Chosen != null) {
            TopsDrawCanvas DrawCanvToPrint = this.topsDisplay.GetDrawCanvas(diags.get(ChosenNum));

            if (DrawCanvToPrint != null) {

                Vector<String> eps = DrawCanvToPrint.getEPS();

                if (!this.appletMode) {
                    FileDialog fd = new FileDialog(this.f, "Choose EPS filename",
                            FileDialog.SAVE);
                    fd.setFont(new Font("TimesRoman", Font.PLAIN, 18));
                    fd.setVisible(true);

                    String file = fd.getFile();
                    if (file == null)
                        return;

                    String dir = fd.getDirectory();

                    PrintWriter pw;
                    try {
                        FileOutputStream fos = new FileOutputStream(dir + file);
                        pw = new PrintWriter(fos);
                    } catch (IOException e) {
                        this.error("Problem writing file " + dir + file);
                        return;
                    }

                    Enumeration<String> en = eps.elements();
                    while (en.hasMoreElements()) {
                        pw.println((String) en.nextElement());
                    }

                    pw.close();
                } else {

                    if (this.controlApplet != null) {
                        PleaseWaitFrame pwf = new PleaseWaitFrame(
                                "Printing ... (may take up to 30 seconds)");
                        pwf.setVisible(true);
                        pwf.toFront();
                        PostscriptPrinter psp = (PostscriptPrinter) this.controlApplet;
                        psp.printPostscript(eps);
                        pwf.dispose();
                    }

                }
            }

        }

    }

    public void writePSFile() {

        // form the postscript
        Vector<tops.web.display.applet.TopsDrawCanvas> dcs = this.topsDisplay.GetDrawCanvases();
        Vector<Vector<String>> EPSS = new Vector<Vector<String>>();
        Vector<String> titles = new Vector<String>();
        Enumeration<tops.web.display.applet.TopsDrawCanvas> endcs = dcs.elements();
        tops.web.display.applet.TopsDrawCanvas tdc;
        while (endcs.hasMoreElements()) {
        	tdc = endcs.nextElement();
        	EPSS.addElement(tdc.getEPS());
        	titles.addElement(tdc.getLabel());
        }

        Vector<String> PS;
        try {
            PS = PostscriptFactory.PSArrayA4(titles, EPSS, 54, 0.5f);
        } catch (PSException pse) {
            this.error(pse.Message);
            return;
        }

        // get an output file
        if (!this.appletMode) {
            FileDialog fd = new FileDialog(
            		this.f, "Choose PS filename",FileDialog.SAVE);
            fd.setFont(new Font("TimesRoman", Font.PLAIN, 18));
            fd.setVisible(true);

            String file = fd.getFile();
            if (file == null)
                return;

            String dir = fd.getDirectory();

            PrintWriter pw;
            try {
                FileOutputStream fos = new FileOutputStream(dir + file);
                pw = new PrintWriter(fos);
            } catch (IOException e) {
                System.out.println("An IOException was caught in WritePS");
                this.error("Problem writing file " + dir + file);
                return;
            }

            Enumeration<String> enps = PS.elements();
            while (enps.hasMoreElements()) {
                pw.println((String) enps.nextElement());
            }
            pw.close();
        } else {
            if (this.controlApplet != null) {
                PleaseWaitFrame pwf = new PleaseWaitFrame(
                        "Making PDF ... (may take up to 30 seconds)");
                pwf.setVisible(true);
                pwf.toFront();
                PostscriptPrinter psp = (PostscriptPrinter) this.controlApplet;
                psp.printPostscript(PS);
                pwf.dispose();
            }
        }
    }

    // reads a file specifying equivalences between SSEs in a set of related
    // domains and colours these cartoons according to the equivalence
    public void colourAlign() {
        if (this.proteins == null || this.proteins.isEmpty()) {
            System.out.println("No cartoons to orient !!! ");
            return;
        }

        FileDialog fd = 
        	new FileDialog(this.f, "Read SSE alignment file", FileDialog.LOAD);
        fd.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        fd.setVisible(true);

        String file = fd.getFile();
        if (file == null)
            return;

        try {
        	OrientInfo oi = new OrientInfo(new File(file));
            if (oi.hasMapping()) {
            	this.colourAlign(oi);
            } else {
                return;
            }
        } catch (EquivFileFormatException effe) {
            this.error("Format error in file " + file);
            return;
        } catch (IOException ioe) {
            this.error("IO error reading file " + file);
            return;
        }
    }
    
    public void colourAlign(OrientInfo oi) {
    	// colour equivalences
        int i = 0;
        Enumeration<String> orient_names = oi.getNames();
        while (orient_names.hasMoreElements()) {
            String domname = (String) orient_names.nextElement();
            Cartoon root = this.getRootSSE(domname);
            if (root == null) {
                this.error("Domain " + domname + " not found");
                break;
            }
            for (int j = 0; j < oi.numberOfMappings(); j++) {
                SecStrucElement s = root.GetSSEByNumber(oi.getMapping(i, j));
                if (s == null) {
                    this.error("SSE number out of range in equivalences file");
                    return;
                } else {
                    s.setColour(Color.lightGray);
                }
            }
            i++;
        }

        // do repaints
        this.topsDisplay.repaint();
    }

    // reads a file specifying equivalences between SSEs in a set of related
    // domains and orients these cartoons according to the equivalence
    public void orientCartoons() {
        if (this.proteins == null || this.proteins.isEmpty()) {
            System.out.println("No cartoons to orient !!! ");
            return;
        }

        FileDialog fd = new FileDialog(
        		this.f, "Read SSE alignment file",FileDialog.LOAD);
        fd.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        fd.setVisible(true);

        String file = fd.getFile();
        if (file == null)
            return;

        try {
        	OrientInfo oi = new OrientInfo(new File(file));
            if (oi.hasMapping()) {
            	this.orientCartoons(oi);
            } else {
            	return;
            }
        } catch (EquivFileFormatException effe) {
            this.error("Format error in file " + file);
            return;
        } catch (IOException ioe) {
            this.error("IO error reading file " + file);
            return;
        }

        
    }
    
    public void orientCartoons(OrientInfo oi) {

        // everything is oriented w.r.t. a reference - the first domain in the
        // equivalence file
        Enumeration<String> orient_names = oi.getNames();
        String refdomname = orient_names.nextElement();
        Cartoon refdomroot = this.getRootSSE(refdomname);
        if (refdomroot == null) {
            this.error("Domain " + refdomname + " not found");
            return;
        }

        // orient all other domains in equivalence file w.r.t. reference
        Cartoon root;
        String domname;
        int i = 0;
        try {
            int[] reference = oi.getMapping(0);
            while (orient_names.hasMoreElements()) {
                i++;
                domname = orient_names.nextElement();
                root = this.getRootSSE(domname);
                if (root == null) {
                    this.error("Domain " + domname + " not found");
                    break;
                }
                TopsDrawCanvas tdc = this.topsDisplay.GetDrawCanvas(root);
                oi.orientConsensus(refdomroot, root, reference, oi.getMapping(i), tdc);
            }
        } catch (TopsLinkedListException tle) {
            System.out.println("Exception caught in orientCartoons");
        } finally {
            // do repaints
            this.topsDisplay.repaint();
        }

    }

    private Cartoon getRootSSE(String dom_name) {
        if (this.proteins == null)
            return null;

        Enumeration<Protein> ps = this.proteins.elements();

        while (ps.hasMoreElements()) {
            Protein p = (Protein) ps.nextElement();
            Cartoon sseRoot = p.getRootSSE(dom_name);
            if (sseRoot != null) {
            	return sseRoot;
            }
        }

        return null;

    }

    public void print() {

        if (this.proteins == null)
            this.proteins = new Vector<Protein>();

        List<String> strs = new ArrayList<String>();
        List<Cartoon> diags = new ArrayList<Cartoon>();

        Enumeration<Protein> prots = this.proteins.elements();
        while (prots.hasMoreElements()) {
            Protein p = (Protein) prots.nextElement();
            List<DomainDefinition> doms = p.getDomainDefs();
            List<Cartoon> lls = p.getLinkedLists();
            for (int i = 0; i < doms.size(); i++) {
                strs.add(doms.get(i).toString());
                diags.add(lls.get(i));
            }
        }

        StringChoice stc = new StringChoice(this.f, "Select domain to print", strs);
        stc.setVisible(true);
        String Chosen = stc.getChoice();
        int ChosenNum = stc.getChoiceNumber();

        if (Chosen != null) {
            TopsDrawCanvas DrawCanvToPrint = 
                    this.topsDisplay.GetDrawCanvas(diags.get(ChosenNum));

            if (DrawCanvToPrint != null) {

                if (!this.appletMode) {

                    PrintJob pjob = this.f.getToolkit().getPrintJob(this.f,
                            "Print TOPS diagram", TopsEditor.printprefs);
                    if (pjob == null)
                        return;
                    Graphics page = pjob.getGraphics();

                    Dimension canv_size = DrawCanvToPrint.getSize();
                    Dimension page_size = pjob.getPageDimension();

                    page.translate((page_size.width - canv_size.width) / 2,
                            (page_size.height - canv_size.height) / 2);

                    DrawCanvToPrint.print(page);

                    page.dispose();
                    pjob.end();
                } else {
                    if (this.controlApplet != null) {
                        PleaseWaitFrame pwf = new PleaseWaitFrame(
                                "Printing ... (may take up to 30 seconds)");
                        pwf.setVisible(true);
                        pwf.toFront();
                        ImagePrinter ip = (ImagePrinter) this.controlApplet;
                        ip.printImage(DrawCanvToPrint.getImage());
                        pwf.dispose();
                    }
                }
            }
        }
    }

    public void processDSSPFile() {

        FileDialog fd = new FileDialog(this.f, "Read DSSP file", FileDialog.LOAD);
        fd.setFont(new Font("TimesRoman", Font.PLAIN, 18));
        fd.setFilenameFilter(new DSSPFileFilter());
        fd.setVisible(true);

        String file = fd.getFile();
        if (file == null)
            return;

        int i;
        StringBuffer sb = new StringBuffer();
        for (i = 0; i < file.length(); i++) {
            if (file.charAt(i) == '.')
                break;
            sb.append(file.charAt(i));
        }

        String pc = sb.toString();
        String tmpfile = this.randomTopsFile();
        String TopsCommand = "Topsf " + pc + " -s " + tmpfile;

        Runtime runtime = Runtime.getRuntime();

        PleaseWaitFrame pwf = new PleaseWaitFrame(
                "Please wait while I process the dssp file ... ");
        pwf.setVisible(true);
        System.out.println("Starting execution: " + TopsCommand);
        Process proc;
        try {
            proc = runtime.exec(TopsCommand);
            BufferedReader br = new BufferedReader(new InputStreamReader(proc
                    .getInputStream()));
            BufferedReader bre = new BufferedReader(new InputStreamReader(proc
                    .getErrorStream()));
            String line = br.readLine();
            while (line != null) {
                System.out.println(line);
                line = br.readLine();
            }
            line = bre.readLine();
            while (line != null) {
                System.out.println(line);
                line = bre.readLine();
            }
            proc.waitFor();
            System.out.println("Finished execution");
            pwf.dispose();
        } catch (IOException e) {
            System.out.println("exec threw an IOException");
            pwf.dispose();
            return;
        } catch (InterruptedException e) {
            System.out.println("waitFor threw an InterrputedException");
            pwf.dispose();
            return;
        }

        File tmpf = new File(tmpfile);
        Protein p = this.readTopsFile(tmpf);
        if (p != null) {
            this.addProtein(p);
        }
        tmpf.delete();

    }

    public TopsDisplayScroll getDisplayScroll() {
        return this.topsDisplay;
    }

    /**
     * a private method which reads the tops file and catches any exceptions
     */
    private Protein readTopsFile(File f) {

        Protein p = null;
        TopsFileReader topsFileReader = new TopsFileReader();
        try {
            p = topsFileReader.readTopsFile(f);
        } catch (FileNotFoundException e) {
            this.error("Unable to find file " + f);
            return null;
        } catch (TopsFileFormatException e) {
        	this.error("Format error in file " + f);
            return null;
        } catch (IOException e) {
        	this.error("Problem reading file " + f);
            return null;
        }

        return p;

    }

    private String randomTopsFile() {
        double rd = Math.random();
        long ri = Math.round(rd * 1000000000.0);
        String s = "tmp" + ri + ".tops";
        return s;
    }
    
    public void actionPerformed(ActionEvent e) {

    	String command = e.getActionCommand(); 
        if (command.equals(READ_TOPS)) {
        	this.readTopsFile();
        } else if (command.equals(WRITE_TOPS)) {
        	this.writeTopsFile();
        } else if (command.equals(READ_DSSP)) {
        	this.processDSSPFile();
        } else if (command.equals(QUIT)) {
        	this.quit();
        } else if (command.equals(SET_NEW_FILE_LIST)) {
        	this.setNewFileList();
        } else if (command.equals(NEXT_FILE)) {
        	this.nextFile();
        } else if (command.equals(PREVIOUS_FILE)) {
        	this.previousFile();
        } else if (command.equals(CLEAR_DISPLAY)) {
        	this.clearDisplay();
        } else if (command.equals(SCALE_DISPLAY)) {
        	this.scaleDisplay();
        } else if (command.equals(SHOW_COLOUR_CHOICE)) {
        	this.setColChoiceVisible(true);
        } else if (command.equals(HIDE_COLOUR_CHOICE)) {
        	this.setColChoiceVisible(false);
        } else if (command.equals(SHOW_PRINT_DIALOG)) {
        	this.print();
        } else if (command.equals(WRITE_EPS)) {
        	this.writeEPSFile();
        } else if (command.equals(WRITE_PS)) {
        	this.writePSFile();
        } else if (command.equals(ORIENT_CARTOONS)) {
        	this.orientCartoons();
        } else if (command.equals(COLOUR_ALIGN)) {
        	this.colourAlign();
        } else if (command.equals(HELP_EDITOR)) {
        	this.help();
        }
    }
    
    public FileList handleArgv(String[] argv) {
    	 FileList list = null;
         File cf = null;

         /* handle the command line */
         /* if an argument is present it must be a directory or a file */
         if ((argv != null) && (argv.length > 0) && (argv[0] != null)) {
             File f = new File(argv[0]);
             if (f.exists()) {
                 if (f.isDirectory()) {
                     String[] sfilelist = f.list(new TopsFileFilter());
                     if (sfilelist != null) {
                         list = new FileList(argv[0], sfilelist);
                         cf = list.getCurrentFile();
                     }
                 } else if (f.isFile()) {
                     cf = f;
                 } else {
                	 this.error("The input argument " 
                    		 + argv[0] + " is not an existing file or  directory");
                 }
             } else {
                 this.error("The input argument " 
                		 + argv[0] + " is not an existing file or  directory");
             }
         }
         Protein p = null;
         if (cf != null)
             p = this.readTopsFile(cf);
         if (p != null)
             this.addProtein(p);
         
         return list;
    }

}

class TopsFileFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return name.endsWith(".tops");
    }
}

class DSSPFileFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return name.endsWith(".dssp");
    }
}



