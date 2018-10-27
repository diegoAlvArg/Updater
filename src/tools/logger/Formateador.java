package tools.logger;

//#4 Java
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 25
 * @author Diego Alvarez
 */
public class Formateador extends Formatter {

    @Override
    public String format(LogRecord record) {
        String cabezera = record.getSourceClassName();
        if (record.getSourceMethodName() != null) {
            cabezera = cabezera + "." + record.getSourceMethodName();
        }
        return "\n" + new Date(record.getMillis()) + " --- Thread: " + 
                record.getThreadID() + "   " + cabezera + "\n" +
                record.getLevel() + ": " + record.getMessage();
    }
}
