package tops.web.display.servlet;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class TopsFileFilter implements FilenameFilter {

    private String pdbid;

    private Pattern p;

    public TopsFileFilter(String pdbid, String chain) {
        this.pdbid = pdbid;
        if (chain.equals("")) {
            this.p = Pattern.compile(pdbid + "..tops");
        } else {
            this.p = Pattern.compile(pdbid + chain + ".tops");
        }
    }

    public boolean accept(File directory, String name) {
        if (this.pdbid.equals("")) {
            return false; // okay, don't allow people to retrieve a particular domain from ALL proteins!
        }
        boolean matches = (this.p.matcher(name)).matches();
        return matches;
    }

}
