package tops.drawing.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.EmptyStackException;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import tops.drawing.Cartoon;
import tops.drawing.symbols.CartoonConnector;

public class TopsEditor extends JApplet {
    
    // Activities
    public enum State {
        SELECT,
        UNDO,
        DELETE,
        CLEAR,
        ZOOM_IN,
        ZOOM_OUT,
        SUBMIT,
        
        // Symbols
        STRAND_UP,
        STRAND_DOWN,
        HELIX_UP,
        HELIX_DOWN,
        TEMPLATE,
        
        // Arcs
        H_BOND,
        RIGHT_ARC,
        LEFT_ARC,
        RANGE,
        
        // Flips and Align
        FLIP,
        HORIZONTAL_ALIGN,
        VERTICAL_ALIGN,
        FLIP_X,
        FLIP_Y
    }
    
    private String currentFilename; // the name of where the file is currently stored
    private String currentDir;
    private boolean isSaved;
    
    private TopsCanvas canvas;
    private Menubar menuBar;

    private EditingToolbar editingToolbar;
    
    private TemplateDialog templateDialog;
    private SubmissionDialog submitDialog;
    private InsertEditRangeDialog addEditDialog;
    
    private State currentState;

    public TopsEditor() {
        
        this.menuBar = new Menubar(this);
        this.canvas = new TopsCanvas(this);
        
        JScrollPane canvasScrollPane = new JScrollPane(canvas);
        canvasScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        canvasScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        canvasScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        this.editingToolbar = new EditingToolbar(this);
        
        this.setLayout(new BorderLayout());
        this.add(this.editingToolbar, BorderLayout.NORTH);
        this.add(canvasScrollPane, BorderLayout.CENTER);
        this.setVisible(true);
        
        try {
        	templateDialog = new TemplateDialog(this);
        } catch (NullPointerException npe) {
        	System.err.println("npe when making template dialog");
        }
        submitDialog = new SubmissionDialog(this);
        addEditDialog = new InsertEditRangeDialog(this);
        
        // load/save stuff - may not be necessary...
        isSaved = false;            // the save status for the cartoon
        currentFilename = null;
        currentDir = null;
    }
    
    @Override
    public void init() {
        this.setSize(500, 500);
    }
   
    public void setTemplateDialogVisible() {
        this.templateDialog.setVisible(true);
    }
    
    public void toggleTemplateDialogVisible() {
        this.templateDialog.setVisible(this.templateDialog.isVisible());
    }
    
    public void setSubmitDialogVisible(boolean isVisible) {
        this.submitDialog.setVisible(isVisible);
    }
    
    public void deselectAll() {
        this.canvas.deselectAll();
    }
    
    public void zoomIn() {
        this.canvas.zoomIn();
    }
    
    public void zoomOut() {
        this.canvas.zoomOut();
    }
    
    public String[] getSubmissionData() {
        return this.submitDialog.getSubmissionData();
    }
    
    public void print() {
        // TODO
    }
    
    // This is the dialog that either asks the user to
    // 'Delete' , 'Edit' or 'Add' a range.
    public void showOptionDialog(CartoonConnector con) {
        addEditDialog.setConnection(con);
        addEditDialog.setVisible(true);
        this.canvas.repaint();
    }

    public void loadAddEditDialog(int to, int from) {
        this.addEditDialog.reset(to, from);
    }
    
    public String getTopsString() {
        return this.canvas.getCartoon().toString();
    }
    
    public Cartoon getCartoon() {
        return this.canvas.getCartoon();
    }

    public State getState() {
        return this.currentState;
    }
    
    public void setState(State state) {
        currentState = state;
        this.canvas.setState(state);
    }
    
    public void fireHorizontalAlign() {
        int numberOfSelected = this.canvas.numberOfSelectedSSESymbols();
        if (numberOfSelected < 1) {
            System.err.println("You must select two or more figures to align!");
        } else {
            this.setState(State.HORIZONTAL_ALIGN);
        }
    }
    
    public void fireVerticalAlign() {
        int numberOfSelected = this.canvas.numberOfSelectedSSESymbols();
        if (numberOfSelected < 1) {
            System.err.println("You must select two or more figures to align!");
        } else {
            this.setState(State.VERTICAL_ALIGN);
        }
    }
    
    public void selectAll() {
        this.canvas.selectAll();
    }
    
    public void center() {
        this.canvas.center();
    }
    
    public void fireDeleteAll() {
        canvas.clearCanvas();
        canvas.fitToScreen();
    }

    public void fireInsertRangesEvent() {
    }

