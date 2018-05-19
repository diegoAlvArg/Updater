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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Updater.tools.ResourceLeng;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.Optional;
import javafx.application.HostServices;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.util.Duration;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 *
 * @author Diego Alvarez
 */
public class HelloWorld extends Application {

    public static Properties internalInformation = new Properties();
    public static final int APPLICATION_VERSION = 9;

    static {
        //Important for Web Browser
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        //----------Properties-------------
        internalInformation.put("Version", APPLICATION_VERSION);
        internalInformation.put("ReleasedDate", "29/02/2018");

        System.out.println("Outside of Application Start Method");
    }
    private static HostServices hostSer;
    private static ResourceBundle rb;
    private static Stage stage;
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    private static int update;

    @Override
    public void start(Stage primaryStage) throws IOException {
        rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", Locale.getDefault());
        System.out.println(ResourceLeng.APP_INIT);
        Scene scene;
//        update = howIsLastUpdate();
//        System.out.println("Last " + update);
        actualizarVersion(false);
//        if (update > (int) internalInformation.get("Version")) {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Resources/fxml/askUpdate.fxml"));
//            Parent root = (Parent) loader.load();
//            AskUpdateController askPanel = (AskUpdateController) loader.getController();
//            askPanel.setVersionsAsk(update, (int) internalInformation.get("Version"));
//
//            scene = new Scene(root, 300, 200);
//            primaryStage.setTitle("Actualizando");
//        } else {
//            Parent root = FXMLLoader.load(getClass().getResource("/Resources/fxml/Main.fxml"), rb);
        if (SystemTray.isSupported()) {
            Platform.setImplicitExit(false);
            buildSystemTray(rb);

            primaryStage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    try {
                        if (!oldValue && newValue) {
                            tray.add(trayIcon);
                            stage.hide();
                            System.out.println("added to SystemTray");
                        }
//                        else if (oldValue && !newValue) {
//                            System.err.println("jeje");
//                        }
                    } catch (Exception ex) {
                        // AL volverlo a levantar desde el SystemTry generara este evento
                        System.out.println("unable to add to tray");
                        ex.printStackTrace();
                    }
//                    primaryStage.hide();
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        }
        TabPane root = (TabPane) FXMLLoader.load(getClass().getResource("/Resources/fxml/interface.fxml"), rb);
        scene = new Scene(root);//, 400, 400);
//            scene.getStylesheets().add(STYLESHEET_MODENA)
        primaryStage.setTitle(rb.getString(ResourceLeng.APP_TITLE)
                + internalInformation.get("Version"));
//        }
        hostSer = getHostServices();
//        Platform.setImplicitExit(false);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
//            System.err.println("cerrando esto");
            System.exit(0);
        });
        stage = primaryStage;
    }

    public static void changeTitle(String _title) {
        stage.setTitle(_title + internalInformation.get("Version"));
    }

    public static void changeStage(Stage _newStage) {
        stage = _newStage;
    }

    public static ResourceBundle getResource() {
        return rb;
    }

    public static void setResource(ResourceBundle newrb) {
        rb = newrb;
        if (trayIcon != null) {
            changeLanguageSystemTray();
        }
    }

    //---------------------------------------------------------------------------------------
    /**
     * Calling this method to start the main Application which is XR3Player
     */
    public static void restartApplication(String appName, int version) {
        System.out.println(ResourceLeng.APP_INIT);
        // Restart XR3Player
        new Thread(() -> {
            String path = InfoTool.getBasePathForClass(HelloWorld.class);
            String[] applicationPath = {new File(path + appName + ".jar").getAbsolutePath()};
            //Show message that application is restarting
            try {
                System.out.println(appName + " Path is : " + applicationPath[0]);

                //Create a process builder
                ProcessBuilder builder = new ProcessBuilder("java", "-jar", applicationPath[0], String.valueOf(version));
                System.out.println("CMD: " + builder.command());
                builder.redirectErrorStream(true);
                Process process = builder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                // Continuously Read Output to check if the main application started
                String line;
                System.out.println("CHECKING is alive");
                String mark_OK = ResourceLeng.APP_UPDATER_INIT;
                String mark_ERROR = ResourceLeng.ERROR;
                while (process.isAlive()) {
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println("LINE: " + line);
                        if (line.isEmpty()) {
                            break;
                        } else if (line.contains(mark_OK)) {
                            //This line is being printed when XR3Player Starts 
                            //So the AutoUpdater knows that it must exit
//  >>>>                          deleteFolder(path);
                            System.exit(0);
                        } else if (line.contains(mark_ERROR)) {
                            //Some kind of problem
                            throw new InterruptedException();
                        }
                    }
                }

            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(HelloWorld.class.getName()).log(Level.INFO, null, ex);

                // Show failed message
                Platform.runLater(() -> Platform.runLater(() -> ActionTool.showNotification("Starting " + appName + " failed",
                        "\nApplication Path: [ " + applicationPath[0] + " ]\n\tTry to do it manually...", Duration.seconds(10), NotificationType.ERROR)));

            }
        }, "Start Application Thread").start();
    }

    public static HostServices getHostService() {
        return hostSer;
    }

    public static void actualizarVersion(boolean mostrarMensaje) {
        int currentVersion = (int) internalInformation.get("Version");
        int lastVersion = howIsLastUpdate();

        if (currentVersion < lastVersion) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(rb.getString(ResourceLeng.UPDATE_TITLE));
            alert.setHeaderText(String.format(rb.getString(ResourceLeng.UPDATE_HEADER),
                    currentVersion, lastVersion));
            alert.setContentText(rb.getString(ResourceLeng.UPDATE_CONTENT));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                HelloWorld.restartApplication("XR3PlayerUpdater", lastVersion);
            }
        } else if (mostrarMensaje) {
            if (Platform.isFxApplicationThread()) {
                /*Platform.runLater(() -> /*Platform.runLater(() ->*/
                ActionTool.showNotification(
                        rb.getString(ResourceLeng.UPDATE_INFO),
                        rb.getString(ResourceLeng.UPDATE_INFO_TEXT),
                        Duration.seconds(10), NotificationType.INFORMATION);//)/*)*/;
            } else if (SystemTray.isSupported()) { 
                trayIcon.displayMessage(rb.getString(ResourceLeng.UPDATE_INFO), rb.getString(ResourceLeng.UPDATE_INFO_TEXT), MessageType.INFO);
//                System.err.println("Estamos en SysTray");
            }
            HelloWorld.changeEnable(ResourceLeng.SYS_TRAY_UPDATE, true);
        }
    }

    /**
     * @deprecated
     */
    public static void showApp() {
        stage.show();
    }

    /**
     * @deprecated
     */
    public static void hideApp() {
        stage.hide();
    }

    private void buildSystemTray(ResourceBundle rb) {
//        System.out.println("system tray supported");
        tray = SystemTray.getSystemTray();
        URL imageURL = this.getClass().getResource("/Resources/Icons/logo_moodle.png");
        System.err.println(imageURL);

        Image image = (new ImageIcon(imageURL)).getImage();//Toolkit.getDefaultToolkit().getImage("./logo_moodle.png");
        ActionListener exitListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                System.out.println("Exiting....");
                System.exit(0);
            }
        };
        PopupMenu popup = new PopupMenu();
        MenuItem defaultItem = new MenuItem(rb.getString(ResourceLeng.SYS_TRAY_EXIT));
        defaultItem.setName(ResourceLeng.SYS_TRAY_EXIT);
        defaultItem.addActionListener(exitListener);
        popup.add(defaultItem);

        defaultItem = new MenuItem(rb.getString(ResourceLeng.SYS_TRAY_OPEN));
        defaultItem.setName(ResourceLeng.SYS_TRAY_OPEN);
        defaultItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tray.remove(trayIcon);
                            stage.show();
                            stage.setIconified(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        popup.insert(defaultItem, 0);

        trayIcon = new TrayIcon(image, rb.getString(ResourceLeng.SYS_TRAY_TOOLTIP), popup);
        trayIcon.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tray.remove(trayIcon);
                            stage.show();
                            stage.setIconified(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        trayIcon.setImageAutoSize(true);
    }
    public static void addOptionPopup(Object control, String methodName, String textLabel) {
        boolean isnew = true;
        MenuItem auxItem;
        // Si es distinto de null es porque el SystemTray esta soportado y por tanto inicializado
        if (trayIcon != null) {

            PopupMenu popup = trayIcon.getPopupMenu();
            MenuItem defaultItem = new MenuItem(rb.getString(textLabel));
            defaultItem.setName(textLabel);
            System.err.println(defaultItem.getName());
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        final Method method = control.getClass().getMethod(methodName);
                        method.invoke(control);
                    } catch (NoSuchMethodException ex) {
                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            for (int i = 0; i < trayIcon.getPopupMenu().getItemCount(); i++) {    
                auxItem = trayIcon.getPopupMenu().getItem(i);
                if(auxItem.getName().equals(textLabel)){
                    isnew = false;
                    break;
                }
            }
            if(isnew){
                popup.insert(defaultItem, 0);
            }
        }
    }
    private static void changeLanguageSystemTray() {
        MenuItem auxItem;
        for (int i = 0; i < trayIcon.getPopupMenu().getItemCount(); i++) {
            auxItem = trayIcon.getPopupMenu().getItem(i);
            auxItem.setLabel(rb.getString(auxItem.getName()));
        }
    }
    public static void changeEnable(String option, boolean activo){
        System.err.println("Desactivando" + option);
//        String aux;
        MenuItem auxItem;
        for (int i = 0; i < trayIcon.getPopupMenu().getItemCount(); i++) {
            auxItem = trayIcon.getPopupMenu().getItem(i);
//            aux = auxItem.getName();
            if(auxItem.getName().equals(option)){
                System.err.println("lo encontre");
                auxItem.setEnabled(activo);
                break;
            }
        }
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
