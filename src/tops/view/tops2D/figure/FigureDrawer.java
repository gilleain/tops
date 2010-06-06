package tops.view.tops2D.figure;

import java.awt.Graphics;

import java.util.ArrayList;

/**
 * Intended to draw topologies that can be represented as flat sheets
 * (sandwiches, simple barrels) Cannot represent any arbitrary TOPS graph, so
 * does not take one as an argument.
 */

public class FigureDrawer {

    public static final int UP = 0;

    public static final int DOWN = 1;

//    private int width;

//    private int height;

//    private ArrayList sheets;

    private ArrayList currentSheet;

    public FigureDrawer(int width, int height) {
//        this.width = width;
//        this.height = height;
//        this.sheets = new ArrayList();
    }

    public void startNewSheet() {
        this.currentSheet = new ArrayList();
    }

    public void addStrand(int orientation, int sheetOrder) {
        this.currentSheet.add(sheetOrder, new Integer(orientation));
    }

    public void addHelix(int start, int end) {
    }

    public void addTerminus(char label) {
    }

    public void draw(Graphics g) {
    }

}
