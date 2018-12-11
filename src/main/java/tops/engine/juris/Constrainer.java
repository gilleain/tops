package tops.engine.juris;

import java.util.ArrayList;
import java.util.Iterator;

public class Constrainer {

    private String[] vstrs; 
    private String[] estrs;

    private int pop;

    private int maxAOut;

    private int maxPOut;

    private int maxLOut;

    private int maxROut;

    private int maxZOut;

    private int maxXOut;

    private int maxAIn;

    private int maxPIn;

    private int maxLIn;

    private int maxRIn;

    private int maxZIn;

    private int maxXIn;

    private int maxELower;

    private int maxEUpper;

    private int maxHLower;

    private int maxHUpper;

    private int maxVert;

    private int maxA;

    private int maxP;

    private int maxZ;

    private int maxX;

    private int maxR;

    private int maxL;

    private int maxEdge;

    private int maxThings;

    private int totalVert;

    private int totalEdge;

    private int totalThings;

    public Constrainer(String[] victims) {
        this.pop = victims.length;
        this.vstrs = new String[this.pop];
        this.estrs = new String[this.pop];
        TParser tp = new TParser(victims);
        for (int i = 0; i < this.pop; ++i) { // chop off N/C
            this.vstrs[i] = tp.getVertexString();
            this.estrs[i] = tp.getEdgeString();
            tp.next();
        }

        this.setMaxAOut(this.findMaxOutdex('A'));
        this.maxPOut = this.findMaxOutdex('P');
        this.setMaxLOut(this.findMaxOutdex('L'));
        this.setMaxROut(this.findMaxOutdex('R'));
        this.setMaxZOut(this.findMaxOutdex('Z'));
        this.setMaxXOut(this.findMaxOutdex('X'));

        this.setMaxAIn(this.findMaxIndex('A'));
        this.setMaxPIn(this.findMaxIndex('P'));
        this.setMaxLIn(this.findMaxIndex('L'));
        this.setMaxRIn(this.findMaxIndex('R'));
        this.setMaxZIn(this.findMaxIndex('Z'));
        this.setMaxXIn(this.findMaxIndex('X'));

        this.setMaxELower(this.findMin('e', this.vstrs));
        this.setMaxEUpper(this.findMin('E', this.vstrs));
        this.setMaxHLower(this.findMin('h', this.vstrs));
        this.setMaxHUpper(this.findMin('H', this.vstrs));
        
     // !!!if e = E and h = H, then it doesn't matter which we sum!
        this.setMaxVert(this.getMaxELower() + this.getMaxHUpper()); 

        this.setMaxA(this.findMin('A', this.estrs));
        this.setMaxP(this.findMin('P', this.estrs));
        this.setMaxZ(this.findMin('Z', this.estrs));
        this.setMaxX(this.findMin('X', this.estrs));
        this.setMaxR(this.findMin('R', this.estrs));
        this.setMaxL(this.findMin('L', this.estrs));
        this.setMaxEdge(this.getMaxA() + this.getMaxP() + this.getMaxZ() + this.getMaxX() + this.getMaxR() + this.getMaxL());

        this.setMaxThings(this.getMaxVert() + this.getMaxEdge());

        this.setTotalVert(this.sums(this.vstrs));
        this.setTotalEdge(this.sums(this.estrs));
        this.setTotalThings(this.getTotalVert() + this.getTotalEdge());
    }

    private int sums(String[] e) {
        int sum = 0;
        for (int i = 0; i < this.pop; ++i) {
            for (int j = 0; j < e[i].length(); ++j) {
                if (Character.isLetter(e[i].charAt(j)))
                    sum++;
            }
        }
        return sum;
    }

    private int findMin(char n, String[] vORe) {
        int min = 0; // store best result
        // N is the /opposite/ of n, in terms of case
        char nChar = (Character.isUpperCase(n)) ? Character.toLowerCase(n)
                : Character.toUpperCase(n);
        char ch;
        for (int i = 0; i < this.pop; ++i) {
            int c = 0;
            for (int j = 0; j < vORe[i].length(); ++j) {
                ch = vORe[i].charAt(j);
                if ((ch == n) || (ch == nChar))
                    c++;
            }
            if (c == 0)
                return 0;
            if (c <= min)
                min = c;
        }
        return min;
    }

