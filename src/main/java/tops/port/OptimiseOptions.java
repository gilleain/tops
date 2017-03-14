package tops.port;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class OptimiseOptions {

    private int anglePenalty = 0;
    private int arcsSample = 0;
    private int chainPenalty = 5;
    private int clashPenalty = 1000;
    private int crossPenalty = 0;
    private int decrement = 10;
    private long finishTemperature = 0;
    private int gridSize = 50;
    private int gridUnitSize = 50;
    private int handPenalty = 100;
    private int insideBarrelPenalty = 100;
    private int lineSample = 50;
    private int multiplicity = 6;
    private int neighbourPenalty = 100;
    private int noConfigs = 250;
    private int randomSeed = 28464;
    private long startTemperature = 100;
    private int stepSize = 100;

    /*
     * The grid unit size is 2.5 * r
     */
    public void setGridUnitSize(int radius) {
        gridUnitSize = 2 * radius + radius / 2;
    }

    public void parseArguments(String[] args) {
        int ErrorStatus = 0;
        String ErrStr = "";

        for (int i = 1; i < args.length; i++) {
            String c = args[i];
            char s = (c.charAt(0) == '-' || c.charAt(0) == '+' ? c.charAt(0)
                    : '*');
            char Cmd = c.charAt(0);

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
                            startTemperature = Integer.valueOf(c);
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
        }

        if (ErrorStatus > 0) {
            log("%s\n", ErrStr);
        }
    }

    public void printRunParams(PrintStream out) {
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
        print(out, "GridSize %d\n", gridSize);
        print(out, "StartTemperature %d\n", startTemperature);
        print(out, "FinishTemperature %d\n", finishTemperature);
        print(out, "NoConfigs %d\n", noConfigs);
        print(out, "Decrement %d\n", decrement);
        print(out, "StepSize %d\n", stepSize);
        print(out, "LineSample %d\n", lineSample);
        print(out, "ArcSample %d\n", arcsSample);
        print(out, "RandomSeed %d\n", randomSeed);
    }

    private void print(PrintStream out, String s, Object... args) {
        out.print(String.format(s, args));
    }

    private void log(String string, Object... args) {
        System.out.println(String.format(string, args));
    }

    /*
     * Function to check runtime options are reasonable
     */
    public void checkOptions() throws Exception {
        if (gridSize <= 0) {
            throw new Exception("ERROR: GridSize negative or zero");
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
        if (startTemperature < 0) {
            throw new Exception("ERROR: StartTemperature negative");
        }
        if ((startTemperature - finishTemperature) <= 0) {
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

    public int readDefaults(BufferedReader Def) throws IOException {

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
                } else if ("StartTemperature".equals(key)) {
                    try {
                        startTemperature = Integer.parseInt(value);
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
                } else {
                    ErrorStatus = 1;
                    ErrStr = String.format(
                            "ERROR: format of defaults file: at line: %s",
                            buffer);
                    break;
                }
            }
        }

        if (ErrorStatus > 0) {
            System.out.println(String.format("%s\n", ErrStr));
        }

        return ErrorStatus;
    }

}
