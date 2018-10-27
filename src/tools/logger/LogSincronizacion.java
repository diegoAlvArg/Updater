package tools.logger;

//#4 Java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 60
 * @author Diego Alvarez
 */
public class LogSincronizacion {
    static Logger logger;

    private LogSincronizacion() throws IOException {
        logger = Logger.getLogger(LogSincronizacion.class.getName());
        Path defaultPath = Paths.get("./Log/LogSyncro.log", new String[0]);
        if (!Files.exists(defaultPath.getParent().toAbsolutePath(), new LinkOption[0])) {
            Files.createDirectories(defaultPath.getParent().toAbsolutePath(), new FileAttribute[0]);
        }
        Handler fileHandler = new FileHandler(defaultPath.toAbsolutePath().toString(), true);
        
        //instance formatter, set formatting, and handler
        fileHandler.setFormatter(new Formateador());
        logger.addHandler(fileHandler);
    }

    private static Logger getLogger() {
        if (logger == null) {
            try {
                new LogSincronizacion();
            } catch (IOException e) {
//                e.printStackTrace();
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
