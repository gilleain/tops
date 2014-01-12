package tops.engine.juris;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TParser {

    String current;

    String[] toDo;

    int index;

    Pattern edgeP = Pattern.compile("(\\d+):(\\d+)(\\w)"); // the regex

    public TParser() {
        this.toDo = null;
    }

    public TParser(String s) {
        this.current = s;
        this.toDo = null;
    }

    public TParser(String[] sarr) {
        this.toDo = sarr;
        this.current = this.toDo[0];
        this.index = 0;
    }

    public int next() {
        if (this.index < this.toDo.length)
            this.current = this.toDo[this.index++];
        else
            return -1;
        return 1;
    }

    public void load(String s) {
        this.current = s;
    }

    public String getName() {
        int sp = this.current.indexOf(' ');
        return this.current.substring(0, sp);
    }

    public String getVertexString() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        return this.current.substring(sp + 1, spsp);
    }

    public char[] getVertices() {
        return this.getVertexString().toCharArray();
    }

    public String getEdgeString() {
        int sp = this.current.indexOf(' ');
        int spsp = this.current.indexOf(' ', sp + 1);
        int spspsp = this.current.indexOf(' ', spsp + 1);
        if (spspsp == -1)
            spspsp = this.current.length();
        return this.current.substring(spsp, spspsp);
    }

    public String[] getEdges() {
        String tail = this.getEdgeString();
        Matcher m = this.edgeP.matcher(tail);
        ArrayList<String> bits = new ArrayList<String>();
        while (m.find()) {
            // start i at 1, because group(0) is the whole match!
            for (int i = 1; i <= m.groupCount(); i++) {
                bits.add(m.group(i));
            }
        }
        return (String[]) bits.toArray(new String[0]);
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
        TParser t = new TParser(args[0]);
        System.out.println(t.getName());
        System.out.println(t.getVertices());
        String[] sta = t.getEdges();
        for (int k = 0; k < sta.length; k++) {
            System.out.println(sta[k]);
        }
    }
}// EOC
