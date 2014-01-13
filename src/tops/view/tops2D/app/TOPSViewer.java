package tops.view.tops2D.app;

import java.awt.*;
import java.awt.event.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

import javax.swing.BorderFactory;
//import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
//import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import tops.engine.Result;
import tops.engine.TopsStringFormatException;

import tops.engine.drg.Comparer;
import tops.engine.drg.Matcher;

public class TOPSViewer implements ActionListener, ListSelectionListener {

    private static LinearViewPanel[] panes;

//    private int numberGraphs = 0;

    private JButton showButton, matchButton, compareButton;

    private JTable jTable;

    private DefaultTableModel tableModel;

    private JTextField patternField;

    private Map<String, String> vertexMap, edgeMap, highlightMap, classMap;

    private static int numPanes;

    private static int paneHeight;

    private static int paneWidth;
    
    public TOPSViewer(String fileName, String[] args) {
        JFrame frame = new JFrame("TOPS Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add stuff to the main-frame
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(null);
        Component controls = this.createControls();

        contentPane.add(controls);
        controls.setLocation(0, 0);
        controls.setSize(250, 590);

        int numRows = (args.length > 1) ? Integer.parseInt(args[1]) : 4;
        int numCols = (args.length > 2) ? Integer.parseInt(args[2]) : 1;
        TOPSViewer.paneWidth = (args.length > 3) ? Integer.parseInt(args[3]) : 500;
        TOPSViewer.paneHeight = (args.length > 4) ? Integer.parseInt(args[4]) : 150;

        int paneSpacing = TOPSViewer.paneHeight;
        TOPSViewer.numPanes = numRows * numCols;

        TOPSViewer.panes = new LinearViewPanel[TOPSViewer.numPanes];
        for (int col = 0; col < numCols; col++) {
            int py = 50;
            int rowStart = 260 + (TOPSViewer.paneWidth * col);
            for (int row = 0; row < numRows; ++row, py += paneSpacing) {
                int rowIndex = row + (col * numRows);
                TOPSViewer.panes[rowIndex] = this.createView(String.valueOf(rowIndex));
                TOPSViewer.panes[rowIndex].setLocation(rowStart, py);
                contentPane.add(TOPSViewer.panes[rowIndex]);
            }
        }

        this.loadFile(fileName);

        frame.pack();
        frame.setSize(800, 700);
        frame.setVisible(true);
        
    }

    public Component createControls() {
        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.setLayout(null);

        this.tableModel = new DefaultTableModel();
        TableSorter sorter = new TableSorter(this.tableModel);
        this.jTable = new JTable(sorter);
        this.jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        ListSelectionModel lsm = this.jTable.getSelectionModel();
        lsm.addListSelectionListener(this);
        sorter.addMouseListenerToHeaderInTable(this.jTable);
        pane.add(this.jTable);

        JScrollPane scrollPane = new JScrollPane(this.jTable);
        pane.add(scrollPane);
        scrollPane.setLocation(10, 50);
        scrollPane.setSize(250, 300);

        this.showButton = new JButton("Show");
        pane.add(this.showButton);
        this.showButton.setLocation(10, 350);
        this.showButton.setSize(80, 80);
        this.showButton.setMnemonic('s');
        this.showButton.addActionListener(this);

        this.matchButton = new JButton("Match");
        pane.add(this.matchButton);
        this.matchButton.setLocation(10, 430);
        this.matchButton.setSize(80, 80);
        this.matchButton.setMnemonic('m');
        this.matchButton.addActionListener(this);
        this.matchButton.setEnabled(false); // start this button as disabled

        this.compareButton = new JButton("Compare");
        pane.add(this.compareButton);
        this.compareButton.setLocation(90, 430);
        this.compareButton.setSize(80, 80);
        this.compareButton.setMnemonic('c');
        this.compareButton.addActionListener(this);
        this.compareButton.setEnabled(false); // start this button as disabled

        this.patternField = new JTextField();
        pane.add(this.patternField);
        this.patternField.setLocation(10, 510);
        this.patternField.setSize(150, 20);
        this.patternField.addActionListener(this);

        return pane;
    }

    public LinearViewPanel createView(String s) {
        LinearViewPanel pane = new LinearViewPanel();
        pane.setSize(TOPSViewer.paneWidth, TOPSViewer.paneHeight);
        pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder(s), BorderFactory.createEmptyBorder(10, 10,
                10, 10)));
        return pane;
    }

    public void loadFile(String fileName) {
        this.vertexMap = new HashMap<String, String>();
        this.edgeMap = new HashMap<String, String>();
        this.highlightMap = new HashMap<String, String>();
        this.classMap = new HashMap<String, String>();

        try {
            FileReader inFile;
            ArrayList<String> names = new ArrayList<String>();
            inFile = new FileReader(fileName);
            BufferedReader buffy = new BufferedReader(inFile);
            String line, head, vstr, estr, classification;
            TParser tp = new TParser();

            while ((line = buffy.readLine()) != null) {
                if ((line.charAt(line.length() - 1) != 'C')
                        && (!line.equals("\n"))) {
                    tp.setCurrent(line);
                    head = tp.getName();
                    vstr = tp.getVertexStringSafely();
                    estr = tp.getEdgeString();
                    classification = tp.getClassification();
                    names.add(head);
                    this.vertexMap.put(head, vstr);
                    this.edgeMap.put(head, estr);
                    
                    // what if classification is null?
                    this.classMap.put(head, classification); 
                    
//                    this.numberGraphs++;
                }
            }
            buffy.close();

            Vector<String> nameVector = new Vector<String>();
            nameVector.addAll(names);

            this.tableModel.addColumn("names", nameVector);

            int[] sel = new int[1];
            sel[0] = 0;
            this.showSelected(sel); // show the first in list
        } catch (IOException ioe) {
            System.out.print(ioe);
        }
    }

    @SuppressWarnings("unchecked")
	public String[] getInstances() {
        ArrayList<String> instances = new ArrayList<String>();
        Vector<Vector<?>> dataVector = this.tableModel.getDataVector();
        int nameColumnIndex = 0;

        for (int i = 0; i < dataVector.size(); i++) {
            String name = (String) (dataVector.elementAt(i)).elementAt(nameColumnIndex);
            StringBuffer instance = new StringBuffer(name);
            instance.append(" ");
            instance.append(this.vertexMap.get(name));
            instance.append(" ");
            instance.append(this.edgeMap.get(name));
            instance.append(" ");
            instance.append(this.classMap.get(name));
            instances.add(instance.toString());
        }
        return instances.toArray(new String[0]);
    }

    public void showSelected(int[] indices) {
        int nameColumnIndex = 0;

        for (int i = 0; i < TOPSViewer.numPanes && i < indices.length; ++i) {
            String name = (String) (this.tableModel.getValueAt(indices[i],
                    nameColumnIndex));
            // System.err.println("from table got name " + name);
            TOPSViewer.panes[i].setBorder(BorderFactory.createCompoundBorder(BorderFactory
                    .createTitledBorder(name), BorderFactory.createEmptyBorder(
                    10, 10, 10, 10)));
            String vStr = this.vertexMap.get(name);
            String eStr = this.edgeMap.get(name);
            String hStr = this.highlightMap.get(name);
            TOPSViewer.panes[i].renderGraph(vStr, eStr, hStr);
        }
    }

    public void compare() {
        String pattern = this.patternField.getText();
        System.out.println("Comparing : " + pattern);
        String[] instances = this.getInstances();
        Comparer e = new Comparer();
        Result[] results = null;
        try {
            results = e.compare(pattern, instances);
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe);
        }

        this.tableModel.addColumn("compression");
        this.tableModel.addColumn("classification");

        for (int i = 0; i < results.length; i++) {
            Result r = results[i];
            this.tableModel.setValueAt(r.getID(), i, 0);
            this.tableModel.setValueAt(new Float(r.getCompression()), i, 1);
            this.tableModel.setValueAt(r.getClassification(), i, 2);
        }
    }

    public void match() {
        String pattern = this.patternField.getText();
        System.out.println("Matching : " + pattern);
        Matcher m = null;
        try {
            m = new Matcher(this.getInstances());
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe);
        }

        String[] results = m.run(pattern);

        this.highlightMap.clear();
        for (int i = 0; i < results.length; i++) {
            String[] bits = results[i].split(" ");
            this.highlightMap.put(bits[0], bits[3]);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.showButton) {
            // if (jList.isSelectionEmpty()) { System.out.println("You fool!");
            // }
            // else showSelected();
            // showSelected();
        } else if (e.getSource() == this.matchButton) {
            this.match();
        } else if (e.getSource() == this.compareButton) {
            this.compare();
        } else if (e.getSource() == this.patternField) {
            this.matchButton.setEnabled(true);
            this.compareButton.setEnabled(true);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (!lsm.isSelectionEmpty()) {
            int first = lsm.getMinSelectionIndex();
            int last = lsm.getMaxSelectionIndex();
            ArrayList<Integer> selection = new ArrayList<Integer>();
            for (int i = first; i <= last; i++) {
                if (lsm.isSelectedIndex(i)) {
                    // System.err.println("selected : " + i);
                    selection.add(new Integer(i));
                }
            }
            int[] sel = new int[selection.size()];
            for (int j = 0; j < selection.size(); j++) {
                sel[j] = selection.get(j).intValue();
            }
            this.showSelected(sel);
        }
    }

    public static void main(String[] args) throws IOException {
        
        try {
            new TOPSViewer(args[0], args);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            System.out.println(aioobe + " No file specified : using default!");
            new TOPSViewer("test.str", args);
        }
        
        
    }

}
