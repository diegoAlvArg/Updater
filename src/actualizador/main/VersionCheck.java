package actualizador.main;

//#1 Static import
import aplicacion.HelloWorld;
import tools.lenguaje.ResourceLeng;
import tools.logger.LogGeneral;
import actualizador.tools.InfoTool;
//#3 Third party
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
//#4 Java
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * 78
 * @author GOXR3PLUS
 * @version 1.0 This class is fetching data from github to check if the is a new
 * update
 * @see https://github.com/goxr3plus/JavaFXApplicationAutoUpdater
 *
 * @author Diego
 * @version 1.01 Cambios para internalizacion los errores no se tratan aqui,
 * aniadido logger
 */
public class VersionCheck {

    /**
     * This method is fetching data from github to check if the is a new update
     * for XR3Player
     *
     * @return -1 No Conection, -2 Update service not enable
     */
    public static int howIsLastUpdate() {
        System.out.println("Buscando...");
        int lastVersion = -1;

        //Check if we have internet connection
        if (InfoTool.isReachableByPing("www.google.com")) {
            lastVersion = searchForUpdatesPart2();
        }
        
        return lastVersion;
    }

    /**
     * Method that looks for the number of the latest version
     *
     * @return -2 Error. >= 0
     */
    private static int searchForUpdatesPart2() {
        int lastVersionUpdate = -2;
//        System.err.println("Update 2");
        try {

            Document doc = Jsoup.connect("https://raw.githubusercontent.com/diegoAlvArg/Updater/master/HelloWorldUpdatePage.html").get();

            //Document doc = Jsoup.parse(new File("XR3PlayerUpdatePage.html"), "UTF-8", "http://example.com/");
            Element lastArticle = doc.getElementsByTag("article").last();

            lastVersionUpdate = Integer.valueOf(lastArticle.id());
        } catch (IOException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            LogRecord logRegistro = new LogRecord(Level.WARNING, HelloWorld.getResource()
                    .getString(ResourceLeng.TRACE_STORE_SAVE) + "\n" + errors.toString());
            logRegistro.setSourceClassName("searchForUpdatesPart2");
            LogGeneral.log(logRegistro);
        } finally {
            return lastVersionUpdate;
        }
    }

}
