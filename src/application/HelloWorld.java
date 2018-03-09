/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import static Updater.main.VersionCheck.howIsLastUpdate;
import Updater.tools.ActionTool;
import Updater.tools.InfoTool;
import Updater.tools.NotificationType;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Updater.tools.ResourceLeng;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 *
 * @author Diego Alvarez
 */
public class HelloWorld extends Application {

    public static Properties internalInformation = new Properties();
    public static final int APPLICATION_VERSION = 8;
    static {
        //Important for Web Browser
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        //----------Properties-------------
        internalInformation.put("Version", APPLICATION_VERSION);
        internalInformation.put("ReleasedDate", "29/02/2018");

        System.out.println("Outside of Application Start Method");
    }

    private static ResourceBundle rb;
    private static Stage stage;

    private static int update;

    @Override
    public void start(Stage primaryStage) throws IOException {
        rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", Locale.getDefault());
        Scene scene;
        update = howIsLastUpdate();
        System.out.println("Last " + update);
        if (update > (int) internalInformation.get("Version")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Resources/fxml/askUpdate.fxml"));
            Parent root = (Parent) loader.load();
            AskUpdateController askPanel = (AskUpdateController) loader.getController();
            askPanel.setVersionsAsk(update, (int) internalInformation.get("Version"));

            scene = new Scene(root, 300, 200);
            primaryStage.setTitle("Actualizando");
        } else {
            Parent root = FXMLLoader.load(getClass().getResource("/Resources/fxml/Main.fxml"), rb);
            scene = new Scene(root);//, 400, 400);
//            scene.getStylesheets().add(STYLESHEET_MODENA)
            primaryStage.setTitle(rb.getString(ResourceLeng.APP_TITLE) 
                    + internalInformation.get("Version"));
        }

        primaryStage.setScene(scene);
        primaryStage.show();
        stage = primaryStage;
    }

    public static void changeTitle(String _title) {
        stage.setTitle(_title + internalInformation.get("Version"));
    }

    public static ResourceBundle getResource() {
        return rb;
    }

    //---------------------------------------------------------------------------------------
    /**
     * Calling this method to start the main Application which is XR3Player
     */
    public static void restartApplication(String appName) {
        System.out.println(rb.getString(ResourceLeng.APP_INIT));
        // Restart XR3Player
        new Thread(() -> {
            String path = InfoTool.getBasePathForClass(HelloWorld.class);
            String[] applicationPath = {new File(path + appName + ".jar").getAbsolutePath()};
            //Show message that application is restarting
            try {
                System.out.println(appName + " Path is : " + applicationPath[0]);

                //Create a process builder
                ProcessBuilder builder = new ProcessBuilder("java", "-jar", applicationPath[0], String.valueOf(update));
                System.out.println("CMD: " + builder.command());
                builder.redirectErrorStream(true);
                Process process = builder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                // Continuously Read Output to check if the main application started
                String line;
                System.out.println("CHECKING is alive");
                while (process.isAlive()) {
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println("LINE: " + line);
                        if (line.isEmpty()) {
                            break;
                        } //This line is being printed when XR3Player Starts 
                        //So the AutoUpdater knows that it must exit
                        else if (line.contains("HelloWorld Application Started")) {
//                            deleteFolder(path);
                            System.exit(0);
                        } else if (line.contains("Error: ")) {
                            // Show failed message
                            Platform.runLater(() -> Platform.runLater(() -> ActionTool.showNotification("Starting " + appName + " failed",
                                    "\nApplication Path: [ " + applicationPath[0] + " ]\n\tTry to do it manually...", Duration.seconds(10), NotificationType.ERROR)));
//                            System.exit(0);
                        }
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(HelloWorld.class.getName()).log(Level.INFO, null, ex);

                // Show failed message
                Platform.runLater(() -> Platform.runLater(() -> ActionTool.showNotification("Starting " + appName + " failed",
                        "\nApplication Path: [ " + applicationPath[0] + " ]\n\tTry to do it manually...", Duration.seconds(10), NotificationType.ERROR)));

            }
        }, "Start Application Thread").start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
