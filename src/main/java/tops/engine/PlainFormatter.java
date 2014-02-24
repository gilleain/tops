package tops.engine;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class PlainFormatter extends Formatter {

	private String lineSeparator = System.getProperty("line.separator");

    @Override
    public synchronized String format(LogRecord record) {
        return this.formatMessage(record) + this.lineSeparator;
    }
}
