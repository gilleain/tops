package tops.web.display.servlet;

import java.io.File;
import java.io.FilenameFilter;

public class TopsFileFilter implements FilenameFilter {

    String pdbid;

    String chain;

    java.util.regex.Pattern p;

    public TopsFileFilter(String pdbid, String chain) {
        this.pdbid = pdbid;
        this.chain = chain;
        if (chain.equals("")) {
            this.p = java.util.regex.Pattern.compile(pdbid + "..tops");
        } else {
            this.p = java.util.regex.Pattern.compile(pdbid + chain + ".tops");
        }
    }

    public boolean accept(File directory, String name) {
        if (this.pdbid.equals(""))
            return false; // okay, don't allow people to retrive a particular
                            // domain from ALL proteins!
        boolean matches = (this.p.matcher(name)).matches();
        // System.out.println("matching " + p.pattern() + " to " + name + "
        // result : " + matches);
        return matches;
    }

} // EOC
