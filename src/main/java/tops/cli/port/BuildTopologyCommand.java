package tops.cli.port;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.port.DomainBoundaryFileReader;
import tops.port.DomainCalculator;
import tops.port.Optimise;
import tops.port.Options;
import tops.port.io.PostscriptFileWriter;
import tops.port.io.TopsFileWriter;
import tops.port.model.Cartoon;
import tops.port.model.DomainDefinition;
import tops.port.model.DsspReader;
import tops.port.model.PlotFragInformation;
import tops.port.model.Protein;
import tops.port.model.SSE;

/**
 * Builds topologies from secondary structure.
 * 
 *
 *   Program:         TOPS
 *   Description: Program to automatically generate protein topology cartoons
 *   Version:         3
 *   Author: T. Flores (original) with further development by D. Westhead (EBI), 
 *   ported to Java by Gilleain Torrance.
 *
 * 
 * @author maclean
 *
 */
public class BuildTopologyCommand implements Command {
    
    private String STANDARD_DEFAULTS_FILE = "tops.def";

    @Override
    public String getDescription() {
        return "Build toplogy from secondary structure";
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String defaultsFile = STANDARD_DEFAULTS_FILE;
        Options options = null;
        try {
            options = readDefaults(defaultsFile);
        } catch (FileNotFoundException e) {
            // XXX - why do we require a defaults file anyway??
            log("Defaults file not found \"%s\"", defaultsFile);
            System.exit(1);
        }
        
        // XXX - have to parse arguments before reading defaults if location of 
        // XXX defaults file is somewhere else!??!
        String proteinCode = options.parseArguments(args);
        verifyCode(proteinCode);
        
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
            options.printRunParams(System.out);
        }
        
        /* call main driver */
        try {
            runTops(proteinCode, options);
        } catch (IOException ioe) {
            log("Error running main method %s", ioe.getMessage());
        }
        if (options.isVerbose()) {
            System.out.println("TOPS completed successfully\n");
        }
        
    }
    
    public void runTops(String proteinCode, Options options) throws IOException {
        Protein protein = null;

        /* Read secondary structure file ( DSSP or STRIDE ) or old save file */
        if (options.getFileType().equals("dssp")) {

            if (options.isVerbose())
                System.out.println("Reading dssp file\n");

            String dsspFile = new File(options.getDsspFilePath(), options.getDSSPFileName(proteinCode)).getAbsolutePath();
            protein = new DsspReader().readDsspFile(dsspFile);
            if (protein == null) {
                log("Tops error: processing DSSP information\n");
                return;
            }
        } else if (options.getFileType().equals("stride")) { // TODO
//
//            if (options.isVerbose())
//                System.out.println("Reading pdb file\n");
//
//            protein = ReadPDBFile(new File(PDBFilePath, GetPDBFileName(Pcode)));
//            if (protein != null) {
//                log("Tops error: reading pdbfile %s\n", GetPDBFileName(Pcode));
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
//            }
//
//            protein.BridgePartFromHBonds();

        } else if (options.getFileType().equals("tops")) {
            log("Tops error: this bit not implemented yet\n");
        } else {
            log("Tops error: Unrecognized file type %s\n", options.getFileType());
        }

        /* Assign protein name and code from input file name */
        protein.setCode(proteinCode); 

        /* Initialise the chirality code at this point */
        // InitialiseChirality( protein, Error ); // XXX TODO

        readDomainBoundaryFile(protein, options);
        
        /* add default domains */
        if (options.isVerbose()) {
            System.out.println("Setting default domains\n");
        }
        DomainCalculator domainCalculator = new DomainCalculator();
        List<DomainDefinition> domains = 
                domainCalculator.defaultDomains(protein, options.getChainToPlot().charAt(0));

        /* check domain definitions */
        DomainCalculator.DomDefError ddep  = domainCalculator.checkDomainDefs(domains, protein);
        if (ddep != null) {
            if (ddep.ErrorType != null) {
                log("Tops warning: problem with domain definitions type %d\n", ddep.ErrorType);
                log("%s\n", ddep.ErrorString);
            }
        }
        
        /*
         * information on plotted chain fragments is held in PlotFragInf for use
         * in output postscript
         */

        /* set up the domain breaks and PlotFragInfo */
        if (options.isVerbose()) {
            System.out.println("Setting domain breaks and domains to plot\n");
        }
        List<SSE> root = null;
        // TODO - Root will be null here!! XXX
        PlotFragInformation plotFragInf = domainCalculator.setDomBreaks(domains, protein, root);    

        List<DomainDefinition> domainsToPlot = 
                domainCalculator.selectDomainsToPlot(
                        domains, options.getChainToPlot().charAt(0), options.getDomainToPlot());
        
        if (domainsToPlot.isEmpty()) {
            log("Tops error: fixing domains to plot\n");
            return;
        }

        /* Loop over domains to plot */
        List<Cartoon> cartoons = new ArrayList<Cartoon>();
        for (DomainDefinition domainToPlot : domainsToPlot) {

            if (options.isVerbose()) {
                log("\nPlotting domain %d\n", domainToPlot.getCode());
            }

            /* Set the domain to plot */
            Cartoon cartoon = domainCalculator.setDomain(root, protein, domainToPlot);

            // TODO - factor this out
            new Optimise().optimise(cartoon);
            
            cartoon.calculateConnections(options.getRadius());
            cartoons.add(cartoon);
        }

        /* temporary write out of TOPS file */
        if (options.isVerbose()) {
            System.out.println("\nWriting tops file\n");
        }
        
        String topsFilename;
        if (options.getTopsFilename() != null) {
            topsFilename = options.getTopsFilename();
        } else {
            topsFilename = options.getTOPSFileName(proteinCode, options.getChainToPlot(), options.getDomainToPlot());
        }
        
        new TopsFileWriter().writeTOPSFile(topsFilename, cartoons, protein, domainsToPlot);

        /* Create postscript files for each Cartoon */
        new PostscriptFileWriter(options).makePostscript(cartoons, protein, plotFragInf);

        if (options.isVerbose()) {
            System.out.println("\n");
        }
    }
    
    private void verifyCode(String proteinCode) {
        if (proteinCode == null) {
            log("Tops error: No protein specified on command line\n");
            System.exit(1);
        }
        if (proteinCode.length() != 4) {
            log("Tops error: Protein code %s must be exactly 4 characters\n", proteinCode);
            System.exit(1);
        }
    }
    
    private Options readDefaults(String defaultsFile) throws FileNotFoundException {
        Options options = new Options();
        File defsFile = new File(defaultsFile);
        BufferedReader reader = new BufferedReader(new FileReader(defsFile));
        try {
            options.readDefaults(reader);
        } catch (IOException ioe) {
            log("Error reading defaults file");
        } finally {
            try {
                reader.close();
            } catch (IOException ioe) {
                log("Error closing defaults file");
            }
        }
        return options;
    }
    
    private void readDomainBoundaryFile(Protein protein, Options options) throws IOException {
        String dbf = null;
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
            } else {
                dbf = "DomainFile";

            }

            if (dbf != null) {
                new DomainBoundaryFileReader().readDomBoundaryFile(dbf, protein);
            } else {
                log("TOPS warning: no domain file was found at %s\n", dbf);
            }
        }
    }
    
    private void log(String string, Object... args) {
        System.err.println(String.format(string, args));
    }

}
