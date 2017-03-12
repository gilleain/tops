package tops.port;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import tops.port.model.Cartoon;
import tops.port.model.DomainDefinition;
import tops.port.model.DsspReader;
import tops.port.model.PlotFragInformation;
import tops.port.model.Protein;
import tops.port.model.SSE;

/*
	Program:         TOPS
	Description: Program to automatically generate protein topology cartoons
	Version:         3
	Author: T. Flores (original) with further development by D. Westhead (EBI), 
	ported to Java by Gilleain Torrance.
*/

public class Tops {
    
    private double ProgramVersion = 2.0;
    
    private SSE Root = null; /* Pointer to link list of structures */
    private SSE OriginalRoot = null; /* Pointer to master link list of structures */
    private SSE CentreFixed = null;
    private double[][] CaXYZ = null; /* Calpha coordinates */
    private int NumberResidues = 0; /* Number of residues in protein */
    private boolean Small = false;
    private String defaultsFile = "tops.def";
    private String TOPS_HOME = null;
    private final int DOMS_PER_PAGE = 2;    // for ps output

    /* --------------------- */
    /* Main control function */
    /* --------------------- */

    public static void main(String[] args) throws IOException {
        new Tops().run(args);
    }

    private void log(String string, Object... args) {
        System.out.println(String.format(string, args));
    }

