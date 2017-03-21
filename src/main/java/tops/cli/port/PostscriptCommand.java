package tops.cli.port;

import java.util.List;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.port.Options;
import tops.port.io.PostscriptFileWriter;
import tops.port.model.Cartoon;
import tops.port.model.PlotFragInformation;
import tops.port.model.Protein;

public class PostscriptCommand implements Command {

    @Override
    public String getDescription() {
        return "Generate postscript file";
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
        Options options = new Options();
        options.parseArguments(args);
        
        List<Cartoon> cartoons = null;
        Protein protein = null;
        PlotFragInformation plotFragInf = null;
        
        PostscriptFileWriter postscriptFileWriter = new PostscriptFileWriter(options);
        postscriptFileWriter.makePostscript(cartoons, protein, plotFragInf);
    }

}
