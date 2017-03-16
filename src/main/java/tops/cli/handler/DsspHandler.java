package tops.cli.handler;

import java.io.File;
import java.io.IOException;

import tops.cli.InputHandler;
import tops.port.model.DsspReader;
import tops.port.model.Protein;

public class DsspHandler implements InputHandler<Protein> {
    
    private File dsspFile;
    
    public DsspHandler(File file) {
        this.dsspFile = file;
    }

    @Override
    public Protein get() {
        try {
            return new DsspReader().readDsspFile(dsspFile.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;    // XXX?
        }
    }

}
