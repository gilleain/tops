package tops.engine;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class TabFormatter extends Formatter {

    private String lineSeparator = System.getProperty("line.separator");

    @Override
    public synchronized String format(LogRecord record) {
        Object[] args = record.getParameters();
        StringBuffer line = new StringBuffer();
        if (args.length > 1) {
            int lastArg = args.length - 1;
            for (int i = 0; i < lastArg; i++) {
                if (args[i] != null) {
                    line.append(args[i].toString());
                }
                line.append('\t');
            }
            if (args[lastArg] != null) {
                line.append(args[lastArg].toString());
            }
        } else {
            if (args[0] != null) {
                line.append(args[0].toString());
            }
        }
        line.append(this.lineSeparator);
        return line.toString();
    }
}
