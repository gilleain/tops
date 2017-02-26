package tops.cli.engine.juris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.juris.PipedFinder;

public class PipedFinderCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String name = (args.length < 1) ? "pattern" : args[0];
        
        List<String> instances = new ArrayList<String>();
        BufferedReader buffy = new BufferedReader(new InputStreamReader(System.in));
        String instr;
        
        try {
            while ((instr = buffy.readLine()) != null) {
                instances.add(instr);
            }
        } catch (IOException IOE) {
            System.err.println(IOE.toString());
        }

        String[] inst = (String[]) instances.toArray(new String[0]);
        
        new PipedFinder(name, inst);
    }

}
