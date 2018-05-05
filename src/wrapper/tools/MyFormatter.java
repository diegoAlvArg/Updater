package wrapper.tools;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Usuario
 */
public class MyFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        String cabera = record.getSourceClassName();
        if (record.getSourceMethodName() != null) {
            cabera += "." + record.getSourceMethodName();//cabera.substring(0, cabera.length()-1);
        }

        return "\n" + new Date(record.getMillis()) + " --- Thread: " + record.getThreadID() + "   "
                + cabera + "\n" + record.getLevel() + ": " + record.getMessage();
    }

}
