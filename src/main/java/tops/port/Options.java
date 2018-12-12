package tops.port;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class Options {
    
    private String defaultFileType = "dssp";
    
    private String pdbFilePath = "./";
    private String pdbPref = "pdb";
    private String pdbExt = "ent";
    
    private String strideFilePath = "./";
    private String stridePref = null;
    private String strideExt = "stride";
    
    private String dsspFilePath = "./";
    private String dsspPref = null;
    private String dsspExt = "dssp";
    
    private String postscript = null; /* Save picture into postscript file */
    private String topsFilename = null;
    private String domBoundaryFile = null;
    private boolean verbose = false;
    private String chainToPlot = null;
    private int domainToPlot = 0;
    private String fileType = null;
    
    private int radius = 20; /* The radius of the symbols */
    private int mergeStrands = 5; /* ?? */
    private boolean mergeBetweenSheets;
    private double cutoffDistance = 20.0; /* ?? */
   
    
    public String getTOPSFileName(String pcode, String chainToPlot, int domainToPlot) {
        return pcode + chainToPlot + domainToPlot + ".tops";  // TODO
    }

    public String getSTRIDEFileName(String pcode) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPDBFileName(String pcode) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDSSPFileName(String pcode) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public int getDomainToPlot() {
        return domainToPlot;
    }
    
    public String getChainToPlot() {
        return chainToPlot;
    }
    
    public String getDomBoundaryFile() {
        return domBoundaryFile;
    }
    
    public String getTopsFilename() {
        return topsFilename;
    }
    
    public String getPostscript() {
        return postscript;
    }
   
    public String getDsspFilePath() {
        return dsspFilePath;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public boolean isVerbose() {
        return verbose;
    }

    public int readDefaults(BufferedReader def) throws IOException {

        String buffer;
        String errStr = "";

        int errorStatus = 0;
        final String unableToReadError = "ERROR: unable to read %s from defaults file";

        while ((buffer = def.readLine()) != null) {

            /* skip comments and blank lines */
            if (buffer.charAt(0) == '#' || buffer.charAt(0) == '\n')
                continue;

            int indexOfSpace = buffer.indexOf(' ');
            if (indexOfSpace > 0) {
                String key = buffer.substring(0, indexOfSpace);
                String value = buffer.substring(indexOfSpace);

                /* go through the possibilities */
                if ("MergeBetweenSheets".equals(key)) {
                    if ((value.charAt(0) == 'F') || (value.charAt(0) == '0')) {
                        mergeBetweenSheets = false;
                    } else {
                        mergeBetweenSheets = true;
                    }
                
                } else if ("MergeStrands".equals(key)) {
                    try {
                        mergeStrands = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(unableToReadError, key);
                        break;
                    }
                } else if ("CutoffDistance".equals(key)) {
                    try {
                        cutoffDistance = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(unableToReadError, key);
                        break;
                    }
                } else if ("Postscript".equals(key)) {
                    postscript = value;
                
                } else if ("FileType".equals(key)) {
                    fileType = value;
                    if (fileType == null) {
                        fileType = defaultFileType;
                    }
                    fileType = fileType.toLowerCase();
                } else if ("DSSPPrefix".equals(key)) {
                    dsspPref = value;
                } else if ("DSSPExtension".equals(key)) {
                    dsspExt = value;
                } else if ("PDBPrefix".equals(key)) {
                    pdbPref = value;
                } else if ("PDBExtension".equals(key)) {
                    pdbExt = value;
                } else if ("STRIDEPrefix".equals(key)) {
                    stridePref = value;
                } else if ("STRIDEExtension".equals(key)) {
                    strideExt = value;
                
                } else if ("Radius".equals(key)) {
                    try {
                        radius = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(unableToReadError, key);
                        break;
                    }
                } else if ("Verbose".equals(key)) {
                    if ((value.charAt(0) == 'T') || (value.charAt(0) == '1')) {
                        verbose = true;
                    } else {
                        verbose = false;
                    }
                
                } else if ("DomainFile".equals(key)) {
                    domBoundaryFile = value;
                } else if ("PDBDirectory".equals(key)) {
                    pdbFilePath = value;
                } else if ("STRIDEDirectory".equals(key)) {
                    strideFilePath = value;
                } else if ("DSSPDirectory".equals(key)) {
                    dsspFilePath = value;
                } else {
                    errorStatus = 1;
                    errStr = String.format(
                            "ERROR: unrecognized key %s in tops.def", key);
                    break;
                }

            } else {
                errorStatus = 1;
                errStr = String.format(
                        "ERROR: format of defaults file: at line: %s", buffer);
                break;
            }

        }

        if (errorStatus > 0) {
            System.out.println(String.format("%s\n", errStr));
        }

        return errorStatus;
    }
    
    
    
    public String parseArguments(String[] args) {

        int i;
        int cmd;
        String pcode = null;
        char s;
        String errStr = "";

        int errorStatus = 0;

        /* go through arguments one by one */
        for (i = 1; i < args.length; i++) {
            String c = args[i];

            s = (c.charAt(0) == '-' || c.charAt(0) == '+' ? c.charAt(0) : '*');

            /* if s was not a + or a - then this argument must be the pcode */
            if (s == '*') {
                if (pcode == null) {
                    pcode = c;
                    continue;
                } else {
                    errorStatus = 1;
                    errStr = "ERROR: command line incorrect (too many switch free argments)";
                    break;
                }
            }

            /* now parse the options */
            if (c.length() != 2) {
                errorStatus = 1;
                log("ERROR: unrecognized switch %s", c);
                return null;
            }

            cmd = args[i].charAt(0);

            switch (cmd) {
            
            case 'p':
                if (++i < args.length) {
                    postscript = c;
                    if ((postscript.charAt(0) == '-')
                            || (postscript.charAt(0) == '+')) {
                        errStr = String.format(
                                "ERROR: postscript file must not begin with + or - %s",
                                c);
                        errorStatus = 1;
                    }
                } else {
                    errStr = "ERROR: No postscript file after -p\n";
                    errorStatus = 1;
                }
                break;
            case 'r':
                if (++i < args.length) {
                    try {
                        radius = Integer.valueOf(c);
                    } catch (Exception e) {

                        errStr = String.format(
                                "ERROR: unable to read Radius (int) after -r %s%n",
                                c);
                        errorStatus = 1;
                    }
                } else {
                    errStr = "ERROR: No radius after -r\n";
                    errorStatus = 1;
                }
                break;
            case 't':
                if (++i < args.length) {
                    topsFilename = c;
                    if ((topsFilename.charAt(0) == '-')
                            || (topsFilename.charAt(0) == '+')) {
                        errStr = String.format(
                                "ERROR: tops file must not begin with + or - %s",
                                c);
                        errorStatus = 1;
                    }
                } else {
                    errStr = "ERROR: No tops file after -t\n";
                    errorStatus = 1;
                }
                break;
            case 'v':
                if (s == '-') {
                    verbose = false;
                } else {
                    verbose = true;
                }
                break;
           
            case 'B':
                if (++i < args.length) {
                    domBoundaryFile = c;
                    if ((domBoundaryFile.charAt(0) == '-')
                            || (domBoundaryFile.charAt(0) == '+')) {
                        errStr = String.format(
                                "ERROR: domain boundary file must not begin with - or + %s",
                                c);
                        errorStatus = 1;
                    }
                } else {
                    errStr = "ERROR: No domain boundary file after -B\n";
                    errorStatus = 1;
                }
                break;
            case 'C':
                if (++i < args.length) {
                    chainToPlot = c;
                    if ((chainToPlot.charAt(0) == '-')
                            || (chainToPlot.charAt(0) == '+')) {
                        errStr = String.format(
                                "ERROR: chain to plot must not begin + or - %s",
                                c);
                        errorStatus = 1;
                    }
                } else {
                    errStr = "ERROR: No chain to plot after -C\n";
                    errorStatus = 1;
                }
                break;
            case 'D':
                if (++i < args.length) {
                    try {
                        domainToPlot = Integer.valueOf(c);
                    } catch (Exception e) {

                        errStr = String.format(
                                "ERROR: unable to read domain (int) after -D %s%n",
                                c);
                        errorStatus = 1;
                    }
                } else {
                    errStr = "ERROR: No domain to plot after -D\n";
                    errorStatus = 1;
                }
                break;
            case 'F':
                if (++i < args.length) {
                    fileType = c;
                    if ((fileType.charAt(0) == '-')
                            || (fileType.charAt(0) == '+')) {
                        errStr = String.format(
                                "ERROR: file type must begin with a single alphanumeric %s",
                                c);
                        errorStatus = 1;
                    }
                } else {
                    errStr = "ERROR: No file type after -F\n";
                    errorStatus = 1;
                }
                break;
            
            case 'M':
                if (++i < args.length) {
                    try {
                        mergeStrands = Integer.valueOf(c);
                    } catch (Exception e) {

                        errStr = String.format(
                                "ERROR: unable to read MergeStrands (int) after -M %s\n",
                                c);
                        errorStatus = 1;
                    }
                } else {
                    errStr = "ERROR: No MergeStrands after -M\n";
                    errorStatus = 1;
                }
                break;
            case 'P':
                if (++i < args.length) {
                    dsspFilePath = c;
                    if ((dsspFilePath.charAt(0) == '-')
                            || (dsspFilePath.charAt(0) == '+')) {
                        errStr = String.format(
                                "ERROR: DSSP file path must not begin with + or - %s",
                                c);
                        errorStatus = 1;
                    }
                } else {
                    errStr = "ERROR: No file path after -P\n";
                    errorStatus = 1;
                }
                break;
            
           
            default:
                errorStatus = 1;
                errStr = String.format("ERROR: unrecognised switch %c%c", s,
                        cmd);
                break;
            }

            if (errorStatus > 0)
                break;

        }

        if (errorStatus > 0) {
            log("%s\n", errStr);
            pcode = null;
        }

        if (pcode == null) {
            return null;
        } else {
            return pcode;
        }
    }
    
    private void log(String string, Object... args) {
        System.out.println(String.format(string, args));
    }
    
    /*
     * Function to check runtime options are reasonable
     */
    public void checkOptions() throws OptionException {
       
        if (radius <= 0) {
            throw new OptionException("ERROR: Radius negative or zero");
        }
        
        if (mergeStrands < 0) {
            throw new OptionException("ERROR: MergeStrands negative");
        }
        
        if (cutoffDistance <= 0.0) {
            throw new OptionException("ERROR: CutoffDistance negative");
        }

    }
    
    public class OptionException extends Exception {
        public OptionException(String message) {
            super(message);
        }
    }
    
    public void printRunParams(PrintStream out) {
        final String separator = "===================================================";
        print(out, separator + "\n");
        print(out, separator + "\n\n");
        print(out, "Parameters for this run:\n\n");
       
        print(out, "CutoffDistance %f\n", cutoffDistance);
        print(out, "\n");

        print(out, "Radius %d\n", radius);
        
        print(out, "MergeStrands %d\n", mergeStrands);
        if (mergeBetweenSheets)
            print(out, "MergeBetweenSheets true\n");
        else
            print(out, "MergeBetweenSheets false\n");
        print(out, "\n");

        
        print(out, "\n");

        if (postscript != null)
            print(out, "Postscript file %s\n", postscript);
        print(out, "FileType %s\n", fileType);
        print(out, "DSSPDirectory %s\n", dsspFilePath);
        print(out, "PDBDirectory %s\n", pdbFilePath);
        print(out, "STRIDEDirectory %s\n", strideFilePath);
        if (domBoundaryFile != null)
            print(out, "DomainFile %s\n", domBoundaryFile);
        else
            print(out, "DomainFile NON\n");
        if (dsspPref != null)
            print(out, "DSSP prefix %s\n", dsspPref);
        print(out, "DSSP extension %s\n", dsspExt);
        if (pdbPref != null)
            print(out, "PDB prefix %s\n", pdbPref);
        print(out, "PDB extension %s\n", pdbExt);
        if (stridePref != null)
            print(out, "STRIDE prefix %s\n", stridePref);
        print(out, "STRIDE extension %s\n", strideExt);
        print(out, "\n");

        if (chainToPlot != null)
            print(out, "Chain to plot %s\n", chainToPlot);
        if (domainToPlot > 0)
            print(out, "DomainToPlot %d\n", domainToPlot);

        print(out, separator + "\n");
        print(out, separator + "\n");
    }
    
    private void print(PrintStream out, String s, Object... args) {
        out.print(String.format(s, args));
    }


}
