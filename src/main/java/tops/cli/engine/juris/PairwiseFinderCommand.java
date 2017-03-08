package tops.cli.engine.juris;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.juris.PairwiseFinder;
import tops.engine.juris.TParser;

public class PairwiseFinderCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
        
        int lowerIndex = -1;
        int upperIndex = -1;
        String instFi;
        if (args.length == 1) {
            instFi = args[0];
        } else {
            lowerIndex = Integer.parseInt(args[0]);
            upperIndex = Integer.parseInt(args[1]);
            instFi = args[2];
        }
        
        FileReader inFile;
        try {
            inFile = new FileReader(instFi);
        } catch (FileNotFoundException f) {
            System.out.println("No such string file " + f.getMessage());
            return;
        }

        BufferedReader bufferedReader = new BufferedReader(inFile);

        // use a map for the strings (more efficient lookup?)
        List<String> names = new ArrayList<String>();
        TParser tp = new TParser();
        String instr;
        Map<String, String> instMap = new HashMap<String, String>();
        
        try {
            while ((instr = bufferedReader.readLine()) != null) {
                tp.load(instr);
                String name = tp.getName();
                names.add(name);
                instMap.put(name, instr); // KEY is the head/domId
            }
        } catch (IOException IOE) {
            System.out.println("Major Error while reading pair file : " + IOE);
        }
        
        new PairwiseFinder(lowerIndex, upperIndex, names, instMap);
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
