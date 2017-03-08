package tops.cli.engine.juris;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.juris.Constrainer;

public class ConstrainCommand implements Command {

    @Override
    public String getDescription() {
        return "Create constraints for a set of graphs";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        File file = new File(args[0]);
        try {
            List<String> instances = new ArrayList<String>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String instr;
            while ((instr = bufferedReader.readLine()) != null) {
                instances.add(instr);
            }
            bufferedReader.close();

            String[] inst = (String[]) instances.toArray(new String[0]);
            Constrainer graphConstraints = new Constrainer(inst);
            System.out.println(graphConstraints);
        } catch (IOException IOE) {
            System.err.println(IOE.toString());
        }
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
