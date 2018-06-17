package zzParaBorrar;

//#1 Static import
import aplicacion.HelloWorld;
import Tools.lenguaje.ResourceLeng;
import Tools.logger.LogGeneral;
import actualizador.tools.ActionTool;
import actualizador.tools.InfoTool;
import static actualizador.tools.InfoTool.logger;
import actualizador.tools.NotificationType;
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
//#5 JavaFx
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * 89
 *
 * @author XR3Player
 * @version 1.0 This class is fetching data from github to check if the is a new
 * update
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

//        else {
////            System.out.println("False");
//            Platform.runLater(() -> ActionTool.showNotification("Can't Connect",
//                    "Can't connect to the update site :\n1) Maybe there is not internet connection\n2)GitHub is down for maintenance", Duration.millis(2500),
//                    NotificationType.ERROR));
//        }
////        System.out.println("Last " + lastVersion);
        return lastVersion;
    }

    /**
     * Method that looks for the number of the latest version
     *
     * @return -2 Error. >= 0
     */
    private static int searchForUpdatesPart2() {
        int lastVersionUpdate = -2;
        System.err.println("Update 2");
        try {

            Document doc = Jsoup.connect("Jttps://raw.githubusercontent.com/diegoAlvArg/Updater/master/HelloWorldUpdatePage.html").get();

            //Document doc = Jsoup.parse(new File("XR3PlayerUpdatePage.html"), "UTF-8", "http://example.com/");
            Element lastArticle = doc.getElementsByTag("article").last();

            lastVersionUpdate = Integer.valueOf(lastArticle.id());

//            // Not disturb the user every time the application starts if there is not new update
//            int currentVersion = (int) HelloWorld.internalInformation.get("Version");
//            int lastVersion = Integer.valueOf(lastArticle.id());
////            System.out.println("Mi version: " + HelloWorld.internalInformation.get("Version"));
////            System.out.println("Ultima version: " + lastVersion);
//            if (currentVersion <= lastVersion) {
//                if (lastVersion == currentVersion) {
//                    System.out.println("Es la misma version");
//                } else {
//                    System.out.println("Version inferior");
//                }
//                lastVersionUpdate = lastVersion;
//            }
//            if (showTheWindow || Integer.valueOf(lastArticle.id()) > currentVersion) {
////                download.setDisable(Integer.valueOf(lastArticle.id()) <= currentVersion);
////                automaticUpdate.setDisable(download.isDisable());
//                update = Integer.valueOf(lastArticle.id());
////				show();
//            }
        } catch (IOException ex) {
//            Platform.runLater(() -> ActionTool.showNotification("Error", "Trying to fetch update information a problem occured", Duration.millis(2500), NotificationType.ERROR));
//            logger.log(Level.WARNING, "", ex);
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
