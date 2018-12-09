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
import java.util.logging.Logger;

import tops.dw.app.ImagePrinter;
import tops.dw.io.TopsFileReader;
import tops.dw.io.TopsFileWriter;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.ProteinChoice;
import tops.dw.protein.SecStrucElement;
import tops.dw.protein.TopsFileFormatException;
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
    
    private Logger log = Logger.getLogger(TopsEditor.class.getName());

    private static final String TIMES_ROMAN = "TimesRoman";

    private static Properties printprefs = new Properties();

    private Frame f;

    private TopsDisplayScroll topsDisplay;

    private DomainInfoScroll domainInfo;

    private ColourChoice colourChoice;

    private FileList filelist;

    private List<Protein> proteins;

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
    public TopsEditor(boolean appMode, String[] argv) {

        this.appletMode = appMode;
        

        MenuItem mi;

        this.f = new Frame("Tops diagram viewer and editor");
        this.f.setLayout(new BorderLayout());

        this.f.addWindowListener(new WindowAdapter() {
            @Override
        	public void windowClosed(WindowEvent e) {
        		quit();
        	}
        });

        this.topsDisplay = new TopsDisplayScroll();
        this.f.add("Center", this.topsDisplay);

        Menu fileMenu = new Menu("File");
        mi = new MenuItem("Read tops file");
        mi.setActionCommand(TopsEditor.READ_TOPS);
        mi.addActionListener(this);
        if (!this.appletMode)
            fileMenu.add(mi);

        /*
         * this section commented out for release (June 1998) add back in again
         * for use as GUI to Tops C code
         */ 
         mi = new MenuItem("Read DSSP file");
         mi.setActionCommand(TopsEditor.READ_DSSP); 
         mi.addActionListener(this); 
         if ( !appletMode )
        	 fileMenu.add(mi);

        mi = new MenuItem("Write tops file");
        mi.setActionCommand(TopsEditor.WRITE_TOPS);
        mi.addActionListener(this);
        if (!this.appletMode)
            fileMenu.add(mi);
        mi = new MenuItem("Quit");
        mi.setActionCommand(TopsEditor.QUIT);
        mi.addActionListener(this);
        fileMenu.add(mi);

        Menu fileListMenu = new Menu("FileList");

        /*
         * this section commented out for release (June 1998) not yet
         * implemented 
         */
        mi = new MenuItem("Set new file list");
        mi.setActionCommand(TopsEditor.SET_NEW_FILE_LIST ); 
        mi.addActionListener(this);
        fileListMenu.add(mi);

        mi = new MenuItem("Next tops file");
        mi.setActionCommand(TopsEditor.NEXT_FILE);
        mi.addActionListener(this);
        fileListMenu.add(mi);
        if (this.filelist == null)
            mi.setEnabled(false);
        mi = new MenuItem("Previous tops file");
        mi.setActionCommand(TopsEditor.PREVIOUS_FILE);
        mi.addActionListener(this);
        fileListMenu.add(mi);
        if (this.filelist == null)
            mi.setEnabled(false);

        Menu displayMenu = new Menu("Display");
        mi = new MenuItem("Clear display");
        mi.setActionCommand(TopsEditor.CLEAR_DISPLAY);
        mi.addActionListener(this);
        displayMenu.add(mi);

        /*
         * this section commented out for release (June 1998) not yet
         * implemented*/
        mi = new MenuItem("Scale display"); 
        mi.setActionCommand(TopsEditor.SCALE_DISPLAY);
        mi.addActionListener(this); 
        displayMenu.add(mi);

        mi = new MenuItem("Show colour choice");
        mi.setActionCommand(TopsEditor.SHOW_COLOUR_CHOICE);
        mi.addActionListener(this);
        
        displayMenu.add(mi);
        mi = new MenuItem("Hide colour choice");
        mi.setActionCommand(TopsEditor.HIDE_COLOUR_CHOICE);
        mi.addActionListener(this);
        displayMenu.add(mi);

        Menu printMenu = new Menu("Print");

        /*
         * this section commented out for release (June 1998) less confusing for
         * users if only one way to get postscript */ 
         mi = new MenuItem("Show print dialog"); 
         mi.setActionCommand(TopsEditor.SHOW_PRINT_DIALOG);
         mi.addActionListener(this); 
         if ( !appletMode ) printMenu.add(mi);

        if (!this.appletMode) {
            mi = new MenuItem(
                    "Write Encapsulated Postscript file (for single domain/cartoon)");
            mi.setActionCommand(TopsEditor.WRITE_EPS);
            mi.addActionListener(this);
            printMenu.add(mi);
            mi = new MenuItem("Write Postscript file (for entire display)");
            mi.setActionCommand(TopsEditor.WRITE_PS);
            mi.addActionListener(this);
            printMenu.add(mi);
        } else {
            mi = new MenuItem("Produce downloadable PDF file of the display");
            mi.setActionCommand(TopsEditor.WRITE_PS);
            mi.addActionListener(this);
            printMenu.add(mi);
        }

        Menu alignMenu = new Menu("Alignment");
        mi = new MenuItem("Orient cartoons according to alignment");
        if (this.appletMode)
            mi.setEnabled(false);
        mi.setActionCommand(TopsEditor.ORIENT_CARTOONS);
        mi.addActionListener(this);
        alignMenu.add(mi);
        mi = new MenuItem("Colour cartoons according to alignment");
        if (this.appletMode)
            mi.setEnabled(false);
        mi.setActionCommand(TopsEditor.COLOUR_ALIGN);
        mi.addActionListener(this);
        alignMenu.add(mi);

        Menu helpMenu = new Menu("Help");
        mi = new MenuItem("Help on editor functions");
        if (!this.appletMode)
            mi.setEnabled(false);
        mi.setActionCommand(TopsEditor.HELP_EDITOR);
        mi.addActionListener(this);
        helpMenu.add(mi);

        MenuBar menubar = new MenuBar();
        menubar.setFont(new Font(TIMES_ROMAN, Font.BOLD + Font.ITALIC, 12));
        menubar.add(fileMenu);
        if (!this.appletMode)
            menubar.add(fileListMenu);
        menubar.add(displayMenu);
        menubar.add(printMenu);
        menubar.add(alignMenu);
        menubar.add(helpMenu);
        menubar.setHelpMenu(helpMenu);

        this.f.setMenuBar(menubar);

        this.domainInfo = new DomainInfoScroll(this);
        this.f.add("South", this.domainInfo);

        this.f.pack();
        this.f.setVisible(true);
        this.proteins = new ArrayList<>();

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
        this.proteins.add(p);
        this.topsDisplay.addDiagrams(p);
        this.domainInfo.addProtein(p);
        // TODO FIXME
//        this.colourChoice.addColourChangeListeners(
//        		(Vector<? extends PropertyChangeListener>)
//        		this.topsDisplay.GetDrawCanvases());

        this.f.pack();
    }

    public void clearDisplay() {
        this.proteins.clear();
        this.topsDisplay.clear();
        this.domainInfo.Clear();
    }

    public void scaleDisplay() {
        IntegerInDialog iid = new IntegerInDialog(this.f, "Input scale",
                "Please input a scale value as percentage", 100);
        iid.setVisible(true);
        int scale = iid.getInput();
        if (scale <= 0 || scale > 100) {
            scale = 100;
        }

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
                log.warning("Security exception " + e.getMessage());
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
    	log.warning(error);
    	tef.setVisible(true);
    }

    /**
     * the command that gets a file dialog and reads a tops file
     */
    public void readTopsFile() {

        FileDialog fd = new FileDialog(this.f, "Read Tops file", FileDialog.LOAD);
        fd.setFont(new Font(TIMES_ROMAN, Font.PLAIN, 18));
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
    	fd.setFont(new Font(TIMES_ROMAN, Font.PLAIN, 18));
    	fd.setVisible(true);
    	
    	String filename = fd.getFile();
        if (filename != null) {
            return;
        }
        
        String directoryname = fd.getDirectory();
        File directory = new File(directoryname);
        
        this.filelist = new FileList(directory.list());
    }

    /**
     * the command that moves to the next tops file on the list
     */
    public void nextFile() {
        Protein p = null;
        File file = null;

        if (this.filelist != null) {
            file = this.filelist.getNextFile();
        }
        if (file != null) {
            p = this.readTopsFile(file);
        }

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
        File file = null;

        if (this.filelist != null) {
            file = this.filelist.getPreviousFile();
        }
        
        if (file != null) {
            p = this.readTopsFile(file);
        }

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
        Protein wrProtein = pc.getChoice();

        if (wrProtein != null) {

            FileDialog fd = new FileDialog(this.f, "Write Tops file", FileDialog.SAVE);
            fd.setFont(new Font(TIMES_ROMAN, Font.PLAIN, 18));
            fd.setVisible(true);

            String file = fd.getFile();
            if (file == null)
                return;

            String dir = fd.getDirectory();

            try (FileOutputStream fos = new FileOutputStream(dir + file)) {
                if (this.topsDisplay != null) {
                    List<TopsDrawCanvas> dcs = this.topsDisplay.getDrawCanvases();
                    if (dcs != null) {
                        for (TopsDrawCanvas dc : dcs) {
                            dc.setCCodeCoordinates();
                        }
                        
                        TopsFileWriter topsFileWriter = new TopsFileWriter();
                        topsFileWriter.writeTopsFile(wrProtein, fos);
                        
                        for (TopsDrawCanvas dc : dcs) {
                            dc.setCanvasCoordinates();
                        }
                    }
                }
            } catch (IOException e) {
                this.error(String.format("Problem writing file %s %s", dir, file));
            }
        }
    }

    public void writeEPSFile() {
        if (this.proteins == null)
            this.proteins = new Vector<>();

        List<String> strs = new ArrayList<>();
        List<Cartoon> diags = new ArrayList<>();

        for (Protein p : this.proteins) {
            List<DomainDefinition> doms = p.getDomainDefs();
            List<Cartoon> lls = p.getLinkedLists();
            for (int i = 0; i < doms.size(); i++) {
                strs.add(doms.get(i).toString());
                diags.add(lls.get(i));
            }
        }

        StringChoice stc = new StringChoice(this.f, "Select domain", strs);
        stc.setVisible(true);
        String chosen = stc.getChoice();
        int chosenNum = stc.getChoiceNumber();

        if (chosen != null) {
            TopsDrawCanvas drawCanvToPrint = this.topsDisplay.getDrawCanvas(diags.get(chosenNum));

            if (drawCanvToPrint != null) {

                String eps = null;
                try {
                    eps = drawCanvToPrint.getEPS();
                } catch (IOException ioe) {
                    log.warning("IO error writing eps file " + ioe.getMessage());
                }

                if (!this.appletMode) {
                    FileDialog fd = new FileDialog(this.f, "Choose EPS filename", FileDialog.SAVE);
                    fd.setFont(new Font(TIMES_ROMAN, Font.PLAIN, 18));
                    fd.setVisible(true);

                    String file = fd.getFile();
                    if (file == null)
                        return;

                    String dir = fd.getDirectory();

                    PrintWriter pw;
                    try (FileOutputStream fos = new FileOutputStream(dir + file)) {
                        pw = new PrintWriter(fos);
                        pw.println(eps);
                    } catch (IOException e) {
                        this.error("Problem writing file " + dir + file);
                    }
                } else {

                    if (this.controlApplet != null) {
                        PleaseWaitFrame pwf = new PleaseWaitFrame(
                                "Printing ... (may take up to 30 seconds)");
                        pwf.setVisible(true);
                        pwf.toFront();
                        PostscriptPrinter psp = (PostscriptPrinter) this.controlApplet;
//                        psp.printPostscript(eps); TODO
                        pwf.dispose();
                    }

                }
            }

        }

    }

    public void writePSFile() {

        // form the postscript
        List<TopsDrawCanvas> dcs = this.topsDisplay.getDrawCanvases();
        List<String> epSS = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        for (TopsDrawCanvas tdc : dcs) {
        	try {
        	    epSS.add(tdc.getEPS());
        	} catch (IOException ioe) {
        	    // TODO
        	}
        	titles.add(tdc.getLabel());
        }

        List<String> postScript;
        try {
            postScript = PostscriptFactory.PSArrayA4(titles, epSS, 54, 0.5f);
        } catch (PSException pse) {
            this.error(pse.message);
            return;
        }

        // get an output file
        if (!this.appletMode) {
            FileDialog fd = new FileDialog(
            		this.f, "Choose PS filename",FileDialog.SAVE);
            fd.setFont(new Font(TIMES_ROMAN, Font.PLAIN, 18));
            fd.setVisible(true);

            String file = fd.getFile();
            if (file == null)
                return;

            String dir = fd.getDirectory();

            PrintWriter pw;
            try (FileOutputStream fos = new FileOutputStream(dir + file)) {
                pw = new PrintWriter(fos);
                for (String psString : postScript) {
                    pw.println(psString);
                }
            } catch (IOException e) {
                log.warning("An IOException was caught in WritePS");
                this.error("Problem writing file " + dir + file);
            }
        } else {
            if (this.controlApplet != null) {
                PleaseWaitFrame pwf = new PleaseWaitFrame(
                        "Making PDF ... (may take up to 30 seconds)");
                pwf.setVisible(true);
                pwf.toFront();
                PostscriptPrinter psp = (PostscriptPrinter) this.controlApplet;
                psp.printPostscript(postScript);
                pwf.dispose();
            }
        }
    }

    // reads a file specifying equivalences between SSEs in a set of related
    // domains and colours these cartoons according to the equivalence
    public void colourAlign() {
        if (this.proteins == null || this.proteins.isEmpty()) {
            log.warning("No cartoons to orient !!! ");
            return;
        }

        FileDialog fd = 
        	new FileDialog(this.f, "Read SSE alignment file", FileDialog.LOAD);
        fd.setFont(new Font(TIMES_ROMAN, Font.PLAIN, 18));
        fd.setVisible(true);

        String file = fd.getFile();
        if (file == null)
            return;

        try {
        	OrientInfo oi = new OrientInfo(new File(file));
            if (oi.hasMapping()) {
                this.colourAlign(oi);
            }
        } catch (EquivFileFormatException effe) {
            this.error("Format error in file " + file);
        } catch (IOException ioe) {
            this.error("IO error reading file " + file);
        }
    }
    
    public void colourAlign(OrientInfo oi) {
    	// colour equivalences
        int i = 0;
        List<String> orientNames = oi.getNames();
        for (String domname : orientNames) {
            Cartoon root = this.getRootSSE(domname);
            if (root == null) {
                this.error(String.format("Domain %s not found", domname));
                break;
            }
            for (int j = 0; j < oi.numberOfMappings(); j++) {
                SecStrucElement s = root.getSSEByNumber(oi.getMapping(i, j));
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
            log.warning("No cartoons to orient !!! ");
            return;
        }

        FileDialog fd = new FileDialog(
        		this.f, "Read SSE alignment file",FileDialog.LOAD);
        fd.setFont(new Font(TIMES_ROMAN, Font.PLAIN, 18));
        fd.setVisible(true);

        String file = fd.getFile();
        if (file == null)
            return;

        try {
        	OrientInfo oi = new OrientInfo(new File(file));
            if (oi.hasMapping()) {
            	this.orientCartoons(oi);
            }
        } catch (EquivFileFormatException effe) {
            this.error("Format error in file " + file);
        } catch (IOException ioe) {
            this.error("IO error reading file " + file);
        }
    }
    
    public void orientCartoons(OrientInfo oi) {

        // everything is oriented w.r.t. a reference - the first domain in the
        // equivalence file
        List<String> orientNames = oi.getNames();
        String refdomname = orientNames.get(0);
        Cartoon refdomroot = this.getRootSSE(refdomname);
        if (refdomroot == null) {
            this.error("Domain " + refdomname + " not found");
            return;
        }

        // orient all other domains in equivalence file w.r.t. reference
        Cartoon root;
        int i = 0;
        try {
            int[] reference = oi.getMapping(0);
            for (String domname : orientNames) {
                i++;
                root = this.getRootSSE(domname);
                if (root == null) {
                    this.error("Domain " + domname + " not found");
                    break;
                }
                TopsDrawCanvas tdc = this.topsDisplay.getDrawCanvas(root);
                oi.orientConsensus(refdomroot, root, reference, oi.getMapping(i), tdc);
            }
        } finally {
            // do repaints
            this.topsDisplay.repaint();
        }

    }

    private Cartoon getRootSSE(String domName) {
        if (this.proteins == null)
            return null;

        for (Protein p : this.proteins) {
            Cartoon sseRoot = p.getRootSSE(domName);
            if (sseRoot != null) {
            	return sseRoot;
            }
        }

        return null;

    }

    public void print() {

        if (this.proteins == null)
            this.proteins = new ArrayList<>();

        List<String> strs = new ArrayList<>();
        List<Cartoon> diags = new ArrayList<>();

        for(Protein p : this.proteins) {
            List<DomainDefinition> doms = p.getDomainDefs();
            List<Cartoon> lls = p.getLinkedLists();
            for (int i = 0; i < doms.size(); i++) {
                strs.add(doms.get(i).toString());
                diags.add(lls.get(i));
            }
        }

        StringChoice stc = new StringChoice(this.f, "Select domain to print", strs);
        stc.setVisible(true);
        String chosen = stc.getChoice();
        int chosenNum = stc.getChoiceNumber();

        if (chosen != null) {
            TopsDrawCanvas drawCanvToPrint = 
                    this.topsDisplay.getDrawCanvas(diags.get(chosenNum));

            if (drawCanvToPrint != null) {

                if (!this.appletMode) {

                    PrintJob pjob = this.f.getToolkit().getPrintJob(this.f,
                            "Print TOPS diagram", TopsEditor.printprefs);
                    if (pjob == null)
                        return;
                    Graphics page = pjob.getGraphics();

                    Dimension canvSize = drawCanvToPrint.getSize();
                    Dimension pageSize = pjob.getPageDimension();

                    page.translate((pageSize.width - canvSize.width) / 2,
                            (pageSize.height - canvSize.height) / 2);

                    drawCanvToPrint.print(page);

                    page.dispose();
                    pjob.end();
                } else {
                    if (this.controlApplet != null) {
                        PleaseWaitFrame pwf = new PleaseWaitFrame(
                                "Printing ... (may take up to 30 seconds)");
                        pwf.setVisible(true);
                        pwf.toFront();
                        ImagePrinter ip = (ImagePrinter) this.controlApplet;
                        ip.printImage(drawCanvToPrint.getImage());
                        pwf.dispose();
                    }
                }
            }
        }
    }

    public void processDSSPFile() {

        FileDialog fd = new FileDialog(this.f, "Read DSSP file", FileDialog.LOAD);
        fd.setFont(new Font(TIMES_ROMAN, Font.PLAIN, 18));
        fd.setFilenameFilter(new DSSPFileFilter());
        fd.setVisible(true);

        String file = fd.getFile();
        if (file == null)
            return;

        int i;
        StringBuilder sb = new StringBuilder();
        for (i = 0; i < file.length(); i++) {
            if (file.charAt(i) == '.')
                break;
            sb.append(file.charAt(i));
        }

        String pc = sb.toString();
        String tmpfile = this.randomTopsFile();
        String topsCommand = "Topsf " + pc + " -s " + tmpfile;

        Runtime runtime = Runtime.getRuntime();

        PleaseWaitFrame pwf = new PleaseWaitFrame(
                "Please wait while I process the dssp file ... ");
        pwf.setVisible(true);
        log.info("Starting execution: " + topsCommand);
        Process proc;
        try {
            proc = runtime.exec(topsCommand);
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader bre = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = br.readLine();
            while (line != null) {
                line = br.readLine();
            }
            line = bre.readLine();
            while (line != null) {
                line = bre.readLine();
            }
            proc.waitFor();
            log.info("Finished execution");
            pwf.dispose();
        } catch (IOException e) {
            log.warning("exec threw an IOException");
            pwf.dispose();
            return;
        } catch (InterruptedException e) {
            log.warning("waitFor threw an InterrputedException");
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
        return "tmp" + ri + ".tops";
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
             File file = new File(argv[0]);
             if (file.exists()) {
                 if (file.isDirectory()) {
                     String[] sfilelist = file.list(new TopsFileFilter());
                     if (sfilelist != null) {
                         list = new FileList(argv[0], sfilelist);
                         cf = list.getCurrentFile();
                     }
                 } else if (file.isFile()) {
                     cf = file;
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



