package tops.cli.translation;

import java.io.IOException;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.translation.CompressedFileHandler;

public class CompressedFileCommand implements Command {

    @Override
    public String getDescription() {
        return "Decompress a file";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String fileName = args[0];
        String mimeType = args[1];
        String workingDirectory = args[2];

        CompressedFileHandler cfh = 
                new CompressedFileHandler(workingDirectory, workingDirectory);
        try {
            String decompressedFileName = 
                    cfh.attemptDecompressionOfFile(fileName, mimeType);
            System.err.println("Decompressing : " + fileName + " to "
                    + decompressedFileName);
        } catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
