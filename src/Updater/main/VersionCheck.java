/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Updater.main;

import Updater.tools.ActionTool;
import Updater.tools.InfoTool;
import static Updater.tools.InfoTool.logger;
import Updater.tools.NotificationType;
import application.HelloWorld;
import java.io.IOException;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.util.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Usuario
 */
public class VersionCheck {
      /**
     * This method is fetching data from github to check if the is a new update
     * for XR3Player
     *
     */
    public static int howIsLastUpdate() {
        System.out.println("Buscando...");
        int lastVersion = -1;

        //Check if we have internet connection
        if (InfoTool.isReachableByPing("www.google.com")) {
//            System.out.println("True");
            lastVersion = searchForUpdatesPart2();
        } else {
//            System.out.println("False");
            Platform.runLater(() -> ActionTool.showNotification("Can't Connect",
                    "Can't connect to the update site :\n1) Maybe there is not internet connection\n2)GitHub is down for maintenance", Duration.millis(2500),
                    NotificationType.ERROR));
        }
//        System.out.println("Last " + lastVersion);
        return lastVersion;
    }

    private static int searchForUpdatesPart2() {
        int lastVersionUpdate = 0;
        System.err.println("Update 2");
        try {

             Document doc = Jsoup.connect("https://raw.githubusercontent.com/diegoAlvArg/Updater/master/HelloWorldUpdatePage.html").get();

            //Document doc = Jsoup.parse(new File("XR3PlayerUpdatePage.html"), "UTF-8", "http://example.com/");
            Element lastArticle = doc.getElementsByTag("article").last();

            // Not disturb the user every time the application starts if there is not new update
            int currentVersion = (int) HelloWorld.internalInformation.get("Version");
            int lastVersion = Integer.valueOf(lastArticle.id());
            System.out.println("Mi version: " + HelloWorld.internalInformation.get("Version"));
            System.out.println("Ultima version: " + lastVersion);
            if (currentVersion <= lastVersion) {
                if (lastVersion == currentVersion) {
                    System.out.println("Es la misma version");
                } else {
                    System.out.println("Version inferior");
                }
                lastVersionUpdate = lastVersion;
            }

//            if (showTheWindow || Integer.valueOf(lastArticle.id()) > currentVersion) {
////                download.setDisable(Integer.valueOf(lastArticle.id()) <= currentVersion);
////                automaticUpdate.setDisable(download.isDisable());
//                update = Integer.valueOf(lastArticle.id());
////				show();
//            }

        } catch (IOException ex) {
            Platform.runLater(() -> ActionTool.showNotification("Error", "Trying to fetch update information a problem occured", Duration.millis(2500), NotificationType.ERROR));
            logger.log(Level.WARNING, "", ex);
        }
        
        return lastVersionUpdate;
    }

}
