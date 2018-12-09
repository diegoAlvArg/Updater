package aplicacion.eventos;

//#1 Static import
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
import aplicacion.MainClass;
import aplicacion.controlador.TabConfiguracionControlador;
import static aplicacion.eventos.Validador.validarCredencialesMoodle;
import static aplicacion.eventos.Validador.validarCredencialesNaster;
import tools.lenguaje.ResourceLeng;
//#4 Java
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;
import java.util.ResourceBundle;
//#5 JavaFx
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

/**
 * 357
 *
 * @author Diego Alvarez
 */
public class EventosUsuario {

    private List<String> auxResult;
    private boolean preguntarDeNuevo = false;
    private boolean esperaResultado = true;
    private static boolean eligiendoDirectorio = false;//-----------------------------------------
    
    /**
     * Creara un evento que genera un dialogo en el que se preguntara las
     * credenciales del usuario y las validara hasta que sean correctas o se
     * cancele el evento. Este evento cubre tanto la creacion como la edicion
     * del usuario
     *
     * @param usuario NIP del usuario
     * @param contraseniaM contrasenia de Moodle
     * @param contraseniaN contrasenia de Nas-Ter
     * @param usarNas para indicar el uso de Nas-Ter
     * @param rb Indicara el idioma en el momento actual de generar el evento
     * @param preguntarPath para indicar si el campo de path debe ser preguntado
     * @param control
     */
    public EventosUsuario(String usuario, String contraseniaM, String contraseniaN, boolean usarNas, ResourceBundle rb, boolean preguntarPath, TabConfiguracionControlador control) {

        Platform.runLater(() -> {
//            System.err.println(Thread.currentThread().getId());
            do {
                if (preguntarDeNuevo) {
                    auxResult = preguntarCredenciales(rb, auxResult.get(0), auxResult.get(1), auxResult.get(2), auxResult.get(3), preguntarPath, Boolean.parseBoolean(auxResult.get(4)));
                } else {
                    auxResult = preguntarCredenciales(rb, usuario, contraseniaM, contraseniaN, "", preguntarPath, usarNas);
                }

                preguntarDeNuevo = auxResult != null;
                if (auxResult != null) {
                    esperaResultado = true;
                    //Seguimos en un Hilo de JavaFx, cargamos la tarea pesada en 
                    // un hilo aparte
                    new Thread(()
                            -> validarUsuario(auxResult, preguntarPath)//, control, this)
                    ).start();
                    //Sin embargo debemos esperar la resolucion de este, 
                    // la interfaz se "colgara" cuando el usuario realice un 
                    // un evento pero no inmediatamente. Sin embargo como 
                    // tardamos < 3 seg al hilo de la interfaz no genera 
                    //  el "colgado"
                    do {
                        try {
                            Thread.sleep(3);//<5 ms funciona bien
                        } catch (InterruptedException ex) {
                            Logger.getLogger(EventosUsuario.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } while (esperaResultado);
                } 

            } while (preguntarDeNuevo);

            if (auxResult != null) {
                ActionTool.mostrarNotificacion(ResourceLeng.MESSAGE_TITLE_DATES_OK,
                        ResourceLeng.NONE, Duration.seconds(15), NotificationType.INFORMATION);
            }
            control.establecerUsuario(auxResult, preguntarPath);
        });

    }

    /**
     * Metodo que presentara un dialogo sobre los campos que se desean preguntar
     *
     * @param rb Indicara el idioma en el momento actual de generar el evento
     * @param usuario NIP del usuario
     * @param contraseniaM contrasenia de Moodle
     * @param contraseniaN contrasenia de Nas-Ter
     * @param pathLocal path local donde la App funcionara
     * @param preguntarPath para indicar si el campo de path debe ser preguntado
     * @param usarNas para indicar el uso de Nas-Ter
     *
     * @return List<String> con los campos pertinentes
     */
    private List<String> preguntarCredenciales(ResourceBundle rb, String usuario, String contraseniaM, String contraseniaN, String pathLocal, boolean preguntarPath, boolean usarNas) {

        // Create the custom dialog.
        Dialog<List<String>> dialog = new Dialog<>();
        if (preguntarPath) {
            dialog.setTitle(rb.getString(ResourceLeng.ASK_TITLE_NEW_USER));
        } else {
            dialog.setTitle(rb.getString(ResourceLeng.ASK_TITLE_EDIT_USER));
        }
        // Set the button types.
        ButtonType acceptButtonType = new ButtonType(rb.getString(ResourceLeng.ASK_BUTTON_ACCEPT), ButtonBar.ButtonData.OK_DONE);
        // Si lo crea pro defecto lo pone en el idioma por defecto
        ButtonType cancelButtonType = new ButtonType(rb.getString(ResourceLeng.ASK_BUTTON_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(acceptButtonType, cancelButtonType);

        // Create labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 80, 10, 10));

        //------------CAMPOS---------------------------------------------------------------------------------------------------     
        Tooltip tooltip;
        //--Username
        TextField username = new TextField();
        username.setPromptText(rb.getString(ResourceLeng.ASK_FIELD_USER));
        tooltip = new Tooltip(rb.getString(ResourceLeng.ASK_TOOLTIP_USER));
        tooltip.setFont(new Font("System", 13));
        username.setTooltip(tooltip);
        //--Pass Moodle
        PasswordField password1 = new PasswordField();
        password1.setPromptText(rb.getString(ResourceLeng.ASK_FIELD_PASS));
        tooltip = new Tooltip(rb.getString(ResourceLeng.ASK_TOOLTIP_PASS_MOODLE));
        tooltip.setFont(new Font("System", 13));
        password1.setTooltip(tooltip);
        //--Pass Nas-ter
        PasswordField password2 = new PasswordField();
        password2.setPromptText(rb.getString(ResourceLeng.ASK_FIELD_PASS));
        tooltip = new Tooltip(rb.getString(ResourceLeng.ASK_TOOLTIP_PASS_NASTER));
        tooltip.setFont(new Font("System", 13));
        password2.setTooltip(tooltip);
        password2.setDisable(!usarNas);
        //--Use Nas-ter
        CheckBox useNas = new CheckBox();
        tooltip = new Tooltip(rb.getString(ResourceLeng.ASK_TOOLTIP_NASTER));
        tooltip.setFont(new Font("System", 13));
        useNas.setTooltip(tooltip);
        useNas.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                password2.setDisable(!newValue);
            }

        });
        //--Path en el montado
        TextField path = new TextField();
        //--------------------------------------------------------------------------------------------------------------------

        // Posible inicio tras recuperacion
        username.setText(usuario);
        password1.setText(contraseniaM);
        password2.setText(contraseniaN);

        // Montado del panel
        grid.add(new Label(rb.getString(ResourceLeng.ASK_LABEL_USER)), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label(rb.getString(ResourceLeng.ASK_LABEL_PASS1)), 0, 1);
        grid.add(password1, 1, 1);
        grid.add(new Label(rb.getString(ResourceLeng.ASK_LABEL_PASS2)), 0, 2);
        grid.add(password2, 1, 2);
        Label auxLabel = new Label(rb.getString(ResourceLeng.ASK_LABEL_USE_NAS));
        auxLabel.setTooltip(tooltip);
        grid.add(auxLabel, 3, 2);
        grid.add(useNas, 2, 2);
        if (preguntarPath) {
            path.setPromptText(rb.getString(ResourceLeng.ASK_FIELD_PATH));
            path.setEditable(false);
            tooltip = new Tooltip(rb.getString(ResourceLeng.ASK_TOOLTIP_PATH));
            tooltip.setFont(new Font("System", 13));
            path.setTooltip(tooltip);
            Button tempButton = new Button("...");
            tempButton.setTooltip(tooltip);
            tempButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    if(!eligiendoDirectorio){
                        eligiendoDirectorio = true;
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        File selectedFile = directoryChooser.showDialog(null);
                        if (selectedFile != null) {
                            path.setText(selectedFile.getAbsolutePath());
                        } 
                        eligiendoDirectorio = false;
                    }
                }
            });