    private int findMaxOutdex(char type) {
        ArrayList<String> fragments = new ArrayList<>();
        int max = 0;
        for (int i = 0; i < this.estrs.length; ++i) {
            int last = 0;
            for (int j = 0; j < this.estrs[i].length(); ++j) {
                char fr = this.estrs[i].charAt(j);
                if (Character.isLetter(fr)) {
                    // only add relevant edges
                    if (fr == type) {
                        fragments.add(this.estrs[i].substring(last, j + 1));
                    }
                    last = j + 1;
                }
            }

            if (fragments.isEmpty()) { // no point counting the empty edges
                continue;
            }
            
            // currmax stores the maximum degree for this graph
            int currmax = 0; 

            String temp = fragments.get(0); // look at first
            String node = temp.substring(0, temp.indexOf(':'));

            int c = 0;
            for (String edge : fragments) {
                String current = edge.substring(0, edge.indexOf(':'));
                if (current.equals(node)) {
                    c++;
                    if (c > currmax)
                        currmax = c;
                } else {
                    node = current;
                    c = 1;
                } // switch to a new node
            }
            if (currmax > max)
                max = currmax; // has this graph been the winner so far?
            fragments.clear(); // reset for next edgetype

        }
        return max;
    }

    private int findMaxIndex(char type) {
        ArrayList<String> fragments = new ArrayList<>();
        int max = 0;
        
        for (int i = 0; i < this.estrs.length; ++i) {
            int last = 0;
            for (int j = 0; j < this.estrs[i].length(); ++j) {
                char fr = this.estrs[i].charAt(j);
                if (Character.isLetter(fr)) {
                    // only add relevant edges
                    if (fr == type) {
                        fragments.add(this.estrs[i].substring(last, j + 1));
                    }
                    last = j + 1;
                }
            }

            if (fragments.isEmpty()) { // no point counting the empty edges
                continue;
            } 
            
            int currmax = 0;
            Iterator<String> itr = fragments.iterator();
            while (itr.hasNext()) { 
                String t = itr.next();
                String node = t.substring(t.indexOf(':'), t.length() - 1);
                itr.remove();
                int c = 1;
                while (itr.hasNext()) {
                    t = itr.next();
                    String current = t.substring(t.indexOf(':'), t.length() - 1);
                    if (node.equals(current)) {
                        itr.remove();
                        c++;
                    }
                }
                if (c > currmax)
                    currmax = c;
                itr = fragments.iterator(); // reset??
            }

            if (currmax > max)
                max = currmax; // has this graph been the winner so far?
            fragments.clear(); // reset for next edgetype
        }
        return max;
    }

    public String toString() {
        StringBuilder outstring = new StringBuilder("");
        outstring.append("max_e= ");
        outstring.append(this.getMaxELower());
        outstring.append("\nmax_E= ");
        outstring.append(this.getMaxEUpper());
        outstring.append("\nmax_h= ");
        outstring.append(this.getMaxHLower());
        outstring.append("\nmax_H= ");
        outstring.append(this.getMaxHUpper());
        outstring.append("\nmax_vert= ");
        outstring.append(this.getMaxVert());
        outstring.append("\nmax_A_out= ");
        outstring.append(this.maxAOut);
        outstring.append("\nmax_P_out= ");
        outstring.append(this.maxPOut);
        outstring.append("\nmax_L_out= ");
        outstring.append(this.getMaxLOut());
        outstring.append("\nmax_R_out= ");
        outstring.append(this.getMaxROut());
        outstring.append("\nmax_Z_out= ");
        outstring.append(this.getMaxZOut());
        outstring.append("\nmax_X_out= ");
        outstring.append(this.getMaxXOut());
        outstring.append("\nmax_A_in= ");
        outstring.append(this.getMaxAIn());
        outstring.append("\nmax_P_in= ");
        outstring.append(this.getMaxPIn());
        outstring.append("\nmax_L_in= ");
        outstring.append(this.getMaxLIn());
        outstring.append("\nmax_R_in= ");
        outstring.append(this.getMaxRIn());
        outstring.append("\nmax_Z_in= ");
        outstring.append(this.getMaxZIn());
        outstring.append("\nmax_X_in= ");
        outstring.append(this.getMaxXIn());
        outstring.append("\nmax_A= ");
        outstring.append(this.getMaxA());
        outstring.append("\nmax_P= ");
        outstring.append(this.getMaxP());
        outstring.append("\nmax_Z= ");
        outstring.append(this.getMaxZ());
        outstring.append("\nmax_X= ");
        outstring.append(this.getMaxX());
        outstring.append("\nmax_R= ");
        outstring.append(this.getMaxR());
        outstring.append("\nmax_L= ");
        outstring.append(this.getMaxL());
        outstring.append("\nmax_edge= ");
        outstring.append(this.getMaxEdge());
        outstring.append("\ntotal_vert= ");
        outstring.append(this.getTotalVert());
        outstring.append("\ntotal_edge= ");
        outstring.append(this.getTotalEdge());
        return outstring.toString();
    }