    public void installInFrame(JFrame host) {
        host.getContentPane().add(this);
        host.setJMenuBar(this.menuBar);
    }
    
    public void exportImage(String imageType) {
        String baseDir = System.getProperty("home");
        System.err.println("home.dir " + baseDir);
        String extension;
        if (imageType.equals("jpeg")) {
            extension = ".jpg";
        } else if (imageType.equals("png")) {
            extension = ".png";
        } else {
            extension = ".none";    // XXX ; not sure - throw exception?
        }
        
        JFileChooser fc = new JFileChooser(baseDir);

        int returnVal = fc.showSaveDialog(null);

        String fileLocation = null;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String currentFilename = fc.getSelectedFile().getName();
            String currentDir = fc.getCurrentDirectory().getAbsolutePath();
            
            if (currentFilename.endsWith(extension)) {
                fileLocation = currentDir + "/" + currentFilename;
            } else {
                fileLocation = currentDir + "/" + currentFilename + extension;
            }
        }
        
        if (fileLocation == null) { // hmmm...
            return;
        }

        BufferedImage image = this.canvas.getImage(imageType);
        try {
            ImageIO.write(image, imageType, new File(fileLocation));
        } catch (IOException ioe) {
            
        }
    }
    
    public void revert() {
        try {
            this.canvas.revert();
        } catch (EmptyStackException e) {
            System.err.println("empty undo stack");
        }
        this.setAsUnSaved();
    }
    
    public void loadHelp() {
        // TODO
    }

    // creates the File Filter so that the .tops files are only visible
    public class MyFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;

            String extension = this.getExtension(f);
            return (extension.equals("tops")) || (extension.equals("TOPS"));
        }

        public String getDescription() {
            return ".tops files";
        }

        private String getExtension(File f) {
            String s = f.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1)
                return s.substring(i + 1).toLowerCase();
            return "";
        }
    } 

    // performs the Saving of the TOPS Diagrams
    public boolean save() {
        try {
            File filename = new File(currentDir, currentFilename);
            PrintWriter pw = new PrintWriter(new FileWriter(filename));

            Cartoon cartoon = this.canvas.getCartoon();
            cartoon.writeToStream(pw);
            
            pw.close();

            return true;
        } catch (Exception e) {
            System.out.println("Error in performSaving() \n");
            e.printStackTrace();
            return false;
        }
    }

    private void saveErrorPopup() {
        Object[] options = { "OK" };

        JOptionPane.showOptionDialog(
            null,
            "Save failed! \nCheck you have write permissions on the target directory/file.",
            "Save Failed!",
            JOptionPane.YES_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]);
    }

    //  saving controller
    public boolean saveCartoon() {
        if (currentFilename != null) {

            if (save())
                return true;
            else {
                saveErrorPopup();
                return false;
            }
        } else {
            JFileChooser fc = new JFileChooser(currentDir);
            fc.setFileFilter(new MyFilter());

            int returnVal = fc.showSaveDialog(null); // getting the filename to save to

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                currentFilename = fc.getSelectedFile().getName();
                currentDir = fc.getCurrentDirectory().getAbsolutePath();

                //check if filename has .tops suffix
                int dot = currentFilename.lastIndexOf('.');

                if (dot == -1)
                    currentFilename = currentFilename + ".tops";

                else if (!(currentFilename.substring(dot)).equals(".tops"))
                    currentFilename =
                        currentFilename.substring(0, dot) + ".tops";

                if (save())
                    return true;
                else {
                    saveErrorPopup();
                    return false;
                }
            } else // user chooses to cancel
                return false;
        }
    }

    // user selects a new cartoon.
    // Prompt to save and then close.
    public void newCartoon() {
        if (!isSaved) {
            Object[] options =
                { "Save", "Save As", "Don't Save", "Cancel" };
            int n =
                JOptionPane.showOptionDialog(
                    null,
                    "You have not saved this TOPS cartoon. Save it now?",
                    "Save TOPS cartoon?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);

            if (n == 0) { /* if user clicks 'Save' */
                saveCartoon();
                isSaved = false;
                currentFilename = null;
            } else if (n == 1) {  /* if user clicks 'save as' */
                if (saveAsCartoon()) {
                    isSaved = false;
                    currentFilename = null;
                }
            } else if (n == 2) { /* if user clicks 'don't save'*/
                isSaved = false;
                currentFilename = null;
            }
        } // if NOT saved
        else {
            isSaved = false;
            currentFilename = null;
        }
        this.canvas.clearCanvas();
    } 

    public boolean saveAsCartoon() {
        JFileChooser fc = new JFileChooser(currentDir);

        fc.setFileFilter(new MyFilter());
        int returnVal = fc.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFilename = fc.getSelectedFile().getName();
            currentDir = fc.getCurrentDirectory().getAbsolutePath();

            //check if filename has .tops suffix
            int dot = currentFilename.lastIndexOf('.');

            if (dot == -1)
                currentFilename = currentFilename + ".tops";

            else if (!(currentFilename.substring(dot)).equals(".tops"))
                currentFilename = currentFilename.substring(0, dot) + ".tops";

            if (save()) {
                return true;
            } else {
                saveErrorPopup();
                return false;
            }
        } else
            return false;
    }

    public void closeCartoon() {
        // dont have a close option - gmt : well you should.
    }

    public Cartoon loadFromBufferedReader(BufferedReader fileStream) throws IOException {
        /*
        ArrayList figures = new ArrayList();
        
        ArrayList lArcs = new ArrayList();
        ArrayList rArcs = new ArrayList();
        ArrayList aBonds = new ArrayList();
        ArrayList pBonds = new ArrayList();
        
        String line;
        int arc_counter = 0;
        while ((line = fileStream.readLine()) != null) {
            String[] bits = line.split("\\s+");
            if (bits.length == 4) {
                String type = bits[0];
                int symbolNum = Integer.parseInt(bits[1]);
                String xStr = bits[2];
                String yStr = bits[3];
                int x = Integer.parseInt(xStr.substring(1, xStr.length() - 1));
                int y = Integer.parseInt(yStr.substring(0, yStr.length() - 1));
                
                if (type.equals("E")) {
                    figures.add(new Strand(symbolNum, x, y, true));
                } else if (type.equals("e")) {
                    figures.add(new Strand(symbolNum, x, y, false));
                } else if (type.equals("H")) {
                    figures.add(new Helix(symbolNum, x, y, true));
                } else if (type.equals("h")) {
                    figures.add(new Helix(symbolNum, x, y, false));
                }
            } else {
                String range = bits[0];
                int colon_index = range.indexOf(":");
                int start = Integer.parseInt(range.substring(0, colon_index)) - 1;
                int end = Integer.parseInt(range.substring(colon_index + 1)) - 1;
                String typeStr = bits[1];
                
                Figure startFig = (Figure) figures.get(start);
                Figure endFig = (Figure) figures.get(end);
                
                if (typeStr.equals("A")) {
                    aBonds.add(new ABond(arc_counter, startFig, endFig));
                } else if (typeStr.equals("P")) {
                    pBonds.add(new PBond(arc_counter, startFig, endFig));
                } else if (typeStr.equals("R")) {
                    rArcs.add(new RChirality(arc_counter, startFig, endFig));
                } else if (typeStr.equals("L")) {
                    lArcs.add(new LChirality(arc_counter, startFig, endFig));
                }
            }
        
        }
        
        ArrayList connections = new ArrayList();
        for (int i = 0; i < figures.size() - 1; i++) {
            connections.add(new Connection(i + 1, (Figure) figures.get(i), (Figure) figures.get(i + 1), true));
        }
        
        return new Cartoon(figures, connections, lArcs, rArcs, aBonds, pBonds);
        */
        return null;
    }

    public boolean load(BufferedReader fileIn) {
        try {
            Cartoon cartoon = this.loadFromBufferedReader(fileIn);

            // the canvas will be resized to deal with the new figures
            canvas.resizeCanvas();
            canvas.loadNewComponents(cartoon);
            canvas.repaint();

            return true;
        } catch (Exception e) {
            System.out.println("Exception in performLoading()");
            e.printStackTrace();
        }
        return false;
    }

    public boolean openCartoon() {

        JFileChooser fc = new JFileChooser(currentDir);
        fc.setFileFilter(new MyFilter());
        int returnVal = fc.showOpenDialog(null);

        BufferedReader inf = null;

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFilename = fc.getSelectedFile().getName();
            currentDir = fc.getCurrentDirectory().getAbsolutePath();
            File newFile = new File(currentDir, currentFilename);

            try {
                inf = new BufferedReader(new FileReader(newFile));
            } catch (Exception e) {
                System.out.println("Exception in openCartoon() \n");
                e.printStackTrace();
            }

            // perform loading
            if (load(inf)) {
                return true;
            } else {
                //pop up a dialog
                Object[] options = { "OK" };

                JOptionPane.showOptionDialog(
                    null,
                    "Load Failed! \n Possible file corruption.",
                    "Load Failed",
                    JOptionPane.YES_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]);

                return false;
            }

        } else
            return false;
    }

    // loads the filename  file
    public boolean openCartoon(String filename) {
        BufferedReader inf = null;
        java.net.URL fileURL = this.getClass().getResource(filename);

        try {
            inf = new BufferedReader(new InputStreamReader(fileURL.openStream()));
        } catch (Exception e) {
            System.out.println("Exception in openCartoon() \n");
            e.printStackTrace();
            return false;
        }

        // perform loading
        if (load(inf))
            return true;
        else
            return false;
    }

    public void fireSaveAsCartoonEvent() {
        if (saveAsCartoon()) {
            isSaved = true;
        }
    }

    public void fireNewCartoonEvent() {
        newCartoon();
    }

    public void fireSaveCartoonEvent() {
        if (saveCartoon()) {
            isSaved = true;
        }
    }

    // this method should be called any time a change to the drawing is made
    public void setAsUnSaved() {
        isSaved = false;
    }

    public void fireExitEditorEvent() {

        /* if not saved, pop up dialog: "You have not saved! Save tournament? */
        if (!isSaved) {

            Object[] options = { "Save", "Save As", "Don't Save", "Cancel" };
            int n =
                JOptionPane.showOptionDialog(
                    null,
                    "You have not saved this TOPS cartoon. Save it now?",
                    "Save TOPS cartoon?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);

            if (n == 0) {
                /* if user clicks 'Save' */
                if (saveCartoon()) {
                    n = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit TOPS Editor?", "Exit?", JOptionPane.YES_NO_OPTION);
                    if (n == JOptionPane.YES_OPTION)
                        System.exit(0);
                }
            } else if (n == 1) {
                /* if user clicks save as */
                if (saveAsCartoon()) {
                    n = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit TOPS Editor?", "Exit?", JOptionPane.YES_NO_OPTION);
                    if (n == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                }
            } else if (n == 2) {
                /*user clicks 'don't save'*/
                n = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit TOPS Editor?", "Exit?", JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        } else {
            int n = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit TOPS Editor?", "Exit?", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }


    // when the user selects to load a given template from the template list
    public void fireLoadTemplateEvent(String filename) {

        if (!isSaved) {
            Object[] options =
                { "Save", "Save As", "Don't Save", "Cancel" };
            int n =
                JOptionPane.showOptionDialog(
                    null,
                    "Do you wish to save the current TOPS cartoon",
                    "Save TOPS cartoon?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            if (n == 0) {
                /* if user clicks 'Save' */

                if (saveCartoon()) {
                    closeCartoon();
                    if (openCartoon(filename)) {
                        isSaved = true;
                    } else {
                        isSaved = true;
                    }
                }
            } else if (n == 1) {
                /* if user clicks 'save as' */

                if (saveAsCartoon()) {
                    closeCartoon();

                    if (openCartoon(filename)) {
                        isSaved = true;
                    } else {
                        isSaved = true; //to prevent a 'Save?' dialog
                    }
                }
            } else if (n == 2 )   {       /* if user clicks 'don't save'*/
                closeCartoon();
                if (openCartoon(filename)) {
                    isSaved = true;
                } else {
                    isSaved = true; //to prevent a 'Save?' dialog
                }
            }
        } else {
            closeCartoon();
            if (openCartoon(filename)) {
                isSaved = true;
            } else {
                isSaved = true; //to prevent a 'Save?' dialog
            }
        }
    }

    public void fireOpenCartoonEvent() {
        if (!isSaved) {
            Object[] options =
                { "Save", "Save As", "Don't Save", "Cancel" };
            int n =
                JOptionPane.showOptionDialog(
                    null,
                    "Do you wish to save the current TOPS cartoon",
                    "Save TOPS cartoon?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            if (n == 0) {
                /* if user clicks 'Save' */

                if (saveCartoon()) {
                    closeCartoon();
                    if (openCartoon()) {
                        isSaved = true;
                    } else {
                        isSaved = true;
                    }
                }
            } else if (n == 1) {
                /* if user clicks 'save as' */

                if (saveAsCartoon()) {
                    closeCartoon();
                    if (openCartoon()) {
                        isSaved = true;
                    } else {
                        isSaved = true;
                    }
                }
            } else if (n == 2 )    {      /* if user clicks 'don't save'*/
                closeCartoon();
                if (openCartoon()) {
                    isSaved = true;
                }
            }
        } else {
            closeCartoon();
            if (openCartoon()) {
                isSaved = true;
            }
        }
    }
}

