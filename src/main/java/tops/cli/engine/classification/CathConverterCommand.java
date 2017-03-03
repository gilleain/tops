package tops.cli.engine.classification;

import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.data.db.update.CATHParser;
import tops.data.db.update.ClassificationConverter;
import tops.data.db.update.FatalException;

public class CathConverterCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {

        String classificationVersion = args[0];
        String listFileURLBase = args[1];
        String scratchDirectory = args[2];

        String topsFileDirectory = args[3];
        String pdbFileDirectory = args[4];
        String pdbURL = args[5];
        String pdbDirectoryHasStructureString = args[6];
        boolean pdbDirectoryHasStructure = (pdbDirectoryHasStructureString
                .equals("true")) ? true : false;

        String dsspExecutable = args[7];
        String topsExecutable = args[8];
        String currentDirectory = args[9];

        ClassificationConverter converter = new ClassificationConverter(new CATHParser());

        try {
            // Step 1 : Download the latest classification file and domain
            // boundary file for CATH.
            String classificationFilename = "CathDomainList.v"
                    + classificationVersion;
            String listFileURL = listFileURLBase + "/v" + classificationVersion
                    + "/" + classificationFilename;
            String listFilePath = converter.downloadFileToScratchDirectory(
                    scratchDirectory, listFileURL, classificationFilename);
            String domainBoundaryFilename = "CathDomall.v"
                    + classificationVersion;
            String domainBoundaryFileURL = listFileURLBase + "/v"
                    + classificationVersion + "/" + domainBoundaryFilename;
            String domainBoundaryFilePath = converter
                    .downloadFileToScratchDirectory(scratchDirectory,
                            domainBoundaryFileURL, domainBoundaryFilename);

            // Step 2 : Parse the file, and store a list of domain names.
            List<String> lines = converter.readListFile(listFilePath);
            Map<String, String> domainClassificationMap = converter.parseDomainList(lines);

            // Step 3 : Convert domain names to chain names and diff with the
            // existing chain files in the tops directory.
            List<String> chains = converter.getChainsFromDomains(domainClassificationMap);
            List<String> existingChainNames = converter.getExistingChainNames(topsFileDirectory);
            List<String> newChainNames = converter.determineNewChains(chains,existingChainNames);

            // Step 4 : Get PDB-IDs and check that we have the PDB files,
            // downloading the missing ones.
            List<String> newPDBIDList = converter.getPDBIDsFromChains(newChainNames);
            List<String> missingPDBIDs = converter.getPDBFilesToDownload(newPDBIDList, pdbFileDirectory, pdbDirectoryHasStructure);
            converter.downloadMissingPDBFiles(missingPDBIDs, pdbURL,
                    scratchDirectory);

            // Step 5 : Convert the PDB files to DSSP and TOPS/String files.
            List<String> stringList = converter.convertFiles(newChainNames,
                    missingPDBIDs, currentDirectory, domainBoundaryFilePath,
                    pdbFileDirectory, pdbDirectoryHasStructure,
                    scratchDirectory, dsspExecutable, topsExecutable);
            converter.writeStringFile(scratchDirectory, stringList,
                    domainClassificationMap);

        } catch (FatalException e) {
            converter.log(e);
        }
  
    }

}
