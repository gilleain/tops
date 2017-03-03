package tops.data.db.update;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import tops.cli.translation.RunDssp;
import tops.cli.translation.RunTops;
import tops.translation.CompressedFileHandler;
import tops.translation.Tops2String;

/*
 * Provides functions for downloading classification files, and converting lists of new proteins to new TOPS files
 */

public class ClassificationConverter {
    
    private ClassificationParser classificationParser;

    public ClassificationConverter(ClassificationParser classificationParser) {
        this.classificationParser = classificationParser;
    }
    
    public Map<String, String> parseDomainList(List<String> lines) {
        return classificationParser.parseDomainList(lines);
    }

    public void log(String message) {
        System.err.println(message);
    }

    public void log(Exception e) {
        System.err.println(e.toString());
    }

    public String downloadFileToScratchDirectory(String scratchDirectory,
            String urlString, String desiredFilename) throws FatalException {
        // construct a File to save the data to
        File file;
        try {
            file = new File(scratchDirectory, desiredFilename);
        } catch (NullPointerException npe) {
            this.log(npe);
            throw new FatalException(
                    "Provide a path to a scratch directory to save downloaded files to!");
        }
        this.log("Downloading url : " + urlString + " to file : " + file);

        // quick check to see if the file already exists!
        if (file.exists()) {
            this.log("...file already exists!");
            return file.toString();
        }

        // try making the urlString into an URL object
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException m) {
            this.log(m);
            throw new FatalException(
                    "The url : "
                            + urlString
                            + " is malformed somehow. Change it in the build.properties file");
        }
        this.log("Trying to connect to url : " + url);

