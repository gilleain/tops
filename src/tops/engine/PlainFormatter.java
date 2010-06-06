package tops.engine;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class PlainFormatter extends Formatter {

    private String lineSeparator = (String) java.security.AccessController
            .doPrivileged(new sun.security.action.GetPropertyAction(
                    "line.separator"));

    @Override
    public synchronized String format(LogRecord record) {
        return this.formatMessage(record) + this.lineSeparator;
    }
}
