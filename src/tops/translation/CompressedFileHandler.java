package tops.translation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.NoSuchElementException;

import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CompressedFileHandler {

    private String inputDirectory;

    private String outputDirectory;

    private HashMap<String, String> mimeTypes;

    private HashMap<String, String> fileExtensions;

    public static String UNKNOWN = "UNKNOWN";

    public static String GZIP = "GZIP";

    public static String ZIP = "ZIP";

    public CompressedFileHandler(String inputDirectory, String outputDirectory) {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;

        this.mimeTypes = new HashMap<String, String>();
        this.mimeTypes.put("application/x-gzip", CompressedFileHandler.GZIP);
        this.mimeTypes.put("multipart/x-gzip", CompressedFileHandler.GZIP);
        this.mimeTypes.put("multipart/x-zip", CompressedFileHandler.ZIP);
        this.mimeTypes.put("application/zip", CompressedFileHandler.ZIP);

        this.fileExtensions = new HashMap<String, String>();
        this.fileExtensions.put(".gz", CompressedFileHandler.GZIP);
        this.fileExtensions.put(".GZ", CompressedFileHandler.GZIP);
        this.fileExtensions.put(".zip", CompressedFileHandler.ZIP);
        this.fileExtensions.put(".ZIP", CompressedFileHandler.ZIP);
    }

    public String getFileExtension(String fileName) {
        int dotPosition = fileName.lastIndexOf(".");
        if (dotPosition == -1) {
            return "";
        } else {
            return fileName.substring(dotPosition);
        }
    }

    // attempts to decompress a file, returning the filename of the decompressed
    // file or the original if it can't decompress
    public String attemptDecompressionOfFile(String fileName, String fileType)
            throws IOException {
        String fileExtension = this.getFileExtension(fileName);
        String determinedFileType = this.determineFileType(fileType, fileExtension);
        if (determinedFileType == CompressedFileHandler.UNKNOWN) {
            return fileName;
        } else {
            return this.decompress(fileName, determinedFileType);
        }
    }

    public String decompress(String fileName, String fileType)
            throws IOException {
        // System.err.println("attempting decompression of file : " +
        // this.workingDirectory + fileName);
        if (fileType.equals(CompressedFileHandler.GZIP)) {
            return this.gunzip(fileName);
        } else if (fileType.equals(CompressedFileHandler.ZIP)) {
            return this.unzip(fileName);
        } else {
            System.err
                    .println("CompressedFileHandler error: cannot decompress fileType : "
                            + fileType + " file name " + fileName);
            return "";
        }
    }

    public String gunzip(String path) throws IOException {
        // determine the file name for the output file
        File file = new File(path);
        String filename = file.getName();
        int dotPosition = filename.lastIndexOf(".");
        String outputFileName;
        if (dotPosition == -1) {
            // if it is a gzip file, but with no extension (unlikely, but
            // possible, surely) then just use the name
            outputFileName = filename + ".pdb";
        } else {
            String prefix = filename.substring(0, dotPosition);
            // if it has just a simple name like "1dot.GZ" (most likely), then
            // use "1dot.pdb"
            int nextDotPosition = prefix.lastIndexOf(".");
            if (nextDotPosition == -1) {
                outputFileName = prefix + ".pdb";
            } else {
                // else, just use the name before the .gz
                outputFileName = prefix;
            }
        }

        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new GZIPInputStream(new FileInputStream(new File(
                        this.inputDirectory, path))));
        this.pipeToFile(bufferedInputStream, new File(this.outputDirectory,
                outputFileName));

        return outputFileName;
    }

    public String unzip(String fileName) throws IOException {
        ZipFile zipFile = new ZipFile(this.inputDirectory + fileName);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        try {
            ZipEntry zipEntry = entries.nextElement();
            // Be mean, disallow multi-entry zipfiles!
            if (entries.hasMoreElements()) {
                throw new IOException("More than one entry in zipfile!");
            }
            String outputFileName = zipEntry.getName();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    zipFile.getInputStream(zipEntry));
            this.pipeToFile(bufferedInputStream, new File(this.outputDirectory,
                    outputFileName));
            return outputFileName;
        } catch (NoSuchElementException nsee) {
            throw new IOException("Zipfile empty!");
        }
    }

    public void pipeToFile(BufferedInputStream bufferedInputStream,
            File outputFile) throws IOException {
        System.err.println("Writing to decompressed file to " + outputFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                new FileOutputStream(outputFile));
        int part;
        while ((part = bufferedInputStream.read()) != -1) {
            bufferedOutputStream.write(part);
        }
        bufferedInputStream.close();
        bufferedOutputStream.close();
    }

    public String determineFileType(String mimeType, String fileExtension) {
        String mimeTypeType = CompressedFileHandler.UNKNOWN;
        if (this.mimeTypes.containsKey(mimeType)) {
            mimeTypeType = (String) this.mimeTypes.get(mimeType);
            if (fileExtension.equals("")) {
                return mimeTypeType;
            }
        }

        String fileExtensionType = CompressedFileHandler.UNKNOWN;
        if (this.fileExtensions.containsKey(fileExtension)) {
            fileExtensionType = (String) this.fileExtensions.get(fileExtension);
        }

        // If neither information is any use, return 'UNKNOWN'
        if (mimeTypeType.equals(CompressedFileHandler.UNKNOWN)
                && fileExtensionType.equals(CompressedFileHandler.UNKNOWN)) {
            return CompressedFileHandler.UNKNOWN;
        } else {
            // If only one has given a type, return that one
            if (mimeTypeType.equals(CompressedFileHandler.UNKNOWN)) {
                return fileExtensionType;
            } else if (fileExtensionType.equals(CompressedFileHandler.UNKNOWN)) {
                return fileExtensionType;
            }
            // If both have given types, check for agreement!
            else {
                if (mimeTypeType.equals(fileExtensionType)) {
                    return mimeTypeType; // doesn't matter which we return,
                                            // since they both agree!
                } else {
                    System.err.println("CompressedFileHandler error: mimeType "
                            + mimeType + " and fileExtension " + fileExtension
                            + " don't agree!");
                    return CompressedFileHandler.UNKNOWN;
                }
            }
        }
    }

    public static void main(String[] args) {
        String fileName = args[0];
        String mimeType = args[1];
        String workingDirectory = args[2];

        CompressedFileHandler cfh = new CompressedFileHandler(workingDirectory,
                workingDirectory);
        try {
            String decompressedFileName = cfh.attemptDecompressionOfFile(
                    fileName, mimeType);
            System.err.println("Decompressing : " + fileName + " to "
                    + decompressedFileName);
        } catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
    }

}
