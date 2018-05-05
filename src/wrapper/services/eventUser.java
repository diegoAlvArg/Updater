package wrapper.services;

import Updater.tools.ActionTool;
import Updater.tools.NotificationType;
import Updater.tools.ResourceLeng;
import application.HelloWorld;
import application.InterfaceController;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Usuario
 */
public class eventUser extends Service<Void> {

    private String user = "";
    private String pass1 = "";
    private String pass2 = "";
    private ResourceBundle rb;
    private boolean askPath;
    private InterfaceController iu;

    public void setIu(InterfaceController iu) {
        this.iu = iu;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPass1(String pass) {
        this.pass1 = pass;
    }

    public void setRb(ResourceBundle rb) {
        this.rb = rb;
    }

    public void setAskPath(boolean askPath) {
        this.askPath = askPath;
    }

    public void setPass2(String pass2) {
        this.pass2 = pass2;
    }

    @Override
    protected Task<Void> createTask() {
        final String _user = user;
        final String _pass1 = pass1;
        final String _pass2 = pass2;
        final ResourceBundle _rb = rb;
        final boolean _askPath = askPath;
        final InterfaceController _iu = iu;

        return new Task<Void>() {

            @Override
            protected Void call() {

//                System.err.println("eyyyyyyyyyyyyyyyyyy");
                Platform.runLater(new Runnable() {
                    @Override

                    public void run() {
                        List<String> auxList = askCredentials(_rb, _user, _pass1, _pass2, "", _askPath);
                        auxList = validateUser(auxList, _askPath, _iu);

                        if (auxList != null) {
                            rb = HelloWorld.getResource();
                            Platform.runLater(() -> Platform.runLater(()
                                    -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_DATES_OK),
                                            "",
                                            Duration.seconds(15), NotificationType.INFORMATION)));
                        }
                        _iu.setUserInfo(auxList, _askPath);
                    }
                });
                return null;
            }
        };
    }

    private List<String> askCredentials(ResourceBundle rb, String duser, String dpass1, String dpass2, String dpath, boolean showPath) {
        // Create the custom dialog.
        Dialog<List<String>> dialog = new Dialog<>();
        if (showPath) {
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

        TextField username = new TextField();
        username.setPromptText(rb.getString(ResourceLeng.ASK_FIELD_USER));
        PasswordField password1 = new PasswordField();
        password1.setPromptText(rb.getString(ResourceLeng.ASK_FIELD_PASS));
        PasswordField password2 = new PasswordField();
        password2.setPromptText(rb.getString(ResourceLeng.ASK_FIELD_PASS));
        TextField path = new TextField();

        // Posible inicio tras recuperacion
        username.setText(duser);
        password1.setText(dpass1);
        password2.setText(dpass2);

        // Montado del panel
        grid.add(new Label(rb.getString(ResourceLeng.ASK_LABEL_USER)), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label(rb.getString(ResourceLeng.ASK_LABEL_PASS1)), 0, 1);
        grid.add(password1, 1, 1);
        grid.add(new Label(rb.getString(ResourceLeng.ASK_LABEL_PASS2)), 0, 2);
        grid.add(password2, 1, 2);

        if (showPath) {
            path.setPromptText(rb.getString(ResourceLeng.ASK_FIELD_PATH));
            path.setEditable(false);
            Button tempButton = new Button("...");
            tempButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    File selectedFile = directoryChooser.showDialog(null);
                    if (selectedFile != null) {
                        path.setText(selectedFile.getAbsolutePath());
                    }
                }
            });

            path.setText(dpath);
            grid.add(new Label(rb.getString(ResourceLeng.ASK_LABEL_PATH)), 0, 3);
            grid.add(path, 1, 3);
            grid.add(tempButton, 2, 3);
        }

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            List respuesta = null;
            if (dialogButton == acceptButtonType) {
                respuesta = new ArrayList<>();
                respuesta.add(username.getText());
                respuesta.add(password1.getText());
                respuesta.add(password2.getText());
                respuesta.add(path.getText());
//                return new List<String>(username.getText(), password2.getText());
            }
            return respuesta;
        });

        Optional<List<String>> result = dialog.showAndWait();
        List<String> respuesta = null;
        if (result.isPresent()) {
//            Arrays.asList("", "", "", "");;
            respuesta = result.get();
        }
        return respuesta;
    }

    private List<String> validateUser(List<String> dates, boolean checkPath, InterfaceController iu) {
        List<String> auxList = dates;
        int[] estados;
        boolean askAgain = true;
        ResourceBundle rb = HelloWorld.getResource();
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
        while (auxList != null && askAgain) {
//            iu.showLabelCheck();
            estados = new int[]{0, 0, 0};
            //Comprobacion Moodle
            if (!auxList.get(0).isEmpty() && !auxList.get(1).isEmpty()) {

                estados[0] = validateCredentialsMoodle(auxList.get(0), auxList.get(1));
            }
            //Comprobar NAS-TER
            if (!auxList.get(2).isEmpty()) {
                estados[1] = 3; // por ahora para alante
            }
            //Comprobar permisos lectura
            if (checkPath && !auxList.get(3).isEmpty()) {

                if (validator.checkPermissions(auxList.get(3))) {
                    estados[2] = 1;
                } else {
                    estados[2] = 2;
                }
            }

            // Una vez comprobado todo mostramos mensajes y actuamos en concordancia
            askAgain = false;
            if (estados[0] == 1) {
                askAgain = true;
                Platform.runLater(() -> Platform.runLater(()
                        -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_MOODLE_DOWN),
                                rb.getString(ResourceLeng.MESSAGE_INFO_DOWN_TEXT),
                                Duration.seconds(15), NotificationType.WARNING)));
            } else if (estados[0] == 3) {
                askAgain = true;
            } else if (estados[0] == 2) {
                askAgain = false;
                auxList.set(1, "");
                Platform.runLater(() -> Platform.runLater(()
                        -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_MOODLE_REJECT),
                                rb.getString(ResourceLeng.MESSAGE_INFO_REJECT),
                                Duration.seconds(15), NotificationType.ERROR)));
            }
            if (estados[1] == 1) {
                askAgain &= true;
                Platform.runLater(() -> Platform.runLater(()
                        -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_NASTER_DOWN),
                                rb.getString(ResourceLeng.MESSAGE_INFO_DOWN_TEXT),
                                Duration.seconds(15), NotificationType.WARNING)));
            } else if (estados[1] == 3) {
                askAgain &= true;
            } else if (estados[1] == 2) {
                askAgain &= false;
                if (estados[0] == 2) {
                    //Si la conexion a Moodle y NASTER rechazo credenciales, el
                    //  usuario tiene altas probabilidades de estar mal
                    auxList.set(0, "");
                    auxList.set(2, "");
                } else {
                    //Si solo NASTER rechazo conexion la contraseña de NASTER esta mal
                    auxList.set(2, "");
                }
                Platform.runLater(() -> Platform.runLater(()
                        -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_NASTER_REJECT),
                                rb.getString(ResourceLeng.MESSAGE_INFO_REJECT),
                                Duration.seconds(15), NotificationType.ERROR)));
            }

            if (checkPath && estados[2] == 1) {
                askAgain &= true;
            } else if (checkPath && estados[2] == 2) {
                askAgain &= false;
                auxList.set(3, "");
                Platform.runLater(() -> Platform.runLater(()
                        -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_PATH_REJECT),
                                rb.getString(ResourceLeng.MESSAGE_INFO_PATH_REJECT),
                                Duration.seconds(15), NotificationType.ERROR)));
            }
            if (estados[0] == 0 || estados[1] == 0 || (checkPath && estados[2] == 0)) {
                askAgain &= false;
                Platform.runLater(() -> Platform.runLater(()
                        -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_FIELD_EMPTY),
                                rb.getString(ResourceLeng.MESSAGE_INFO_FIELD_EMPTY),
                                Duration.seconds(15), NotificationType.WARNING)));
            }