        // open a stream from that URL to read from
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(url.openConnection()
                    .getInputStream()));
        } catch (IOException ioe) {
            this.log(ioe);
            throw new FatalException("Failed to connect to : " + urlString
                    + ". Check the logfile");
        }

        // now, write out the data streamed from the URL
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            String line;
            while ((line = in.readLine()) != null) {
                out.write(line);
                out.newLine();
            }
        } catch (IOException ioe) {
            throw new FatalException("IO problem with the file: "
                    + file.toString() + ". Check the logfile");
        } finally {
            try {
                in.close();
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                this.log(ioe);
            }
        }
        this.log("File saved");

        return file.toString();
    }

    public List<String> readListFile(String filepath) throws FatalException {
        // make a list of lines
        List<String> lines = new ArrayList<String>();
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(filepath));
            String line;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException ioe) {
            this.log(ioe);
            throw new FatalException("IO problem with the file at path : "
                    + filepath);
        } finally {
        	try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        return lines;
    }

    public List<String> getExistingChainNames(String topsDirectoryName)
            throws FatalException {
        this.log("Getting existing chain names from tops file directory "
                + topsDirectoryName);
        File topsDirectory = new File(topsDirectoryName);
        if (topsDirectory.isDirectory()) {
            // anonymous class to filter out only ".tops" files
            String[] fileList = topsDirectory.list(new FilenameFilter() {

                public boolean accept(File f, String s) {
                    return s.endsWith(".tops");
                }
            });
            // cut off the ".tops" from the names, to leave only the chain ids
            List<String> existingChainNames = new ArrayList<String>();
            for (int i = 0; i < fileList.length; i++) {
                existingChainNames.add(fileList[i].substring(0, 5));
            }
            this.log("Directory " + topsDirectory + " contains "
                    + existingChainNames.size() + " topsfiles");
            return existingChainNames;
        } else {
            throw new FatalException("The TOPSfile directory "
                    + topsDirectoryName + " is not a directory");
        }
    }

    // get chains from a map of domain-classifications
    public List<String> getChainsFromDomains(Map<String, String> domainClassificationMap) {
        this.log("Converting the domain names to chains");
        List<String> chains = new ArrayList<String>();
        int domainCount = 0;
        for (String domainName : domainClassificationMap.keySet()) {
            try {
                String chainName = domainName.substring(0, 5);
                if (chains.contains(chainName)) {
                    continue;
                } else {
                    chains.add(chainName);
                }
                domainCount++;
            } catch (StringIndexOutOfBoundsException e) {
                this.log("Problem with domain name : " + domainName);
            }
        }
        this.log("Got " + chains.size() + " chains from " + domainCount + " domains");
        return chains;
    }

    public List<String> getPDBIDsFromChains(List<String> chainNames) {
        this.log("Converting chain names to pdbids");
        List<String> pdbIDs = new ArrayList<String>();
        for (String chainName : chainNames) {
            String pdbID = chainName.substring(0, 4);
            if (pdbIDs.contains(pdbID)) {
                continue;
            } else {
                pdbIDs.add(pdbID);
            }
        }
        this.log("Got " + pdbIDs.size() + " PDB IDs from " + chainNames.size()
                + " chains");
        return pdbIDs;
    }

    // filter the list of domains by removing those we already have tops files
    // for.
    public List<String> determineNewChains(List<String> chainNames, List<String> existingChainNames) {
        this.log("Checking " + chainNames.size()
                + " chain names against existing chain names");
        List<String> newChainNames = new ArrayList<String>();
        for (String chainName : chainNames) {
            if (existingChainNames.contains(chainName)) {
                continue;
            } else {
                newChainNames.add(chainName);
            }
        }
        this.log("Determined that there are " + newChainNames.size() + " new chains");
        return newChainNames;
    }

    public List<String> getPDBFilesToDownload(List<String> newPDBIDList,
            String pdbFileDirectory, boolean pdbDirectoryHasStructure) {
        this.log("Checking PDB files in directory " + pdbFileDirectory);
        ArrayList<String> missingPDBFiles = new ArrayList<String>();
        for (int i = 0; i < newPDBIDList.size(); i++) {
            String pdbID = (String) newPDBIDList.get(i);
            String chainDirectory = pdbID.substring(1, 3);
            if (pdbDirectoryHasStructure) {
                File pdbSubDirectory = new File(pdbFileDirectory,
                        chainDirectory);
                if (pdbSubDirectory.exists()) {
                    File pdbFile = new File(pdbSubDirectory, "pdb" + pdbID
                            + ".gz");
                    if (pdbFile.exists()) {
                        continue;
                    } else {
                        File upperCasePDBFile = new File(pdbSubDirectory, "PDB"
                                + pdbID.toUpperCase() + ".gz");
                        if (upperCasePDBFile.exists()) {
                            continue;
                        } else {
                            missingPDBFiles.add(pdbID);
                        }
                    }
                    // if the subdirectory doesn't even exist, then we can
                    // probably assume that
                    // the structure doesn't either (unless the HasStructure
                    // flag is wrongly set...)
                } else {
                    missingPDBFiles.add(pdbID);
                }
            } else {
                File pdbFile = new File(pdbFileDirectory, "pdb" + pdbID
                        + ".ent");
                if (pdbFile.exists()) {
                    continue;
                } else {
                    missingPDBFiles.add(pdbID);
                }
            }
        }
        this.log("There are " + missingPDBFiles.size()
                + " missing PDB files in directory " + pdbFileDirectory);
        return missingPDBFiles;
    }

    // get PDB files from the RCSB on the web
    public void downloadMissingPDBFiles(List<String> missingPDBIDs, String pdbURL,
            String scratchDirectory) {
        this.log("Downloading missing pdb files");
        String pdbDownloadPath = this.subDirectory(scratchDirectory, "pdb_downloads");

        int successfulDownloads = 0;
        for (int i = 0; i < missingPDBIDs.size(); i++) {
            String pdbID = (String) missingPDBIDs.get(i);
            String pdbFileURL = pdbURL + pdbID;
            try {
                String filename = pdbID + ".gz";
                this.downloadFileToScratchDirectory(pdbDownloadPath,
                        pdbFileURL, filename);
                successfulDownloads++;
            } catch (FatalException e) {
                // okay, so this is not so fatal...
                this.log(e.toString());
            }
        }
        this.log("Out of " + missingPDBIDs.size() + ", " + successfulDownloads
                + " were successfully downloaded");
    }

    public String subDirectory(String parent, String child) {
        File subDirectory = new File(parent, child);
        if (!subDirectory.exists()) {
            subDirectory.mkdir();
        }
        return subDirectory.toString();
    }

    // call the translation machinery
    public List<String> convertFiles(List<String> newChainNames,
            List<String> missingPDBIDs, String currentDirectory,
            String domainFilePath, String pdbFileDirectory,
            boolean pdbDirectoryHasStructure, String scratchDirectory,
            String dsspExecutable, String topsExecutable) {
        this.log("Converting PDB files to DSSP and TOPS files, and Strings");

        // make the utility conversion classes
        String pdbDownloadPath = this.subDirectory(scratchDirectory,
                "pdb_downloads");
        String pdbScratchPath = this.subDirectory(scratchDirectory, "pdb");
        String dsspScratchPath = this.subDirectory(scratchDirectory, "dssp");
        String topsScratchPath = this.subDirectory(scratchDirectory, "tops");

        CompressedFileHandler decompresser = new CompressedFileHandler("",
                pdbScratchPath);
        RunDssp dssp = new RunDssp(dsspExecutable, pdbScratchPath,
                dsspScratchPath, currentDirectory);
        RunTops tops = new RunTops(topsExecutable, dsspScratchPath,
                topsScratchPath, currentDirectory);
        Tops2String t2s = new Tops2String(topsScratchPath);

        ArrayList<String> stringList = new ArrayList<String>();
//        boolean isCompressed = false;
        for (int i = 0; i < newChainNames.size(); i++) {
            String chainName = (String) newChainNames.get(i);
            String pdbID = chainName.substring(0, 4);
            String chain = chainName.substring(4, 5);

            // if the chain name is in the missing list, get it directly from
            // the scratch directory
            File file;
            if (missingPDBIDs.contains(pdbID)) {
                file = new File(pdbDownloadPath, pdbID + ".gz");
            } else {
                // is the pdb directory organised into subdirectories ba/ bo/
                // mo/ rf/ etc.
                if (pdbDirectoryHasStructure) {
                    File subDirectory = new File(pdbFileDirectory, pdbID
                            .substring(1, 3));
                    file = new File(subDirectory, "pdb" + pdbID + ".gz");
                    if (!file.exists()) {
                        file = new File(subDirectory, "PDB"
                                + pdbID.toUpperCase() + ".GZ");
                        if (!file.exists()) {
                            this.log("Can't find " + pdbID + " in the pdb directory");
                            continue;
                        }
                    }
                    // or is it just one big mess...
                } else {
                    file = new File(pdbFileDirectory, "pdb" + pdbID + ".ent");
                    if (!file.exists()) {
                        this.log("Can't find " + pdbID + " in the pdb directory");
                        continue;
                    }
                }
            }

            try {
                // decompress the file, wherever it is
                String gzippedFilename = file.toString();
                this.log("Decompressing file " + gzippedFilename);
                String filename = decompresser.gunzip(gzippedFilename);

                // convert with c programs
                dssp.convert(filename, pdbID + ".dssp");
                tops.convert(pdbID, chain, pdbID + ".tops", domainFilePath);

                // finally, make into strings
                String[] topsStrings = t2s.convert(pdbID + ".tops", pdbID,
                        "CATH"); // TODO : fix!
                stringList.addAll(Arrays.asList(topsStrings));
            } catch (IOException ioe) {
                this.log("IOException with " + pdbID);
                this.log(ioe.toString());
            }
        }
        this.log("Converted " + newChainNames.size() + " to " + stringList.size()
                + " strings");
        return stringList;
    }

    public void writeStringFile(String scratchDirectory, List<String> stringList,
            Map<String, String> domainClassificationMap) {
        this.log("Writing strings to " + scratchDirectory);
        try {
            File file = new File(scratchDirectory, "newstrings.str");
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < stringList.size(); i++) {
                String string = (String) stringList.get(i);
                String classification = (String) domainClassificationMap
                        .get(string.substring(0, string.indexOf(" ")));
                out.write(string + " " + classification);
                out.newLine();
            }
            out.close();
        } catch (IOException ioe) {
            this.log("IO Exception");
            return;
        }
        this.log("Done");
    }

}
