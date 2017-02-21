package tops.view.app;

//VERSION WITH INSERTS NE[HH]EC etc

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TParser {

    private String current;

    private static List<String> strings;

    private Iterator<String> stringsIterator;

    // Pattern edgeP = Pattern.compile("(\\d+):(\\d+)(\\w)"); //the regex
    private Pattern insertP = Pattern.compile("\\[(.*?)\\]");

    private Matcher m = this.insertP.matcher(""); // blank matcher has to be reset
                                                // every time

    public TParser() {
        TParser.strings = new ArrayList<String>();
        this.stringsIterator = TParser.strings.iterator();
    }

    public TParser(String s) {
        this();
        this.setCurrent(s);
    }

    public TParser(String[] sarr) {
        TParser.strings = Arrays.asList(sarr);
        this.setCurrent((String) TParser.strings.get(0));
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public boolean hasInserts() {
        return (this.getVertexString().indexOf('[') > -1);
    }

    /**
     * These two methods are part of the java.util.Iterator interface... Might
     * be better to actually use a java.util.Collection as a composite
     */

    public String next() {
        return (String) this.stringsIterator.next();
    }

    public boolean hasNext() {
        return this.stringsIterator.hasNext();
    }

    public String getName() {
        int sp = this.current.indexOf(' ');
        return this.current.substring(0, sp);
    }

    public String getVertexString() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        String vertexString = this.current.substring(sp + 1, spsp);
        return vertexString;
    }

    public char[] getVertices() {
        return this.getVertexString().toCharArray();
    }

    public String getVertexStringSafely() {
        String vertexString = this.getVertexString();
        if (vertexString.indexOf("[") != -1) {
            char[] v = this.getVerticesWithInserts();
            return new String(v);
        } else {
            return vertexString;
        }
    }

    public char[] getVerticesWithInserts() {
        String[] s = this.insertP.split(this.getVertexString());
        char[] c = new char[s.length];
        // UGH!
        for (int i = 0; i < s.length; i++) {
            c[i] = s[i].charAt(0);
        }
        return c;
    }

    public String[] getInserts() {
        ArrayList<String> l = new ArrayList<String>();
        this.m.reset(this.getVertexString());
        while (this.m.find()) {
            String insert = this.m.group(1);
            l.add(insert); // add the next insert to the list (minus [])
        }
        return (String[]) l.toArray(new String[0]);
    }

    public String getEdgeString() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        int spspsp = this.current.indexOf(' ', spsp + 1);
        if (spspsp == -1)
            spspsp = this.current.length();
        return this.current.substring(spsp + 1, spspsp);
    }

    /*
     * removed because it doesn't work in java1.3 public String[] getEdges() {
     * String tail = this.getEdgeString(); Matcher m = edgeP.matcher(tail);
     * ArrayList bits = new ArrayList(); while (m.find()) { //start i at 1,
     * because group(0) is the whole match! for (int i = 1; i <= m.groupCount();
     * i++) { bits.add(m.group(i)); } } return (String[]) bits.toArray(new
     * String[0]); }
     */

    // ALTERNATIVE 1.3 method
    public String[] getEdges() {
        String tail = this.getEdgeString();
        ArrayList<String> bytes = new ArrayList<String>();
        char[] bits = tail.toCharArray();
        StringBuffer numstr = new StringBuffer();
        for (int i = 0; i < bits.length; i++) {
            char c = bits[i];

            if (Character.isDigit(c)) {
                numstr.append(c);
            } else if (Character.isLetter(c)) {
                bytes.add(numstr.toString());
                bytes.add(String.valueOf(c));
                numstr = new StringBuffer();
            } else {
                bytes.add(numstr.toString());
                numstr = new StringBuffer();
            }
        }
        return (String[]) bytes.toArray(new String[0]);
    }

    public String getClassification() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        int spspsp = this.current.indexOf(' ', spsp + 1);
        if (spspsp == -1)
            return "";
        return this.current.substring(spspsp);
    }

    public static void main(String[] args) {
//        String pattern = args[0];
        /*
         * TParser t = new TParser(pattern); System.out.println(t.getName());
         * System.out.println(t.getVertices()); String[] ins = t.getInserts();
         * for (int l = 0; l < ins.length; l++) { System.out.println("[" +
         * ins[l] + "]"); } String[] sta = t.getEdges(); for (int k = 0; k <
         * sta.length; k++) { System.out.println("[" + sta[k] + "]"); }
         */
//        TParser t = new TParser();
    }
}// EOC
