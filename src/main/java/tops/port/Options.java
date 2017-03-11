package tops.port;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class Options {
    
    private String PDBFilePath = "./";
    private String STRIDEFilePath = "./";
    private String stride_pref = null;
    private String dssp_pref = null;
    private String pdb_pref = "pdb";
    private String stride_ext = "stride";
    private String dssp_ext = "dssp";
    private String pdb_ext = "ent";
    
    private String Postscript = null; /* Save picture into postscript file */
    private String TopsFilename = null;
    private String DomBoundaryFile = null;
    private String DSSPFilePath = "./";
    private boolean Verbose = false;
    private String ChainToPlot = null;
    private int DomainToPlot = 0;
    private String FileType = null;
    
    private int Radius = 20; /* The radius of the symbols */
    private int GridUnitSize = 50; /* Grid unit size */
    private int GridSize = 50; /* Grid Size */
    private int MergeStrands = 5; /* ?? */
    private boolean MergeBetweenSheets;
    private double CutoffDistance = 20.0; /* ?? */
    private boolean Strands = true; /* Use strands in topology */
    private boolean Helices = true; /* Use helices in topology */
    private int AnglePenalty = 0;
    private int Multiplicity = 6;
    private int ChainPenalty = 5;
    private int ClashPenalty = 1000;
    private int CrossPenalty = 0;
    private int HandPenalty = 100;
    private int NeighbourPenalty = 100;
    private int InsideBarrelPenalty = 100;
    private int NoConfigs = 250;
    private int RandomSeed = 28464;
    private long Temperature = 100;
    private long FinishTemperature = 0;
    private int Decrement = 10;
    private int StepSize = 100;
    private int ArcsSample = 0;
    private int LineSample = 50;
   
    public int getRadius() {
        return Radius;
    }
    
    public boolean isVerbose() {
        return Verbose;
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
                        AnglePenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Multiplicity".equals(key)) {
                    try {
                        Multiplicity = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("MergeBetweenSheets".equals(key)) {
                    if ((value.charAt(0) == 'F') || (value.charAt(0) == '0')) {
                        MergeBetweenSheets = false;
                    } else {
                        MergeBetweenSheets = true;
                    }
                } else if ("ClashPenalty".equals(key)) {
                    try {
                        ClashPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("ChainPenalty".equals(key)) {
                    try {
                        ChainPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Decrement".equals(key)) {
                    try {
                        Decrement = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("GridSize".equals(key)) {
                    try {
                        GridSize = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("LineSample".equals(key)) {
                    try {
                        LineSample = Integer.parseInt(value);
                    } catch (Exception e) {

                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("HandPenalty".equals(key)) {
                    try {
                        HandPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("MergeStrands".equals(key)) {
                    try {
                        MergeStrands = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("NoConfigs".equals(key)) {
                    try {
                        NoConfigs = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("NeighbourPenalty".equals(key)) {
                    try {
                        NeighbourPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("InsideBarrelPenalty".equals(key)) {
                    try {
                        InsideBarrelPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("CutoffDistance".equals(key)) {
                    try {
                        CutoffDistance = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Postscript".equals(key)) {
                    Postscript = value;
                } else if ("ArcSample".equals(key)) {
                    try {
                        ArcsSample = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("StepSize".equals(key)) {
                    try {
                        StepSize = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("FileType".equals(key)) {
                    FileType = value;
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
                        Temperature = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("FinishTemperature".equals(key)) {
                    try {
                        FinishTemperature = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Radius".equals(key)) {
                    try {
                        Radius = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("Verbose".equals(key)) {
                    if ((value.charAt(0) == 'T') || (value.charAt(0) == '1')) {
                        Verbose = true;
                    } else {
                        Verbose = false;
                    }
                } else if ("CrossPenalty".equals(key)) {
                    try {
                        CrossPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("RandomSeed".equals(key)) {
                    try {
                        RandomSeed = Integer.parseInt(value);
                    } catch (Exception e) {
                        ErrorStatus = 1;
                        ErrStr = String.format(
                                "ERROR: unable to read %s from defaults file",
                                key);
                        break;
                    }
                } else if ("DomainFile".equals(key)) {
                    DomBoundaryFile = value;
                } else if ("PDBDirectory".equals(key)) {
                    PDBFilePath = value;
                } else if ("STRIDEDirectory".equals(key)) {
                    STRIDEFilePath = value;
                } else if ("DSSPDirectory".equals(key)) {
                    DSSPFilePath = value;
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
        GridUnitSize = 2 * Radius + Radius / 2;
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
                        AnglePenalty = Integer.valueOf(args[i]);
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
                        ClashPenalty = Integer.valueOf(args[i]);
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
                        ChainPenalty = Integer.valueOf(args[i]);
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
                        HandPenalty = Integer.valueOf(args[i]);
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
                        InsideBarrelPenalty = Integer.valueOf(args[i]);
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
                        Multiplicity = Integer.valueOf(c);
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
                        NeighbourPenalty = Integer.valueOf(c);
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
                    Postscript = c;
                    if ((Postscript.charAt(0) == '-')
                            || (Postscript.charAt(0) == '+')) {
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
                        Radius = Integer.valueOf(c);
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
                    TopsFilename = c;
                    if ((TopsFilename.charAt(0) == '-')
                            || (TopsFilename.charAt(0) == '+')) {
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
                    Verbose = false;
                } else {
                    Verbose = true;
                }
                break;
            case 'x':
                if (++i < args.length) {
                    try {
                        CrossPenalty = Integer.valueOf(c);
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
                    DomBoundaryFile = c;
                    if ((DomBoundaryFile.charAt(0) == '-')
                            || (DomBoundaryFile.charAt(0) == '+')) {
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
                    ChainToPlot = c;
                    if ((ChainToPlot.charAt(0) == '-')
                            || (ChainToPlot.charAt(0) == '+')) {
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
                        DomainToPlot = Integer.valueOf(c);
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
                    FileType = c;
                    if ((FileType.charAt(0) == '-')
                            || (FileType.charAt(0) == '+')) {
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
                        GridSize = Integer.valueOf(c);
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
                        MergeStrands = Integer.valueOf(c);
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
                    DSSPFilePath = c;
                    if ((DSSPFilePath.charAt(0) == '-')
                            || (DSSPFilePath.charAt(0) == '+')) {
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
                        NoConfigs = Integer.valueOf(c);
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
                        RandomSeed = Integer.valueOf(c);
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
                        Temperature = Integer.valueOf(c);
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
                        FinishTemperature = Integer.valueOf(c);
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
                        Decrement = Integer.valueOf(c);
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

        if (GridSize <= 0) {
            throw new Exception("ERROR: GridSize negative or zero");
        }
        if (Radius <= 0) {
            throw new Exception("ERROR: Radius negative or zero");
        }
        if (MergeStrands < 0) {
            throw new Exception("ERROR: MergeStrands negative");
        }
        if (ClashPenalty < 0) {
            throw new Exception("ERROR: ClashPenalty negative");
        }
        if (AnglePenalty < 0) {
            throw new Exception("ERROR: AnglePenalty negative");
        }
        if (AnglePenalty != 0 && (Multiplicity <= 0)) {
            throw new Exception("ERROR: Multiplicity negative or zero");
        }
        if (ChainPenalty < 0) {
            throw new Exception("ERROR: ChainPenalty negative");
        }
        if (CrossPenalty < 0) {
            throw new Exception("ERROR: CrossPenalty negative");
        }
        if (HandPenalty < 0) {
            throw new Exception("ERROR: HandPenalty negative");
        }
        if (NeighbourPenalty < 0) {
            throw new Exception("ERROR: NeighbourPenalty negative");
        }
        if (CutoffDistance <= 0.0) {
            throw new Exception("ERROR: CutoffDistance negative");
        }
        if (InsideBarrelPenalty < 0) {
            throw new Exception("ERROR: InsideBarrelPenalty negative");
        }
        if (NoConfigs <= 0) {
            throw new Exception("ERROR: NoConfigs negative or zero");
        }
        if (RandomSeed <= 0) {
            throw new Exception("ERROR: RandomSeed negative or zero");
        }
        if (FinishTemperature < 0) {
            throw new Exception("ERROR: FinishTemperature negative");
        }
        if (Temperature < 0) {
            throw new Exception("ERROR: StartTemperature negative");
        }
        if ((Temperature - FinishTemperature) <= 0) {
            throw new Exception(
                    "ERROR: FinishTemperature greater than or equal start temperature");
        }
        if ((Decrement <= 0) || (Decrement >= 100)) {
            throw new Exception(
                    "ERROR: Temperature decrement must be between 1 and 99 percent");
        }
        if (StepSize <= 0) {
            throw new Exception("ERROR: StepSize negative or zero");
        }
        if ((LineSample < 0) || (LineSample >= 100)) {
            throw new Exception(
                    "ERROR: LineSample must be between 0 and 99 percent");
        }
        if ((ArcsSample < 0) || (ArcsSample >= 100)) {
            throw new Exception(
                    "ERROR: ArcSample must be between 0 and 99 percent");
        }

    }
    
    public void PrintRunParams(PrintStream out) {

        print(out, "===================================================\n");
        print(out, "===================================================\n\n");
        print(out, "Parameters for this run:\n\n");
        print(out, "ClashPenalty %d\n", ClashPenalty);
        print(out, "ChainPenalty %d\n", ChainPenalty);
        print(out, "HandPenalty %d\n", HandPenalty);
        print(out, "CrossPenalty %d\n", CrossPenalty);
        print(out, "AnglePenalty %d\n", AnglePenalty);
        print(out, "Multiplicity %d\n", Multiplicity);
        print(out, "NeighbourPenalty %d\n", NeighbourPenalty);
        print(out, "InsideBarrelPenalty %d\n", InsideBarrelPenalty);
        print(out, "CutoffDistance %f\n", CutoffDistance);
        print(out, "\n");

        print(out, "Radius %d\n", Radius);
        print(out, "GridSize %d\n", GridSize);
        print(out, "MergeStrands %d\n", MergeStrands);
        if (MergeBetweenSheets)
            print(out, "MergeBetweenSheets true\n");
        else
            print(out, "MergeBetweenSheets false\n");
        print(out, "\n");

        print(out, "StartTemperature %d\n", Temperature);
        print(out, "FinishTemperature %d\n", FinishTemperature);
        print(out, "NoConfigs %d\n", NoConfigs);
        print(out, "Decrement %d\n", Decrement);
        print(out, "StepSize %d\n", StepSize);
        print(out, "LineSample %d\n", LineSample);
        print(out, "ArcSample %d\n", ArcsSample);
        print(out, "RandomSeed %d\n", RandomSeed);
        print(out, "\n");

        if (Postscript != null)
            print(out, "Postscript file %s\n", Postscript);
        print(out, "FileType %s\n", FileType);
        print(out, "DSSPDirectory %s\n", DSSPFilePath);
        print(out, "PDBDirectory %s\n", PDBFilePath);
        print(out, "STRIDEDirectory %s\n", STRIDEFilePath);
        if (DomBoundaryFile != null)
            print(out, "DomainFile %s\n", DomBoundaryFile);
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

        if (ChainToPlot != null)
            print(out, "Chain to plot %s\n", ChainToPlot);
        if (DomainToPlot > 0)
            print(out, "DomainToPlot %d\n", DomainToPlot);

        print(out, "===================================================\n");
        print(out, "===================================================\n");
    }
    
    private void print(PrintStream out, String s, Object... args) {
        out.print(String.format(s, args));
    }

}
