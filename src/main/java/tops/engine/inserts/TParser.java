package tops.engine.inserts;

//VERSION WITH INSERTS NE[HH]EC etc

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static Logger logger = Logger.getLogger(TParser.class.getName());

    public TParser() {
        TParser.strings = new ArrayList<>();
        this.stringsIterator = TParser.strings.iterator();
        TParser.logger.setLevel(Level.OFF);
        TParser.logger.setUseParentHandlers(false);
        TParser.logger.entering("tops.engine.inserts.TParser", "TParser");
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

    public tops.engine.inserts.Pattern parse(String pattern) {
        this.setCurrent(pattern);
        
        // XXX unfortunately means no patterns like "Npattern NEeC 1:2A"!
        if (pattern.charAt(0) == 'N') {	
            pattern = "head " + pattern; // name anonymous patterns!
        }

        // make a pattern
        tops.engine.inserts.Pattern p = new tops.engine.inserts.Pattern();

        char[] vertices;
        String[] inserts;
        boolean hasInserts = this.hasInserts();
        if (hasInserts) {
        	vertices = this.getVerticesWithInserts();
        } else {
        	vertices = this.getVertices();
        }
        p.setVertices(vertices);

        if (hasInserts) {
        	inserts = this.getInserts();
        	p.setInserts(inserts);
        } else {
        	p.convertDisconnectedVerticesToInserts();
        }

        String[] edges = this.getEdges();
        p.setEdges(edges, hasInserts);
        p.sortEdges();

        return p;
    }

    public boolean hasInserts() {
        return (this.getVertexString().indexOf('[') > -1);
    }

    /**
     * These two methods are part of the java.util.Iterator interface... Might
     * be better to actually use a java.util.Collection as a composite
     */

    public String next() {
        return this.stringsIterator.next();
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
        TParser.logger.info("vertex string : " + vertexString);
        return vertexString;
    }

    public char[] getVertices() {
        return this.getVertexString().toCharArray();
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
        List<String> l = new ArrayList<>();
        this.m.reset(this.getVertexString());
        while (this.m.find()) {
            String insert = this.m.group(1);
            logger.log(Level.INFO, "insert : {0}", insert);
            l.add(insert); // add the next insert to the list (minus [])
        }
        return l.toArray(new String[0]);
    }

    public String getEdgeString() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        int spspsp = this.current.indexOf(' ', spsp + 1);
        if (spspsp == -1)
            spspsp = this.current.length();
        return this.current.substring(spsp + 1, spspsp);
    }

    public String[] getEdges() {
        String tail = this.getEdgeString();
        List<String> bytes = new ArrayList<>();
        char[] bits = tail.toCharArray();
        StringBuilder numstr = new StringBuilder();
        for (int i = 0; i < bits.length; i++) {
            char c = bits[i];

            if (Character.isDigit(c)) {
                numstr.append(c);
            } else if (Character.isLetter(c)) {
                bytes.add(numstr.toString());
                bytes.add(String.valueOf(c));
                numstr = new StringBuilder();
            } else {
                bytes.add(numstr.toString());
                numstr = new StringBuilder();
            }
        }
        return bytes.toArray(new String[0]);
    }

    public String getClassification() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        int spspsp = this.current.indexOf(' ', spsp + 1);
        if (spspsp == -1)
            return "";
        return this.current.substring(spspsp);
    }
}