    public int getMaxAOut() {
        return maxAOut;
    }

    public void setMaxAOut(int maxAOut) {
        this.maxAOut = maxAOut;
    }

    public int getMaxPOut() {
        return maxPOut;
    }

    public void setMaxPOut(int maxPOut) {
        this.maxPOut = maxPOut;
    }

    public int getMaxLOut() {
        return maxLOut;
    }

    public void setMaxLOut(int maxLOut) {
        this.maxLOut = maxLOut;
    }

    public int getMaxROut() {
        return maxROut;
    }

    public void setMaxROut(int maxROut) {
        this.maxROut = maxROut;
    }

    public int getMaxZOut() {
        return maxZOut;
    }

    public void setMaxZOut(int maxZOut) {
        this.maxZOut = maxZOut;
    }

    public int getMaxXOut() {
        return maxXOut;
    }

    public void setMaxXOut(int maxXOut) {
        this.maxXOut = maxXOut;
    }

    public int getMaxAIn() {
        return maxAIn;
    }

    public void setMaxAIn(int maxAIn) {
        this.maxAIn = maxAIn;
    }

    public int getMaxPIn() {
        return maxPIn;
    }

    public void setMaxPIn(int maxPIn) {
        this.maxPIn = maxPIn;
    }

    public int getMaxLIn() {
        return maxLIn;
    }

    public void setMaxLIn(int maxLIn) {
        this.maxLIn = maxLIn;
    }

    public int getMaxRIn() {
        return maxRIn;
    }

    public void setMaxRIn(int maxRIn) {
        this.maxRIn = maxRIn;
    }

    public int getMaxZIn() {
        return maxZIn;
    }

    public void setMaxZIn(int maxZIn) {
        this.maxZIn = maxZIn;
    }

    public int getMaxXIn() {
        return maxXIn;
    }

    public void setMaxXIn(int maxXIn) {
        this.maxXIn = maxXIn;
    }

    public int getMaxELower() {
        return maxELower;
    }

    public void setMaxELower(int maxELower) {
        this.maxELower = maxELower;
    }

    public int getMaxEUpper() {
        return maxEUpper;
    }

    public void setMaxEUpper(int maxEUpper) {
        this.maxEUpper = maxEUpper;
    }

    public int getMaxHLower() {
        return maxHLower;
    }

    public void setMaxHLower(int maxHLower) {
        this.maxHLower = maxHLower;
    }

    public int getMaxHUpper() {
        return maxHUpper;
    }

    public void setMaxHUpper(int maxHUpper) {
        this.maxHUpper = maxHUpper;
    }

    public int getMaxVert() {
        return maxVert;
    }

    public void setMaxVert(int maxVert) {
        this.maxVert = maxVert;
    }

    public int getMaxA() {
        return maxA;
    }

    public void setMaxA(int maxA) {
        this.maxA = maxA;
    }

    public int getMaxP() {
        return maxP;
    }

    public void setMaxP(int maxP) {
        this.maxP = maxP;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxR() {
        return maxR;
    }

    public void setMaxR(int maxR) {
        this.maxR = maxR;
    }

    public int getMaxL() {
        return maxL;
    }

    public void setMaxL(int maxL) {
        this.maxL = maxL;
    }

    public int getMaxEdge() {
        return maxEdge;
    }

    public void setMaxEdge(int maxEdge) {
        this.maxEdge = maxEdge;
    }

    public int getMaxThings() {
        return maxThings;
    }

    public void setMaxThings(int maxThings) {
        this.maxThings = maxThings;
    }

    public int getTotalVert() {
        return totalVert;
    }

    public void setTotalVert(int totalVert) {
        this.totalVert = totalVert;
    }

    public int getTotalEdge() {
        return totalEdge;
    }

    public void setTotalEdge(int totalEdge) {
        this.totalEdge = totalEdge;
    }

    public int getTotalThings() {
        return totalThings;
    }

    public void setTotalThings(int totalThings) {
        this.totalThings = totalThings;
    }
}
