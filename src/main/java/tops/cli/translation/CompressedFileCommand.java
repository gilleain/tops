package tops.cli.translation;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.translation.CompressedFileHandler;

public class CompressedFileCommand implements Command {
    
    private Logger log = Logger.getLogger(CompressedFileCommand.class.getName());

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
            log.log(Level.INFO, "Decompressing : {0} to {1}", new Object[] {fileName, decompressedFileName});
        } catch (IOException ioe) {
            log.warning(ioe.toString());
        }
    }

    @Override
    public String getHelp() {
        return "<fileName> <mimeType> <workingDirectory>";
    }

}