    public void run(String[] args) throws IOException {

        String Pcode;
        String ErrStr = null;
        int ErrorStatus = 0;
        File DefsFile = null;
        char[] dfp = new char[1024];

        /* get necessary env. vars. */
        TOPS_HOME = System.getenv("TOPS_HOME");
        Options options = new Options(); 

        /* Parse defaults file */
        BufferedReader reader = null;
        try {
            if (TOPS_HOME != null) {
                defaultsFile = TOPS_HOME + "/" + defaultsFile;
                DefsFile = new File(defaultsFile);
            }
            if (!DefsFile.canRead()) {
                log("Cannot read file%s\n", defaultsFile);
                return;
            }
            reader = new BufferedReader(new FileReader(DefsFile));
            ErrorStatus = options.readDefaults(reader);
            if (ErrorStatus != 0) {
                log("ERROR: while reading %s defaults file\n", defaultsFile);
                reader.close();
                return;
            }
        } catch (Exception e) {
            log("Unable to open defaults file %s\n", defaultsFile);
            return;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        /* Parse command line arguments */
        Pcode = options.parseArguments(args);
        if (ErrorStatus > 0) {
            log("Tops error: in CommandArguments, status %d\n", ErrorStatus);
            System.exit(1);
        }
        if (Pcode == null) {
            log("Tops error: No protein specified on command line\n");
            System.exit(1);
        }
        if (Pcode.length() != 4) {
            log("Tops error: Protein code %s must be exactly 4 characters\n",
                    Pcode);
            System.exit(1);
        }

        /* check runtime options are reasonable */
        try {
            options.checkOptions();
        } catch (Exception e) {
            log("ERROR: checking runtime options\n");
            System.exit(1);
        }

        if (options.isVerbose()) {
            System.out.println("Starting TOPS\n");
            System.out.println(
                    "Copyright � 1996 by European Bioinformatics Institute, Cambridge, UK.\n\n");
            options.PrintRunParams(System.out);
        }

        /* set global variables which are derived from inputs */
        options.setGridUnitSize();

        /* call main driver */
        ErrorStatus = runTops(Pcode, options);
        if (ErrorStatus > 0) {
            log("Error detected by RunTops, status %d\n", ErrorStatus);
            System.exit(1);
        } else {
            if (options.isVerbose())
                System.out.println("TOPS completed successfully\n");
        }
    }

    public int runTops(String Pcode, Options options) throws IOException {

        String Comment;
        int SpecifiedDomains = 0;
        PlotFragInformation PlotFragInf;
        char[] psfile = new char[80];
        String dbf = null;
        String fn;
        int npages, NLastPage, nplot;

        Protein protein = null;
        Protein.DomDefError ddep = null;

        int Error = 0;

        /* Check that a file has been specified */
        if (Pcode == null) {
            Error = 1;
            return Error;
        }

        /* Read secondary structure file ( DSSP or STRIDE ) or old save file */
        

        if (options.getFileType().equals("dssp")) {

            if (options.isVerbose())
                System.out.println("Reading dssp file\n");

            String dsspFile = new File(options.getDsspFilePath(), getDSSPFileName(Pcode)).getAbsolutePath();
            protein = new DsspReader().readDsspFile(dsspFile);
            if (protein == null || Error > 0) {
                log("Tops error: processing DSSP information, code %d\n",
                        Error);
                Error = 7;
                return Error;
            }
        } else if (options.getFileType().equals("stride")) { // TODO
//
//            if (options.isVerbose())
//                System.out.println("Reading pdb file\n");
//
//            protein = ReadPDBFile(new File(PDBFilePath, GetPDBFileName(Pcode)));
//            if (protein != null) {
//                log("Tops error: reading pdbfile %s\n", GetPDBFileName(Pcode));
//                Error = 9;
//                return Error;
//            }
//            RemoveACEResidues(protein);
//
//            if (options.isVerbose())
//                System.out.println("Reading stride file\n");
//            if (!ReadSTRIDEFile(
//                    new File(STRIDEFilePath, GetSTRIDEFileName(Pcode)),
//                    protein)) {
//                log("Tops error: getting secondary structure from %s\n",
//                        GetSTRIDEFileName(Pcode));
//                Error = 10;
//                return Error;
//            }
//
//            protein.BridgePartFromHBonds();

        } else if (options.getFileType().equals("tops")) {

            log("Tops error: this bit not implemented yet\n");
            Error = 99;
            return Error;

        } else {

            log("Tops error: Unrecognized file type %s\n", options.getFileType());
            Error = 5;
            return Error;

        }

        /* Assign protein name and code from input file name */
        protein.setName(Pcode);

        /* Initialise the chirality code at this point */
        // InitialiseChirality( protein, Error );

        /*
         * Read the domain boundary file if ChainToPlot was not specified as ALL
         */
        if (options.getChainToPlot() != null && !options.getChainToPlot().equals("ALL")) {
            if (options.isVerbose())
                System.out.println("Reading domain boundary file\n");

            /*
             * first check if specified file exists, otherwise look for one in
             * TOPS_HOME
             */
            if (options.getDomBoundaryFile() != null
                    && new File(options.getDomBoundaryFile()).canRead()) {
                dbf = options.getDomBoundaryFile();
            } else if (TOPS_HOME != null) {
                dbf = TOPS_HOME + "/" + "DomainFile";

            }

            if (dbf != null) {
                new DomainBoundaryFileReader().readDomBoundaryFile(dbf, protein);
            } else {
                log("TOPS warning: no domain file was found\n");
            }
        }
        /* add default domains */
        if (options.isVerbose())
            System.out.println("Setting default domains\n");
        protein.DefaultDomains(options.getChainToPlot().charAt(0));
        if (Error > 0) {
            log("Tops error: detected in DefaultDomains, code %d\n", Error);
            Error = 8;
            return Error;
        }

        /* check domain definitions */
        ddep = protein.checkDomainDefs();
        if (ddep != null) {
            if (ddep.ErrorType != null) {
                log("Tops warning: problem with domain definitions type %d\n", ddep.ErrorType);
                log("%s\n", ddep.ErrorString);
                /* Error = 18; */
                /* return; */
            }
        }

        /* Build the linked list (main TOPS internal data structure ) */
//        Root = protein; // XXX was TopsLinkedList which has become the dssp reader
        if (Root == null) {
            log("Tops error: Building linked list from protein\n");
            Error = 2;
            return Error;
        }

        /*
         * information on plotted chain fragments is held in PlotFragInf for use
         * in output postscript
         */
        PlotFragInf = new PlotFragInformation();

        /* set up the domain breaks and PlotFragInfo */
        if (options.isVerbose())
            System.out.println("Setting domain breaks and domains to plot\n");
        protein.setDomBreaks(Root, PlotFragInf);

        List<Integer> DomainsToPlot = protein.FixDomainsToPlot(options.getChainToPlot().charAt(0), options.getDomainToPlot());
        if (DomainsToPlot.isEmpty()) {
            Error = 14;
            log("Tops error: fixing domains to plot\n");
            return Error;
        }

        /* Loop over domains to plot */
        List<Cartoon> cartoons = new ArrayList<Cartoon>();
        for (int domainToPlot : DomainsToPlot) {

            if (options.isVerbose()) {
                log("\nPlotting domain %d\n", domainToPlot + 1);
            }

            /* Set the domain to plot */
            Cartoon cartoon = protein.SetDomain(Root, protein.getDomain(domainToPlot));

            new Optimise().optimise(cartoon);
            cartoon.calculateConnections(options.getRadius());
            cartoons.add(cartoon);
        }

        /* temporary write out of TOPS file */
        if (options.isVerbose()) {
            System.out.println("\nWriting tops file\n");
        }
        if (options.getTopsFilename() != null) {
            fn = options.getTopsFilename();
        } else {
            fn = getTOPSFileName(Pcode, options.getChainToPlot(), options.getDomainToPlot());
        }
        
        writeTOPSFile(fn, cartoons, Pcode, protein, DomainsToPlot);

        /* Create postscript files for each Cartoon */
        makePostscript(cartoons, protein, DomainsToPlot.size(), PlotFragInf, options);

        if (options.isVerbose())
            System.out.println("\n");

        if (Error > 0) {
            log("Tops error: terminating the chirality calculation\n");
            Error = 17;
            return Error;
        }
        return 0;

    }
    
    private void makePostscript(
            List<Cartoon> cartoons, 
            Protein protein, 
            int NDomainsToPlot, 
            PlotFragInformation PlotFragInf,
            Options options) {
        String Postscript = options.getPostscript();
        if (Postscript != null) {

            if (options.isVerbose()) System.out.println("Writing postscript file\n");

            int npages = NDomainsToPlot / DOMS_PER_PAGE;
            int NLastPage = NDomainsToPlot % DOMS_PER_PAGE;
            npages += (NLastPage > 0 ? 1 : 0);  // add an extra page if necessary

            for (int i = 0; i < npages; i++) {
                File psfile = getPSFile(Postscript, i);
                int nplot = DOMS_PER_PAGE;
                if ((i == (npages - 1)) && NLastPage > 0)
                    nplot = NLastPage;
//                PrintCartoons(
//                   nplot, cartoons, DOMS_PER_PAGE * i, psfile, protein.getProteinCode(), PlotFragInf);
            }
        }
    }
    
    private File getPSFile(String filePrefix, int i) {
        String filename = filePrefix.substring(0, filePrefix.indexOf('.'));
        return new File(filename + "_" + i + ".ps");
    }

    private String getTOPSFileName(String pcode, String chainToPlot, int domainToPlot) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getSTRIDEFileName(String pcode) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getPDBFileName(String pcode) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getDSSPFileName(String pcode) {
        // TODO Auto-generated method stub
        return null;
    }

    private void getPSFILE(char[] PostScript, char[] psfile, int i) {

        int dum;

        int j;
        for (j = 0; j < PostScript.length; j++) {
            char c = PostScript[j];
            if (c == '.')
                break;
            psfile[j] = c;
        }

        psfile[j] = '_';
        psfile[j + 2] = '.';
        psfile[j + 3] = 'p';
        psfile[j + 4] = 's';
        // psfile[j+5] = '\0'; // XXX no need to null-terminate strings

    }

    private void writeTOPSFile(String Filename, List<Cartoon> cartoonPtrs,
            String pcode, Protein protein, List<Integer> domainsToPlot) throws FileNotFoundException {

        int i, j;
        PrintStream out = new PrintStream(new FileOutputStream(Filename));
        DomainDefinition dompt;

        writeTOPSHeader(out, pcode, cartoonPtrs.size());

        for (i = 0; i < cartoonPtrs.size(); i++) {
            dompt = protein.getDomain(domainsToPlot.get(i));
            out.print(String.format("DOMAIN_NUMBER %d %s", i, dompt.domainCATHCode));
            for (j = 0; j < dompt.numberOfSegments; j++)
                out.print(
                  String.format(" %d %d %d", 
                          dompt.segmentStartIndex[j], dompt.segmentIndices[0][j], dompt.segmentIndices[1][j])
                );
            out.println();
            // XXX ugly, but due to the difference between a pointer and an array/list
            appendLinkedList(out, cartoonPtrs.get(i).getSSEs().get(0));
            out.println();
            out.println();
        }

        out.close();
    }

    private void writeTOPSHeader(PrintStream out, String pcode, int NDomains) {

        print(out,
                "##\n## TOPS: protein topology information file\n##\n## Protein code %s\n## Number of domains %d\n##\n\n",
                pcode, NDomains);

    }

    private void appendLinkedList(PrintStream out, SSE p) {

        for (; p != null; p = p.To) {
            print(out, "\n");
            p.WriteSecStr(out);
        }

        return;

    }
    
    private void print(PrintStream out, String s, Object... args) {
        out.print(String.format(s, args));
    }
}
