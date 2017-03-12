package tops.port;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class Options {
    
    private String defaultFileType = "dssp";
    
    private String pdbFilePath = "./";
    private String pdb_pref = "pdb";
    private String pdb_ext = "ent";
    
    private String strideFilePath = "./";
    private String stride_pref = null;
    private String stride_ext = "stride";
    
    private String dsspFilePath = "./";
    private String dssp_pref = null;
    private String dssp_ext = "dssp";
    
    private String postscript = null; /* Save picture into postscript file */
    private String topsFilename = null;
    private String domBoundaryFile = null;
    private boolean verbose = false;
    private String chainToPlot = null;
    private int domainToPlot = 0;
    private String fileType = null;
    
    private int radius = 20; /* The radius of the symbols */
    private int gridUnitSize = 50; /* Grid unit size */
    private int gridSize = 50; /* Grid Size */
    private int mergeStrands = 5; /* ?? */
    private boolean mergeBetweenSheets;
    private double cutoffDistance = 20.0; /* ?? */
    private boolean strands = true; /* Use strands in topology */
    private boolean helices = true; /* Use helices in topology */
    
    private int anglePenalty = 0;
    private int multiplicity = 6;
    private int chainPenalty = 5;
    private int clashPenalty = 1000;
    private int crossPenalty = 0;
    private int handPenalty = 100;
    private int neighbourPenalty = 100;
    private int insideBarrelPenalty = 100;
    private int noConfigs = 250;
    private int randomSeed = 28464;
    private long temperature = 100;
    private long finishTemperature = 0;
    private int decrement = 10;
    private int stepSize = 100;
    private int arcsSample = 0;
    private int lineSample = 50;
    
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

    public int ReadDefaults(BufferedReader Def) throws IOException {

        String buffer;
        String ErrStr = "";

        int ErrorStatus = 0;

        while ((buffer = Def.readLine()) != null) {

            /* skip comments and blank lines */
            if (buffer.charAt(0) == '#' || buffer.charAt(0) == '\n')
                continue;

            int indexOfSpace = buffer.indexOf(' ');
            if (indexOfSpace > 0) {
                String key = buffer.substring(0, indexOfSpace);
                String value = buffer.substring(indexOfSpace);

                /* go through the possibilities */
                if (key.equals("AnglePenalty")) {
                    try {
                        anglePenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Multiplicity".equals(key)) {
                    try {
                        multiplicity = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("MergeBetweenSheets".equals(key)) {
                    if ((value.charAt(0) == 'F') || (value.charAt(0) == '0')) {
                        mergeBetweenSheets = false;
                    } else {
                        mergeBetweenSheets = true;
                    }
                } else if ("ClashPenalty".equals(key)) {
                    try {
                        clashPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("ChainPenalty".equals(key)) {
                    try {
                        chainPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Decrement".equals(key)) {
                    try {
                        decrement = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("GridSize".equals(key)) {
                    try {
                        gridSize = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("LineSample".equals(key)) {
                    try {
                        lineSample = Integer.parseInt(value);
                    } catch (Exception e) {

                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("HandPenalty".equals(key)) {
                    try {
                        handPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("MergeStrands".equals(key)) {
                    try {
                        mergeStrands = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("NoConfigs".equals(key)) {
                    try {
                        noConfigs = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("NeighbourPenalty".equals(key)) {
                    try {
                        neighbourPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("InsideBarrelPenalty".equals(key)) {
                    try {
                        insideBarrelPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("CutoffDistance".equals(key)) {
                    try {
                        cutoffDistance = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Postscript".equals(key)) {
                    postscript = value;
                } else if ("ArcSample".equals(key)) {
                    try {
                        arcsSample = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("StepSize".equals(key)) {
                    try {
                        stepSize = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("FileType".equals(key)) {
                    fileType = value;
                    if (fileType == null) {
                        fileType = defaultFileType;
                    }
                    fileType = fileType.toLowerCase();
                } else if ("DSSPPrefix".equals(key)) {
                    dssp_pref = value;
                } else if ("DSSPExtension".equals(key)) {
                    dssp_ext = value;
                } else if ("PDBPrefix".equals(key)) {
                    pdb_pref = value;
                } else if ("PDBExtension".equals(key)) {
                    pdb_ext = value;
                } else if ("STRIDEPrefix".equals(key)) {
                    stride_pref = value;
                } else if ("STRIDEExtension".equals(key)) {
                    stride_ext = value;
                } else if ("StartTemperature".equals(key)) {
                    try {
                        temperature = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("FinishTemperature".equals(key)) {
                    try {
                        finishTemperature = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Radius".equals(key)) {
                    try {
                        radius = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Verbose".equals(key)) {
                    if ((value.charAt(0) == 'T') || (value.charAt(0) == '1')) {
                        verbose = true;
                    } else {
                        verbose = false;
                    }
                } else if ("CrossPenalty".equals(key)) {
                    try {
                        crossPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("RandomSeed".equals(key)) {
                    try {
                        randomSeed = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
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
                    ErrorStatus = 1;
                    ErrStr = String.format(
                            "ERROR: unrecognized key %s in tops.def", key);
                    break;
                }

            } else {
                ErrorStatus = 1;
                ErrStr = String.format(
                        "ERROR: format of defaults file: at line: %s", buffer);
                break;
            }

        }

        if (ErrorStatus > 0) {
            System.out.println(String.format("%s\n", ErrStr));
        }

        return ErrorStatus;
    }
    
    /*
     * This function sets those global variables which are derived from inputs
     */
    public void SetGlobalVariables() { // TODO - rename
        gridUnitSize = 2 * radius + radius / 2;
    }
    
    public String CommandArguments(String[] args) {

        int i, Cmd;
        String pcode = null;
        char s;
        String ErrStr = "";

        int ErrorStatus = 0;

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
                    ErrorStatus = 1;
                    ErrStr = "ERROR: command line incorrect (too many switch free argments)";
                    break;
                }
            }

            /* now parse the options */
            if (c.length() != 2) {
                ErrorStatus = 1;
                log("ERROR: unrecognized switch %s", c);
                return null;
            }

            Cmd = args[i].charAt(0);

            switch (Cmd) {
            case 'a':
                if (++i < args.length) {
                    try {
                        anglePenalty = Integer.valueOf(args[i]);
                    } catch (Exception e) {
                        log("ERROR: unable to read angle penalty (int) after -a %s\n",
                                args[i]);
                        ErrorStatus = 1;
                    }
                } else {
                    log("ERROR: No angle penalty after -a\n");
                    ErrorStatus = 1;
                }
                break;
            case 'b':
                if (++i < args.length) {
                    try {
                        clashPenalty = Integer.valueOf(args[i]);
                    } catch (Exception e) {
                        log("ERROR: unable to read clash penalty (int) after -b %s\n",
                                args[i]);
                        ErrorStatus = 1;
                    }
                } else {
                    log("ERROR: No clash penalty after -b\n");
                    ErrorStatus = 1;
                }
                break;
            case 'c':
                if (++i < args.length) {
                    try {
                        chainPenalty = Integer.valueOf(args[i]);
                    } catch (Exception e) {
                        log("ERROR: unable to read chain penalty (int) after -c %s\n",
                                args[i]);
                        ErrorStatus = 1;
                    }
                } else {
                    log("ERROR: No chain penalty after -c\n");
                    ErrorStatus = 1;
                }
                break;
            case 'h':
                if (++i < args.length) {
                    try {
                        handPenalty = Integer.valueOf(args[i]);
                    } catch (Exception e) {
                        log(ErrStr,
                                "ERROR: unable to read hand penalty (int) after -h %s\n",
                                args[i]);
                        ErrorStatus = 1;
                    }
                } else {
                    log("ERROR: No hand penalty after -h\n");
                    ErrorStatus = 1;
                }
                break;
            case 'i':
                if (++i < args.length) {
                    try {
                        insideBarrelPenalty = Integer.valueOf(args[i]);
                    } catch (Exception e) {
                        log("ERROR: unable to read inside barrel penalty (int) after -i %s\n",
                                args[i]);
                        ErrorStatus = 1;
                    }
                } else {
                    log("ERROR: No inside barrel penalty after -h\n");
                    ErrorStatus = 1;
                }
                break;
            case 'm':
                if (++i < args.length) {
                    try {
                        multiplicity = Integer.valueOf(c);
                    } catch (Exception e) {

                        log("ERROR: unable to read Multiplicity (int) after -m %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    log("ERROR: No multiplicity after -m\n");
                    ErrorStatus = 1;
                }
                break;
            case 'n':
                if (++i < args.length) {
                    try {
                        neighbourPenalty = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read NeighbourPenalty (int) after -n %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No NeighbourPenalty after -n\n";
                    ErrorStatus = 1;
                }
                break;
            case 'p':
                if (++i < args.length) {
                    postscript = c;
                    if ((postscript.charAt(0) == '-')
                            || (postscript.charAt(0) == '+')) {
                        ErrStr = String.format(
                                "ERROR: postscript file must not begin with + or - %s",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No postscript file after -p\n";
                    ErrorStatus = 1;
                }
                break;
            case 'r':
                if (++i < args.length) {
                    try {
                        radius = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read Radius (int) after -r %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No radius after -r\n";
                    ErrorStatus = 1;
                }
                break;
            case 't':
                if (++i < args.length) {
                    topsFilename = c;
                    if ((topsFilename.charAt(0) == '-')
                            || (topsFilename.charAt(0) == '+')) {
                        ErrStr = String.format(
                                "ERROR: tops file must not begin with + or - %s",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No tops file after -t\n";
                    ErrorStatus = 1;
                }
                break;
            case 'v':
                if (s == '-') {
                    verbose = false;
                } else {
                    verbose = true;
                }
                break;
            case 'x':
                if (++i < args.length) {
                    try {
                        crossPenalty = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read CrossPenalty (int) after -x %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No CrossPenalty after -x\n";
                    ErrorStatus = 1;
                }
                break;
            case 'B':
                if (++i < args.length) {
                    domBoundaryFile = c;
                    if ((domBoundaryFile.charAt(0) == '-')
                            || (domBoundaryFile.charAt(0) == '+')) {
                        ErrStr = String.format(
                                "ERROR: domain boundary file must not begin with - or + %s",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No domain boundary file after -B\n";
                    ErrorStatus = 1;
                }
                break;
            case 'C':
                if (++i < args.length) {
                    chainToPlot = c;
                    if ((chainToPlot.charAt(0) == '-')
                            || (chainToPlot.charAt(0) == '+')) {
                        ErrStr = String.format(
                                "ERROR: chain to plot must not begin + or - %s",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No chain to plot after -C\n";
                    ErrorStatus = 1;
                }
                break;
            case 'D':
                if (++i < args.length) {
                    try {
                        domainToPlot = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read domain (int) after -D %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No domain to plot after -D\n";
                    ErrorStatus = 1;
                }
                break;
            case 'F':
                if (++i < args.length) {
                    fileType = c;
                    if ((fileType.charAt(0) == '-')
                            || (fileType.charAt(0) == '+')) {
                        ErrStr = String.format(
                                "ERROR: file type must begin with a single alphanumeric %s",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No file type after -F\n";
                    ErrorStatus = 1;
                }
                break;
            case 'G':
                if (++i < args.length) {
                    try {
                        gridSize = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read grid size (int) after -G %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No grid size after -G\n";
                    ErrorStatus = 1;
                }
                break;
            case 'M':
                if (++i < args.length) {
                    try {
                        mergeStrands = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read MergeStrands (int) after -M %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No MergeStrands after -M\n";
                    ErrorStatus = 1;
                }
                break;
            case 'P':
                if (++i < args.length) {
                    dsspFilePath = c;
                    if ((dsspFilePath.charAt(0) == '-')
                            || (dsspFilePath.charAt(0) == '+')) {
                        ErrStr = String.format(
                                "ERROR: DSSP file path must not begin with + or - %s",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No file path after -P\n";
                    ErrorStatus = 1;
                }
                break;
            case 'N':
                if (++i < args.length) {
                    try {
                        noConfigs = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read number of configs (int) after -N %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No MergeStrands after -N\n";
                    ErrorStatus = 1;
                }
                break;
            case 'S':
                if (++i < args.length) {
                    try {
                        randomSeed = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read random seed (int) after -S %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No random seed after -S\n";
                    ErrorStatus = 1;
                }
                break;
            case 'T':
                if (++i < args.length) {
                    try {
                        temperature = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read start temperature (int) after -T %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No temperature after -T\n";
                    ErrorStatus = 1;
                }
                break;
            case 'U':
                if (++i < args.length) {
                    try {
                        finishTemperature = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read finish temperature (int) after -U %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No temperature after -U\n";
                    ErrorStatus = 1;
                }
                break;
            case 'V':
                if (++i < args.length) {
                    try {
                        decrement = Integer.valueOf(c);
                    } catch (Exception e) {

                        ErrStr = String.format(
                                "ERROR: unable to read temperature decrement (int) after -V %s\n",
                                c);
                        ErrorStatus = 1;
                    }
                } else {
                    ErrStr = "ERROR: No temperature decrement after -V\n";
                    ErrorStatus = 1;
                }
                break;
            default:
                ErrorStatus = 1;
                ErrStr = String.format("ERROR: unrecognised switch %c%c", s,
                        Cmd);
                break;
            }

            if (ErrorStatus > 0)
                break;

        }

        if (ErrorStatus > 0) {
            log("%s\n", ErrStr);
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
    public void CheckOptions() throws Exception {

        if (gridSize <= 0) {
            throw new Exception("ERROR: GridSize negative or zero");
        }
        if (radius <= 0) {
            throw new Exception("ERROR: Radius negative or zero");
        }
        if (mergeStrands < 0) {
            throw new Exception("ERROR: MergeStrands negative");
        }
        if (clashPenalty < 0) {
            throw new Exception("ERROR: ClashPenalty negative");
        }
        if (anglePenalty < 0) {
            throw new Exception("ERROR: AnglePenalty negative");
        }
        if (anglePenalty != 0 && (multiplicity <= 0)) {
            throw new Exception("ERROR: Multiplicity negative or zero");
        }
        if (chainPenalty < 0) {
            throw new Exception("ERROR: ChainPenalty negative");
        }
        if (crossPenalty < 0) {
            throw new Exception("ERROR: CrossPenalty negative");
        }
        if (handPenalty < 0) {
            throw new Exception("ERROR: HandPenalty negative");
        }
        if (neighbourPenalty < 0) {
            throw new Exception("ERROR: NeighbourPenalty negative");
        }
        if (cutoffDistance <= 0.0) {
            throw new Exception("ERROR: CutoffDistance negative");
        }
        if (insideBarrelPenalty < 0) {
            throw new Exception("ERROR: InsideBarrelPenalty negative");
        }
        if (noConfigs <= 0) {
            throw new Exception("ERROR: NoConfigs negative or zero");
        }
        if (randomSeed <= 0) {
            throw new Exception("ERROR: RandomSeed negative or zero");
        }
        if (finishTemperature < 0) {
            throw new Exception("ERROR: FinishTemperature negative");
        }
        if (temperature < 0) {
            throw new Exception("ERROR: StartTemperature negative");
        }
        if ((temperature - finishTemperature) <= 0) {
            throw new Exception(
                    "ERROR: FinishTemperature greater than or equal start temperature");
        }
        if ((decrement <= 0) || (decrement >= 100)) {
            throw new Exception(
                    "ERROR: Temperature decrement must be between 1 and 99 percent");
        }
        if (stepSize <= 0) {
            throw new Exception("ERROR: StepSize negative or zero");
        }
        if ((lineSample < 0) || (lineSample >= 100)) {
            throw new Exception(
                    "ERROR: LineSample must be between 0 and 99 percent");
        }
        if ((arcsSample < 0) || (arcsSample >= 100)) {
            throw new Exception(
                    "ERROR: ArcSample must be between 0 and 99 percent");
        }

    }
    
    public void PrintRunParams(PrintStream out) {

        print(out, "===================================================\n");
        print(out, "===================================================\n\n");
        print(out, "Parameters for this run:\n\n");
        print(out, "ClashPenalty %d\n", clashPenalty);
        print(out, "ChainPenalty %d\n", chainPenalty);
        print(out, "HandPenalty %d\n", handPenalty);
        print(out, "CrossPenalty %d\n", crossPenalty);
        print(out, "AnglePenalty %d\n", anglePenalty);
        print(out, "Multiplicity %d\n", multiplicity);
        print(out, "NeighbourPenalty %d\n", neighbourPenalty);
        print(out, "InsideBarrelPenalty %d\n", insideBarrelPenalty);
        print(out, "CutoffDistance %f\n", cutoffDistance);
        print(out, "\n");

        print(out, "Radius %d\n", radius);
        print(out, "GridSize %d\n", gridSize);
        print(out, "MergeStrands %d\n", mergeStrands);
        if (mergeBetweenSheets)
            print(out, "MergeBetweenSheets true\n");
        else
            print(out, "MergeBetweenSheets false\n");
        print(out, "\n");

        print(out, "StartTemperature %d\n", temperature);
        print(out, "FinishTemperature %d\n", finishTemperature);
        print(out, "NoConfigs %d\n", noConfigs);
        print(out, "Decrement %d\n", decrement);
        print(out, "StepSize %d\n", stepSize);
        print(out, "LineSample %d\n", lineSample);
        print(out, "ArcSample %d\n", arcsSample);
        print(out, "RandomSeed %d\n", randomSeed);
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
        if (dssp_pref != null)
            print(out, "DSSP prefix %s\n", dssp_pref);
        print(out, "DSSP extension %s\n", dssp_ext);
        if (pdb_pref != null)
            print(out, "PDB prefix %s\n", pdb_pref);
        print(out, "PDB extension %s\n", pdb_ext);
        if (stride_pref != null)
            print(out, "STRIDE prefix %s\n", stride_pref);
        print(out, "STRIDE extension %s\n", stride_ext);
        print(out, "\n");

        if (chainToPlot != null)
            print(out, "Chain to plot %s\n", chainToPlot);
        if (domainToPlot > 0)
            print(out, "DomainToPlot %d\n", domainToPlot);

        print(out, "===================================================\n");
        print(out, "===================================================\n");
    }
    
    private void print(PrintStream out, String s, Object... args) {
        out.print(String.format(s, args));
    }


}
