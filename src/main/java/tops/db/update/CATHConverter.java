package tops.db.update;

import java.util.ArrayList;
import java.util.HashMap;

public class CATHConverter extends ClassificationConverter {

    public CATHConverter() {
    }

    @Override
    public HashMap<String, String> parseDomainList(ArrayList<String> lines) {
        HashMap<String, String> data = new HashMap<String, String>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.substring(0, 1).equals("#")) {
                continue;
            } else {
                String[] bits = line.split("\\s+");
                String domainID = bits[0];

                // ignore superseded entries
                String resolution = bits[9];
                if (resolution.equals("1000.000")) {
                    continue;
                }

                // concatenate the CATH number
                String CATHNumber = "";
                for (int j = 1; j < 8; j++) {
                    CATHNumber += bits[j];
                    CATHNumber += ".";
                }
                data.put(domainID, CATHNumber);
            }
        }
        return data;
    }

    public static void main(String[] args) {

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

        CATHConverter converter = new CATHConverter();

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
            ArrayList<String> lines = converter.readListFile(listFilePath);
            HashMap<String, String> domainClassificationMap = converter.parseDomainList(lines);

            // Step 3 : Convert domain names to chain names and diff with the
            // existing chain files in the tops directory.
            ArrayList<String> chains = converter.getChainsFromDomains(domainClassificationMap);
            ArrayList<String> existingChainNames = converter.getExistingChainNames(topsFileDirectory);
            ArrayList<String> newChainNames = converter.determineNewChains(chains,existingChainNames);

            // Step 4 : Get PDB-IDs and check that we have the PDB files,
            // downloading the missing ones.
            ArrayList<String> newPDBIDList = converter.getPDBIDsFromChains(newChainNames);
            ArrayList<String> missingPDBIDs = converter.getPDBFilesToDownload(newPDBIDList, pdbFileDirectory, pdbDirectoryHasStructure);
            converter.downloadMissingPDBFiles(missingPDBIDs, pdbURL,
                    scratchDirectory);

            // Step 5 : Convert the PDB files to DSSP and TOPS/String files.
            ArrayList<String> stringList = converter.convertFiles(newChainNames,
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
