package actualizador.tools;

//#1 Static import
import aplicacion.HelloWorld;
import Tools.lenguaje.ResourceLeng;
//#3 Third party
import org.controlsfx.control.Notifications;
//#4 Java
import java.awt.Desktop;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * A class which has a lot of useful methods. 278
 * 
 * @author GOXR3PLUS
 * @version 1.0
 * @see https://github.com/goxr3plus/JavaFXApplicationAutoUpdater
 * 
 * @author Diego
 * @version 1.1 Aniadido metodos para mostrar mensaje, independientemente del 
 *  estado de la App
 * 
 */
public final class ActionTool {

    /**
     * The logger for this class
     */
    private static final Logger logger = Logger.getLogger(ActionTool.class.getName());

    /**
     * Tries to open that URI on the default browser
     *
     * @param uri
     * @return <b>True</b> if succeeded , <b>False</b> if not
     */
    public static boolean openWebSite(String uri) {

        try {
            //Check if Desktop is supported
            if (!Desktop.isDesktopSupported()) {
                ActionTool.showNotification("Problem Occured", "Can't open default web browser at:\n[" + uri + " ]", Duration.millis(2500), NotificationType.INFORMATION);
                return false;
            }

            ActionTool.showNotification("Opening WebSite", "Opening on default Web Browser :\n" + uri, Duration.millis(1500), NotificationType.INFORMATION);
            Desktop.getDesktop().browse(new URI(uri));
        } catch (IOException | URISyntaxException ex) {
            ActionTool.showNotification("Problem Occured", "Can't open default web browser at:\n[" + uri + " ]", Duration.millis(2500), NotificationType.INFORMATION);
            logger.log(Level.INFO, "", ex);
            return false;
        }
        return true;
    }

    /**
     * Private Constructor.
     */
    private ActionTool() {
    }

    /**
     * Copy a file from source to destination.
     *
     * @param source the source
     * @param destination the destination
     * @return True if succeeded , False if not
     */
    public static boolean copy(InputStream source, String destination) {
        boolean succeess = true;

        System.out.println("Copying ->" + source + "\n\tto ->" + destination);

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "", ex);
            succeess = false;
        }

        return succeess;
    }

    /**
     * Show a notification.
     *
     * @param title The notification title
     * @param text The notification text
     * @param d The duration that notification will be visible
     * @param t The notification type
     */
    public static void showNotification(String title, String text, Duration d, NotificationType t) {

        //Check if it is JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            System.err.println("eiii");
            Platform.runLater(() -> showNotification(title, text, d, t));
            return;
        }

        Notifications notification1 = Notifications.create().title(title).text(text);
        notification1.hideAfter(d);

        switch (t) {
            case CONFIRM:
                notification1.showConfirm();
                break;
            case ERROR:
                notification1.showError();
                break;
            case INFORMATION:
                notification1.showInformation();
                break;
            case SIMPLE:
                notification1.show();
                break;
            case WARNING:
                notification1.showWarning();
                break;
            default:
                break;
        }

    }

    /**
     * Makes a question to the user.
     *
     * @param text the text
     * @return true, if successful
     */
    public static boolean doQuestion(String text, Stage window) {
        boolean[] questionAnswer = {false};

        // Show Alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(window);
        alert.setHeaderText("Question");
        alert.setContentText(text);
        alert.showAndWait().ifPresent(answer -> questionAnswer[0] = (answer == ButtonType.OK));

        return questionAnswer[0];
    }
    
    
    /**
     * Metodo para  mostrar mensaje, sin especificar el lenguaje
     * 
     * @param titulo titulo del mensaje
     * @param texto texto del mensaje
     * @param d duracion del mensaje, solo durante modo escritorio
     * @param t tipo de notificacion
     */
    public static void mostrarNotificacion(String titulo, String texto, Duration d, NotificationType t) {
        ActionTool.mostrarNotificacion(HelloWorld.getResource(), titulo, texto, d, t);
    }
    /**
     * Metodo para mostrar mensaje
     * 
     * @param rb resourceBundle del idioma sobre el que se muestra la notificacion
     * @param titulo titulo del mensaje
     * @param texto texto del mensaje
     * @param d duracion del mensaje, solo durante modo escritorio
     * @param t tipo de notificacion
     */
    public static void mostrarNotificacion(ResourceBundle rb, String titulo, String texto, Duration d, NotificationType t) {
        switch (t) {
            case ERROR:
                notificarError(rb.getString(titulo), rb.getString(texto), d);
                break;
            case INFORMATION:
                notificarInformacion(rb.getString(titulo), rb.getString(texto), d);
                break;
            case WARNING:
                notificarAlerta(rb.getString(titulo), rb.getString(texto), d);
                break;
            default:
                break;
        }
    }
    /**
     * Metodo para mostrar un mensaje, alguno de sus campos ha sido formateado
     * 
     * @param titulo titulo del mensaje
     * @param texto texto del mensaje
     * @param d duracion del mensaje, solo durante modo escritorio
     * @param t tipo de notificacion
     */
    public static void mostrarNotificacionConParam(String titulo, String texto, Duration d, NotificationType t) {
        switch (t) {
            case ERROR:
                notificarError(titulo, texto, d);
                break;
            case INFORMATION:
                notificarInformacion(titulo, texto, d);
                break;
            case WARNING:
                notificarAlerta(titulo, texto, d);
                break;
            default:
                break;
        }
    }
    
    /**
     * Metodo para mostrar un mensaje de Error
     * 
     * @param titulo titulo del mensaje
     * @param texto texto del mensaje
     * @param d duracion del mensaje, solo durante modo escritorio
     */
    private static void notificarError(String titulo, String texto, Duration d) {
////        System.err.println("showing error\n");
        if (Platform.isFxApplicationThread()) {
            Platform.runLater(()
                    -> ActionTool.showNotification(
                            titulo, texto, d, NotificationType.ERROR));
        } else if (SystemTray.isSupported()) {
            TrayIcon trayIcon = HelloWorld.getSysTray();
            trayIcon.displayMessage(titulo, texto, TrayIcon.MessageType.ERROR);
        }
    }
    /**
     * Metodo para mostrar un mensaje de Informacion
     * 
     * @param titulo titulo del mensaje
     * @param texto texto del mensaje
     * @param d duracion del mensaje, solo durante modo escritorio
     */
    private static void notificarInformacion(String titulo, String texto, Duration d) {
        if (Platform.isFxApplicationThread()) {
            Platform.runLater(()
                    -> ActionTool.showNotification(
                            titulo, texto, d, NotificationType.INFORMATION));
        } else if (SystemTray.isSupported()) {
            TrayIcon trayIcon = HelloWorld.getSysTray();
            trayIcon.displayMessage(titulo, texto, TrayIcon.MessageType.INFO);
        }
    }
    /**
     * Metodo para mostrar un mensaje de Alerta
     * 
     * @param titulo texto del mensaje
     * @param texto texto del mensaje
     * @param d duracion del mensaje, solo durante modo escritorio
     */
    private static void notificarAlerta(String titulo, String texto, Duration d) {
        if (Platform.isFxApplicationThread()) {
            System.err.println("\tYes");
            Platform.runLater(()
                    -> ActionTool.showNotification(
                            titulo, texto, d, NotificationType.WARNING));
        } else if (SystemTray.isSupported()) {
            System.err.println("\tNO");
            TrayIcon trayIcon = HelloWorld.getSysTray();
            trayIcon.displayMessage(titulo, texto, TrayIcon.MessageType.WARNING);
        }
    }
}
