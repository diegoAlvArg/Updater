/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import Tools.logger.LogGeneral;
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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Tools.lenguaje.ResourceLeng;
import Tools.logger.LogSincronizacion;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.Optional;
import javafx.application.HostServices;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.util.Duration;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.logging.LogRecord;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Diego Alvarez
 */
public class HelloWorld extends Application {

    public static Properties internalInformation = new Properties();
    public static final int APPLICATION_VERSION = 11;

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
    private static boolean makingUpdate = false;

    @Override
    public void start(Stage primaryStage) throws IOException {
        LogRecord logRegistro;
        try {
            rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", Locale.getDefault());
        } catch (MissingResourceException e) {
            rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", Locale.ENGLISH);
            logRegistro = new LogRecord(Level.INFO, String.format(rb.getString(ResourceLeng.TRACE_LANGUAGUE_FAULT), Locale.getDefault()));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
        }
        System.out.println(ResourceLeng.APP_INIT);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
        Scene scene;
        actualizarVersion(false);
//        if (!makingUpdate) {
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

        logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_INIT_LOAD_XML));
        logRegistro.setSourceClassName(this.getClass().getName());
//            LogGeneral.log(logRegistro);
        TabPane root = null;
        try {
            //application.InterfaceController
            root = (TabPane) FXMLLoader.load(getClass().getResource("/Resources/fxml/interface.fxml"), rb);
        } catch (Exception e) {
            // Esta excepcion no deberia ser necesaria, pero podria ocurrir tras
            //  mala manipulacion del controlador
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.SEVERE, "\n" + errors.toString());
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
        }
        scene = new Scene(root);//, 400, 400);
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
            Platform.exit();
//            System.exit(0);
        });
        stage = primaryStage;
//        }
    }

    public static void changeTitle(String _title) {
        stage.setTitle(_title + internalInformation.get("Version"));
    }

    /**
     * @deprecated @param _newStage
     */
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
//        makingUpdate = true;
        // Restart XR3Player
//        new Thread(() -> {
        String path = InfoTool.getBasePathForClass(HelloWorld.class);
        String[] applicationPath = {new File(path + appName + ".jar").getAbsolutePath()};
        //Show message that application is restarting
        try {
//                System.out.println(appName + " Path is : " + applicationPath[0]);

            //Create a process builder
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", applicationPath[0], String.valueOf(version));
            System.out.println("CMD: " + builder.command());
            LogRecord logRegistro = new LogRecord(Level.INFO, "CMD: " + builder.command());
            LogGeneral.log(logRegistro);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Continuously Read Output to check if the main application started
            String line;
//                System.out.println("CHECKING is alive");
            String mark_OK = ResourceLeng.APP_UPDATER_INIT;
            String mark_ERROR = ResourceLeng.ERROR;
            while (process.isAlive()) {
                while ((line = bufferedReader.readLine()) != null) {
//                        System.out.println("LINE: " + line);
                    if (line.isEmpty()) {
                        break;
                    } else if (line.contains(mark_OK)) {
                        //This line is being printed when XR3Player Starts 
                        //So the AutoUpdater knows that it must exit
//  >>>>                          deleteFolder(path);
                        LogSincronizacion.cerrar();
                        LogGeneral.cerrar();
                        System.exit(0);
                    } else if (line.contains(mark_ERROR)) {
                        //Some kind of problem
                        throw new InterruptedException();
                    }
                }
            }

        } catch (IOException | InterruptedException ex) {
//                Logger.getLogger(HelloWorld.class.getName()).log(Level.INFO, null, ex);
            System.err.println("Error updating");
            // Show failed message
//                Platform.runLater(() -> Platform.runLater(() -> ActionTool.showNotification("Starting " + appName + " failed",
//                        "\nApplication Path: [ " + applicationPath[0] + " ]\n\tTry to do it manually...", Duration.seconds(10), NotificationType.ERROR)));
//                Platform.runLater(() -> ActionTool.showNotification(String.format(rb.getString(ResourceLeng.MESSAGE_TITLE_UPDATE_FAIL), appName),
//                        String.format(rb.getString(ResourceLeng.MESSAGE_TEXT_UPDATE_FAIL), applicationPath[0])
//                        , Duration.seconds(10), NotificationType.ERROR));
            Platform.runLater(() -> ActionTool.customNotificationWithParam(String.format(rb.getString(ResourceLeng.MESSAGE_TITLE_UPDATE_FAIL), appName),
                    String.format(rb.getString(ResourceLeng.MESSAGE_TEXT_UPDATE_FAIL), applicationPath[0]),
                    Duration.seconds(10), NotificationType.ERROR));

        }
