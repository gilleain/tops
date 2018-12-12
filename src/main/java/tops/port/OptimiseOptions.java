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
    
    private int parseAnglePenalty(String arg) {
        try {
            anglePenalty = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log("ERROR: unable to read angle penalty (int) after -a %s\n", arg);
            return 1;
        }
    }
    
    public int parseClashPenalty(String arg) {
        try {
            clashPenalty = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log("ERROR: unable to read clash penalty (int) after -b %s\n", arg);
            return 1;
        }
    }
    
    private int parseChainPenalty(String arg) {
        try {
            chainPenalty = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log("ERROR: unable to read chain penalty (int) after -c %s\n", arg);
            return 1;
        }
    }
    
    private int parseHandPenalty(String arg) {
        try {
            handPenalty = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log("ERROR: unable to read hand penalty (int) after -h %s\n", arg);
            return 1;
        }
    }
    
    private int parseInsideBarrelPenalty(String arg) {
        try {
            insideBarrelPenalty = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log("ERROR: unable to read inside barrel penalty (int) after -i %s\n", arg);
            return 1;
        }
    }
    
    private int parseMultiplicity(String arg) {
        try {
            multiplicity = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log("ERROR: unable to read Multiplicity (int) after -m %s\n", arg);
            return 1;
        }
    }
    
    private int parseNeighbourPenalty(String arg) {
        try {
            neighbourPenalty = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log(String.format("ERROR: unable to read NeighbourPenalty (int) after -n %s%n", arg));
            return 1;
        }
    }
    
    private int parseCrossPenalty(String arg) {
        try {
            crossPenalty = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log(String.format("ERROR: unable to read CrossPenalty (int) after -x %s%n", arg));
            return 1;
        }
    }
    
    private int parseGridSize(String arg) {
        try {
            gridSize = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log(String.format("ERROR: unable to read grid size (int) after -G %s%n", arg));
            return 1;
        }
    }
    
    private int parseNoConfigs(String arg) {
        try {
            noConfigs = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log(String.format("ERROR: unable to read number of configs (int) after -N %s%n", arg));
            return 1;
        }
    }
    
    private int parseRandomSeed(String arg) {
        try {
            randomSeed = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log(String.format("ERROR: unable to read random seed (int) after -S %s%n", arg));
            return 1;
        }
    }
    
    private int parseStartTemperature(String arg) {
        try {
            startTemperature = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log(String.format("ERROR: unable to read finish temperature (int) after -U %s%n", arg));
            return 1;
        }
    }
    
    private int parseFinishTemperature(String arg) {
        try {
            finishTemperature = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log(String.format("ERROR: unable to read finish temperature (int) after -U %s%n", arg));
            return 1;
        }
    }
    
    private int parseTemperatureDecrement(String arg) {
        try {
            decrement = Integer.valueOf(arg);
            return 0;
        } catch (Exception e) {
            log(String.format("ERROR: unable to read temperature decrement (int) after -V %s%n", arg));
            return 1;
        }
    }

    public void parseArguments(String[] args) {
        int errorStatus = 0;
        String errStr = "";

        for (int i = 1; i < args.length; i++) {
            String c = args[i];
            char s = (c.charAt(0) == '-' || c.charAt(0) == '+' ? c.charAt(0) : '*');
            char cmd = c.charAt(0);
            boolean hasNext = i < args.length - 1;
            switch (cmd) {
                case 'a':
                    if (hasNext) {
                       errorStatus = parseAnglePenalty(args[i + 1]);
                    } else {
                        log("ERROR: No angle penalty after -a\n");
                        errorStatus = 1;
                    }
                    break;
                case 'b':
                    if (hasNext) {
                        errorStatus = parseClashPenalty(args[i + 1]);
                    } else {
                        log("ERROR: No clash penalty after -b\n");
                        errorStatus = 1;
                    }
                    break;
                case 'c':
                    if (hasNext) {
                       errorStatus = parseChainPenalty(args[i + 1]);
                    } else {
                        log("ERROR: No chain penalty after -c\n");
                        errorStatus = 1;
                    }
                    break;
                case 'h':
                    if (hasNext) {
                        errorStatus = parseHandPenalty(args[i + 1]);
                    } else {
                        log("ERROR: No hand penalty after -h\n");
                        errorStatus = 1;
                    }
                    break;
                case 'i':
                    if (hasNext) {
                       errorStatus = parseInsideBarrelPenalty(args[i + 1]);
                    } else {
                        log("ERROR: No inside barrel penalty after -h\n");
                        errorStatus = 1;
                    }
                    break;
                case 'm':
                    if (hasNext) {
                       errorStatus = parseMultiplicity(c);
                    } else {
                        log("ERROR: No multiplicity after -m\n");
                        errorStatus = 1;
                    }
                    break;
                case 'n':
                    if (hasNext) {
                       errorStatus = parseNeighbourPenalty(c);
                    } else {
                        log("ERROR: No NeighbourPenalty after -n\n");
                        errorStatus = 1;
                    }
                    break;
                case 'x':
                    if (hasNext) {
                       errorStatus = parseCrossPenalty(c);
                    } else {
                        errStr = "ERROR: No CrossPenalty after -x\n";
                        errorStatus = 1;
                    }
                    break;
                case 'G':
                    if (hasNext) {
                        errorStatus = parseGridSize(c);
                    } else {
                        errStr = "ERROR: No grid size after -G\n";
                        errorStatus = 1;
                    }
                    break;
                case 'N':
                    if (hasNext) {
                       errorStatus = parseNoConfigs(c);
                    } else {
                        errStr = "ERROR: No MergeStrands after -N\n";
                        errorStatus = 1;
                    }
                    break;
                case 'S':
                    if (hasNext) {
                       errorStatus = parseRandomSeed(c);
                    } else {
                        errStr = "ERROR: No random seed after -S\n";
                        errorStatus = 1;
                    }
                    break;
                case 'T':
                    if (hasNext) {
                        errorStatus = parseStartTemperature(c);
                    } else {
                        errStr = "ERROR: No temperature after -T\n";
                        errorStatus = 1;
                    }
                    break;
                case 'U':
                    if (hasNext) {
                        errorStatus = parseFinishTemperature(c);
                    } else {
                        errStr = "ERROR: No temperature after -U\n";
                        errorStatus = 1;
                    }
                    break;
                case 'V':
                    if (hasNext) {
                       errorStatus = parseTemperatureDecrement(c);
                    } else {
                        errStr = "ERROR: No temperature decrement after -V%n";
                        errorStatus = 1;
                    }
                    break;
                default:
                    errorStatus = 1;
                    errStr = String.format("ERROR: unrecognised switch %c%c", s,
                            cmd);
                    break;
            }
        }

        if (errorStatus > 0) {
            log("%s\n", errStr);
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
    public void checkOptions() throws OptimiseOptionException {
        if (gridSize <= 0) {
            throw new OptimiseOptionException("ERROR: GridSize negative or zero");
        }
        if (clashPenalty < 0) {
            throw new OptimiseOptionException("ERROR: ClashPenalty negative");
        }
        if (anglePenalty < 0) {
            throw new OptimiseOptionException("ERROR: AnglePenalty negative");
        }
        if (anglePenalty != 0 && (multiplicity <= 0)) {
            throw new OptimiseOptionException("ERROR: Multiplicity negative or zero");
        }
        if (chainPenalty < 0) {
            throw new OptimiseOptionException("ERROR: ChainPenalty negative");
        }
        if (crossPenalty < 0) {
            throw new OptimiseOptionException("ERROR: CrossPenalty negative");
        }
        if (handPenalty < 0) {
            throw new OptimiseOptionException("ERROR: HandPenalty negative");
        }
        if (neighbourPenalty < 0) {
            throw new OptimiseOptionException("ERROR: NeighbourPenalty negative");
        }
        if (insideBarrelPenalty < 0) {
            throw new OptimiseOptionException("ERROR: InsideBarrelPenalty negative");
        }
        if (noConfigs <= 0) {
            throw new OptimiseOptionException("ERROR: NoConfigs negative or zero");
        }
        if (randomSeed <= 0) {
            throw new OptimiseOptionException("ERROR: RandomSeed negative or zero");
        }
        if (finishTemperature < 0) {
            throw new OptimiseOptionException("ERROR: FinishTemperature negative");
        }
        if (startTemperature < 0) {
            throw new OptimiseOptionException("ERROR: StartTemperature negative");
        }
        if ((startTemperature - finishTemperature) <= 0) {
            throw new OptimiseOptionException(
                    "ERROR: FinishTemperature greater than or equal start temperature");
        }
        if ((decrement <= 0) || (decrement >= 100)) {
            throw new OptimiseOptionException(
                    "ERROR: Temperature decrement must be between 1 and 99 percent");
        }
        if (stepSize <= 0) {
            throw new OptimiseOptionException("ERROR: StepSize negative or zero");
        }
        if ((lineSample < 0) || (lineSample >= 100)) {
            throw new OptimiseOptionException(
                    "ERROR: LineSample must be between 0 and 99 percent");
        }
        if ((arcsSample < 0) || (arcsSample >= 100)) {
            throw new OptimiseOptionException(
                    "ERROR: ArcSample must be between 0 and 99 percent");
        }
    }

    public int readDefaults(BufferedReader def) throws IOException {

        String buffer;
        String errStr = "";

        int errorStatus = 0;

        while ((buffer = def.readLine()) != null) {

            /* skip comments and blank lines */
            if (buffer.charAt(0) == '#' || buffer.charAt(0) == '\n')
                continue;

            final String defaultsError = "ERROR: unable to read %s from defaults file";
            int indexOfSpace = buffer.indexOf(' ');
            if (indexOfSpace > 0) {
                String key = buffer.substring(0, indexOfSpace);
                String value = buffer.substring(indexOfSpace);

                /* go through the possibilities */
                if (key.equals("AnglePenalty")) {
                    try {
                        anglePenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("Multiplicity".equals(key)) {
                    try {
                        multiplicity = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("ClashPenalty".equals(key)) {
                    try {
                        clashPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("ChainPenalty".equals(key)) {
                    try {
                        chainPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("Decrement".equals(key)) {
                    try {
                        decrement = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("GridSize".equals(key)) {
                    try {
                        gridSize = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError ,key);
                        break;
                    }
                } else if ("LineSample".equals(key)) {
                    try {
                        lineSample = Integer.parseInt(value);
                    } catch (Exception e) {

                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("HandPenalty".equals(key)) {
                    try {
                        handPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("ArcSample".equals(key)) {
                    try {
                        arcsSample = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("StepSize".equals(key)) {
                    try {
                        stepSize = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("StartTemperature".equals(key)) {
                    try {
                        startTemperature = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("FinishTemperature".equals(key)) {
                    try {
                        finishTemperature = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("CrossPenalty".equals(key)) {
                    try {
                        crossPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("RandomSeed".equals(key)) {
                    try {
                        randomSeed = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("NoConfigs".equals(key)) {
                    try {
                        noConfigs = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("NeighbourPenalty".equals(key)) {
                    try {
                        neighbourPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else if ("InsideBarrelPenalty".equals(key)) {
                    try {
                        insideBarrelPenalty = Integer.parseInt(value);
                    } catch (Exception e) {
                        errorStatus = 1;
                        errStr = String.format(defaultsError, key);
                        break;
                    }
                } else {
                    errorStatus = 1;
                    errStr = String.format(defaultsError, buffer);
                    break;
                }
            }
        }

        if (errorStatus > 0) {
            System.out.println(String.format("%s\n", errStr));
        }

        return errorStatus;
    }

}
