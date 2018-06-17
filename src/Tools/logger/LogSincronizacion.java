package Tools.logger;

//#4 Java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

/**
 *
 * @author Diego
 */
public class LogSincronizacion {

    static Logger logger;
//    public Handler fileHandler;
//    Formatter plainText;

    private LogSincronizacion() throws IOException {
        //instance the logger
        logger = Logger.getLogger(LogSincronizacion.class.getName());
        Path defaultPath = Paths.get("./Log/LogSyncro.log");
        if (!Files.exists(defaultPath.getParent().toAbsolutePath())) {
            Files.createDirectories(defaultPath.getParent().toAbsolutePath());
        }
        Handler fileHandler = new FileHandler(defaultPath.toAbsolutePath().toString(), true);
        //instance formatter, set formatting, and handler
        fileHandler.setFormatter(new formateador());
        logger.addHandler(fileHandler);

    }

    private static Logger getLogger() {
        if (logger == null) {
            try {
                new LogSincronizacion();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logger;
    }

    public static void log(LogRecord record) {
        getLogger().log(record);
    }

    public static void cerrar() {
        if (logger != null) {
            Handler[] aux = logger.getHandlers();
            for (Handler a : aux) {
                a.close();
            }
            logger = null;
        }
    }
}