//        }, "Start Application Thread").start();
    }

    public static HostServices getHostService() {
        return hostSer;
    }

    public static void actualizarVersion(boolean mostrarMensaje) {
        int currentVersion = (int) internalInformation.get("Version");
        int lastVersion = howIsLastUpdate();

        if (currentVersion < lastVersion) {
            saveData();
            if (Platform.isFxApplicationThread()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION); //Idioma en botones
                alert.setTitle(rb.getString(ResourceLeng.UPDATE_TITLE));
                alert.setHeaderText(String.format(rb.getString(ResourceLeng.UPDATE_HEADER),
                        currentVersion, lastVersion));
                alert.setContentText(rb.getString(ResourceLeng.UPDATE_CONTENT));

                ButtonType buttonYES = new ButtonType(rb.getString(ResourceLeng.ASK_BUTTON_ACCEPT));
                ButtonType buttonNO = new ButtonType(rb.getString(ResourceLeng.ASK_BUTTON_CANCEL));

                alert.getButtonTypes().setAll(buttonYES, buttonNO);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonYES) {
                    restartApplication("XR3PlayerUpdater", lastVersion);
                }
            } else {
                Object[] options = {rb.getString(ResourceLeng.ASK_BUTTON_ACCEPT),
                    rb.getString(ResourceLeng.ASK_BUTTON_CANCEL)};
                int n = JOptionPane.showOptionDialog(new JFrame("DialogDemo"),
                        String.format(rb.getString(ResourceLeng.UPDATE_HEADER),
                                currentVersion, lastVersion),
                        rb.getString(ResourceLeng.UPDATE_TITLE),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, //do not use a custom Icon
                        options, //the titles of buttons
                        options[0]); //default button title
                if (n == JOptionPane.YES_OPTION) {
                    restartApplication("XR3PlayerUpdater", lastVersion);
//                    System.err.println("Updating form SysTray");
                }
            }
        } else if (mostrarMensaje) {
            ActionTool.customNotification(rb, ResourceLeng.UPDATE_INFO,
                    ResourceLeng.UPDATE_INFO_TEXT, Duration.seconds(10), NotificationType.INFORMATION);
//            changeEnable(ResourceLeng.SYS_TRAY_UPDATE, true);
        }
        endUpdate();
    }

    private static Object myControl;
    private static String methodEnd;
    private static String methodSave;
    
    private static void endUpdate() {
        changeEnable(ResourceLeng.SYS_TRAY_UPDATE, true);
        if (myControl != null) {
            try {
                final Method method = myControl.getClass().getMethod(methodEnd);
                method.invoke(myControl);
            } catch (NoSuchMethodException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private static void saveData() {
        
        if (myControl != null) {
            try {
                final Method method = myControl.getClass().getMethod(methodSave);
                method.invoke(myControl);
            } catch (NoSuchMethodException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    

    public static void setControlAction(Object obj, String metEnd, String metSave) {
        myControl = obj;
        methodEnd = metEnd;
        methodSave = metSave;
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
                LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_END_SYSTRAY));
                logRegistro.setSourceClassName(this.getClass().getName());
                LogGeneral.log(logRegistro);
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

//        defaultItem = new MenuItem("Testing");
//        defaultItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                testNotification();
//            }
//        });
//        popup.insert(defaultItem, 0);
//        
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

    /**
     *
     * @param control
     * @param methodName
     * @param textLabel
     *
     * @see
     * https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#getMethod-java.lang.String-java.lang.Class...-
     * @see
     * https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Method.html#invoke-java.lang.Object-java.lang.Object...-
     */
    public static void addOptionPopup(Object control, String methodName, String textLabel) {
        boolean isnew = true;
        MenuItem auxItem;
        // Si es distinto de null es porque el SystemTray esta soportado y por tanto inicializado
        if (trayIcon != null) {

            PopupMenu popup = trayIcon.getPopupMenu();
            MenuItem defaultItem = new MenuItem(rb.getString(textLabel));
            defaultItem.setName(textLabel);
//            System.err.println(defaultItem.getName());
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        LogRecord logRegistro = new LogRecord(Level.INFO, String.format(
                                rb.getString(ResourceLeng.TRACE_USE_SYSTRAY),
                                rb.getString(textLabel), textLabel));
                        logRegistro.setSourceClassName("SystemTray");
                        LogGeneral.log(logRegistro);

                        defaultItem.setEnabled(false);
                        final Method method = control.getClass().getMethod(methodName);
                        method.invoke(control);
                    } catch (NoSuchMethodException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            for (int i = 0; i < trayIcon.getPopupMenu().getItemCount(); i++) {
                auxItem = trayIcon.getPopupMenu().getItem(i);
                if (auxItem.getName().equals(textLabel)) {
                    isnew = false;
                    break;
                }
            }
            if (isnew) {
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

    public static void changeEnable(String option, boolean activo) {
        System.err.println("Desactivando" + option);
//        String aux;
        if (trayIcon != null) {
            MenuItem auxItem;
            for (int i = 0; i < trayIcon.getPopupMenu().getItemCount(); i++) {
                auxItem = trayIcon.getPopupMenu().getItem(i);
//              aux = auxItem.getName();
                if (auxItem.getName().equals(option)) {
                    System.err.println("lo encontre");
                    auxItem.setEnabled(activo);
                    break;
                }
            }
        }

    }

    public static void testNotification() {
        System.out.println("jajajaj");
        ActionTool.customNotification("title", "msj", Duration.seconds(15), NotificationType.INFORMATION);
    }

    public static TrayIcon getSysTray() {
        return trayIcon;
    }

    @Override
    public void stop() {
//        if (!makingUpdate) {
        saveData();
        LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_END_APP));
        logRegistro.setSourceClassName(this.getClass().getName());
        LogGeneral.log(logRegistro);
        LogSincronizacion.cerrar();
        LogGeneral.cerrar();
//        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
