package tops.cli.classification;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.model.classification.CATHLevel;
import tops.model.classification.CathLevelCode;

public class LevelCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String filename = args[0];
        String groupName = args[1];

        try {
        	CATHLevel root = CATHLevel.fromFile(filename, CathLevelCode.R, groupName);
            System.out.println(root);
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }

    @Override
    public String getHelp() {
        return "<filename> <groupName>";
    }
}