            path.setText(pathLocal);
            grid.add(new Label(rb.getString(ResourceLeng.ASK_LABEL_PATH)), 0, 3);
            grid.add(path, 1, 3);
            grid.add(tempButton, 2, 3);
        }

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to List when the accept button is clicked.
        dialog.setResultConverter(dialogButton -> {
            List respuesta = null;
            if (dialogButton == acceptButtonType) {
                respuesta = new ArrayList<>();
                respuesta.add(username.getText());
                respuesta.add(password1.getText());
                respuesta.add(password2.getText());
                respuesta.add(path.getText());
                respuesta.add(String.valueOf(useNas.isSelected()));
            }
            return respuesta;
        });

        // Espera hasta que el dialogo devuelva una Lista de String
        Optional<List<String>> result = dialog.showAndWait();
        List<String> respuesta = null;
        if (result.isPresent()) {
            respuesta = result.get();
        }
        return respuesta;
    }
    
    
    private void validarUsuario(List<String> datos, boolean comprobarPath) {
        List<String> auxList = datos;
        int[] estados;
        boolean askAgain = true;
        ResourceBundle rb = MainClass.getResource();
        // Comprobar valores, volver a preguntar hasta que valores bien o null
        // La comprobacion de Moodel / NAS-TER tiene 4 estados
        //      Estado 0: User & Pass == ""                 REPETIMOS
        //      ESTADO 1: Moodle/NAS-TER esta caido         SEGUIMOS PARA ALANTE
        //      ESTADO 2: Las credenciales son erroneas     REPETIMOS
        //      ESTADO 3: Las credenciales son correctas    SEGUIMOS PARA ALANTE 
        // La comprobacion del directorio tiene 3 estados
        //      ESTADO 0: Path == ""                        REPETIMOS
        //      ESTADO 1: Tenemos permiso de E/L            SEGUIMOS PARA ALANTE
        //      ESTADO 2: No tenemos permisos de E/L
        estados = new int[]{0, 0, 0};
        //Comprobacion Moodle
        if (!auxList.get(0).isEmpty() && !auxList.get(1).isEmpty()) {
//            System.err.print("User " + auxList.get(0) + ",Pass " + auxList.get(1));
            estados[0] = validarCredencialesMoodle(auxList.get(0), auxList.get(1));
//            System.err.println(" Data: " + estados[0]);
        }
        //Comprobar NAS-TER
        if (Boolean.parseBoolean(auxList.get(4))) {
            if (!auxList.get(0).isEmpty() && !auxList.get(2).isEmpty()) {
                estados[1] = validarCredencialesNaster(auxList.get(0), auxList.get(2));
            } else {
                estados[1] = 0;
            }
        } else {
            estados[1] = 3; // por ahora para alante
        }

        //Comprobar permisos lectura
        if (comprobarPath && !auxList.get(3).isEmpty()) {

            if (Validador.checkPermissions(auxList.get(3))) {
                estados[2] = 1;
            } else {
                estados[2] = 2;
            }
        }

        // Una vez comprobado todo mostramos mensajes y actuamos en concordancia
        askAgain = false;
        if (estados[0] == 1) {
            askAgain = true;
            Platform.runLater(()
                    -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_MOODLE_DOWN),
                            rb.getString(ResourceLeng.MESSAGE_INFO_DOWN_TEXT),
                            Duration.seconds(15), NotificationType.WARNING));
        } else if (estados[0] == 3) {
            askAgain = true;
        } else if (estados[0] == 2) {
            askAgain = false;
            auxList.set(1, "");
            Platform.runLater(()
                    -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_MOODLE_REJECT),
                            rb.getString(ResourceLeng.MESSAGE_INFO_MOODLE_REJECT),
                            Duration.seconds(15), NotificationType.ERROR));
        }
        if (estados[1] == 1) {
            askAgain &= true;
            Platform.runLater(()
                    -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_NASTER_DOWN),
                            rb.getString(ResourceLeng.MESSAGE_INFO_DOWN_TEXT),
                            Duration.seconds(15), NotificationType.WARNING));
        } else if (estados[1] == 3) {
            askAgain &= true;
        } else if (estados[1] == 2) {
            askAgain &= false;
            if (Boolean.parseBoolean(auxList.get(4)) && estados[0] == 2) {
                //Si la conexion a Moodle y NASTER rechazo credenciales, el
                //  usuario tiene altas probabilidades de estar mal
                auxList.set(0, "");
                auxList.set(2, "");
            } else if (Boolean.parseBoolean(auxList.get(4))) {
                //Si solo NASTER rechazo conexion la contraseña de NASTER esta mal
                auxList.set(2, "");
            }
            Platform.runLater(()
                    -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_NASTER_REJECT),
                            rb.getString(ResourceLeng.MESSAGE_INFO_NASTER_REJECT),
                            Duration.seconds(15), NotificationType.ERROR));
        }

        if (comprobarPath && estados[2] == 1) {
            askAgain &= true;
        } else if (comprobarPath && estados[2] == 2) {
            askAgain &= false;
            auxList.set(3, "");
            Platform.runLater(()
                    -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_PATH_REJECT),
                            rb.getString(ResourceLeng.MESSAGE_INFO_PATH_REJECT),
                            Duration.seconds(15), NotificationType.ERROR));
        }
        if (estados[0] == 0 || estados[1] == 0 || (comprobarPath && estados[2] == 0)) {
            askAgain &= false;
            Platform.runLater(()
                    -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_FIELD_EMPTY),
                            rb.getString(ResourceLeng.MESSAGE_INFO_FIELD_EMPTY),
                            Duration.seconds(15), NotificationType.WARNING));
        }
        askAgain = !askAgain;
        this.preguntarDeNuevo = askAgain;
        this.auxResult = auxList;
        esperaResultado = false;
//            this.procesoFin.release();
    }
}