//            iu.hideLabelCheck();
            askAgain = !askAgain;
            if (askAgain) {
                auxList = askCredentials(rb, auxList.get(0), auxList.get(1),
                        auxList.get(2), auxList.get(3), checkPath);
            }

        }

        return auxList;
    }

    /**
     *
     * Comprueba que es posible el acceso dado param user pass
     *
     * @param user
     * @param pass
     * @return 1-moodle caido, 2- credenciales erroneas, 3- credenciales Ok
     */
    public static int validateCredentialsMoodle(String user, String pass) {
        int respuesta = 1;
        String title = "";
        try {
            Connection.Response res = Jsoup.connect("https://moodle2.unizar.es/add/login/index.php")
                    .timeout(18 * 1000)
                    .data("username", user, "password", pass)
                    .method(Connection.Method.POST)
                    .execute();
            Document doc = res.parse();
            title = doc.select("head>title").text();
            if (title.contains("ADD Unizar - Moodle 2")) {
                respuesta = 2;
            } else {
                respuesta = 3;
            }
        } catch (IOException ex) {
            // Se rechazo por un TimeOut lo que significa que moodle esta caido
        } finally {
//            System.err.println("\tReturnning " + respuesta);
//            System.err.println("\tTitle " + title);
            return respuesta;
        }
    }

}
