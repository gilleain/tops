package tops.drawing.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class Menubar extends JMenuBar implements ActionListener{

    // the different Menus in this menu bar
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu helpMenu;

    // items for the file menu
    private JMenuItem newItem;
    private JMenuItem openItem;
    private  JMenuItem saveItem;    
    private  JMenuItem saveAsItem; 
    private JMenuItem exitItem;

    // export to jpg, bmp file
    private JMenu export;
    private JMenuItem jpegItem;
    private JMenuItem pngItem;
    
    // items for the edit menu
    private JMenuItem undoItem;
    private JMenuItem selectAllItem;
    private JMenuItem centerItem;
    
    // for the help menu
    private JMenuItem helpItem;

    private TopsEditor parentPanel;

    public Menubar(TopsEditor parentPanel) {
        this.parentPanel = parentPanel;
        
        fileMenu = new JMenu("File");
        editMenu = new JMenu("Edit");
        helpMenu = new JMenu("Help");
        
        newItem = new JMenuItem("New");
        newItem.addActionListener(this);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        fileMenu.add(newItem);
        
        openItem = new JMenuItem("Open");
        openItem.addActionListener(this);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        fileMenu.add(openItem);
        
        saveItem = new JMenuItem("Save");
        saveItem.addActionListener(this);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        fileMenu.add(saveItem);
        
        saveAsItem = new JMenuItem("Save as");
        saveAsItem.addActionListener(this);
        fileMenu.add(saveAsItem);
        
        export = new JMenu("Export");
        
        jpegItem = new JMenuItem("To .jpeg File");
        jpegItem.addActionListener(this);
        export.add(jpegItem);
        
        pngItem = new JMenuItem("To .png file");
        pngItem.addActionListener(this);
        export.add(pngItem);
        
        fileMenu.add(export);
        
        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(this);
        KeyStroke exit_key = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK);
        exitItem.setAccelerator(exit_key);
        fileMenu.add(exitItem);
        
        undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(this);
        KeyStroke undo_key = KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK);
        undoItem.setAccelerator(undo_key);
        editMenu.add(undoItem);
        
        selectAllItem = new JMenuItem("Select all");
        selectAllItem.addActionListener(this);
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        editMenu.add(selectAllItem);
        
        centerItem = new JMenuItem("Center");
        centerItem.addActionListener(this);
        centerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        editMenu.add(centerItem);
        
        helpItem = new JMenuItem("Help");
        helpItem.addActionListener(this);
        helpMenu.add(helpItem);
        
        add(fileMenu);
        add(editMenu);
        add(helpMenu);
    }
    
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source == newItem) {
            parentPanel.fireNewCartoonEvent();
        } else if (source == openItem) {
            parentPanel.fireOpenCartoonEvent();
        } else if (source == saveItem) {
            parentPanel.fireSaveCartoonEvent();
        } else if (source ==exitItem) {
            parentPanel.fireExitEditorEvent();
        } else if (source == saveAsItem) {
            parentPanel.fireSaveAsCartoonEvent();
        } else if (source == jpegItem) {
            parentPanel.exportImage("jpeg");
        } else if (source == pngItem) {
            parentPanel.exportImage("png");
        } else if (source == undoItem) {
            parentPanel.revert();
        } else if (source == selectAllItem) {
            parentPanel.selectAll();
        } else if (source == centerItem) {
            parentPanel.center();
        } else if (source == helpItem) {
            parentPanel.loadHelp();
        }
        
    }
}
