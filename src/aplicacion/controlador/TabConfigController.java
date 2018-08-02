package aplicacion.controlador;

import Tools.almacen.InformacionUsuario;
import Tools.lenguaje.ResourceLeng;
import Tools.logger.LogGeneral;
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.text.TextFlow;
import aplicacion.controlador.MainController;
import aplicacion.eventos.EventosUsuario;
import aplicacion.eventos.ProcesoSyncronizacion;
import aplicacion.eventos.Validador;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 *
 * @author Usuario
 */
public class TabConfigController {// implements Initializable{

    private MainController main;

    @FXML
    private Label LLanguague;
    @FXML
    private ComboBox CLanguague;
    @FXML
    private Spinner hourSpinner;
    @FXML
    private Spinner minutesSpinner;
    @FXML
    private ImageView IUserIcon;
    @FXML
    private Button BNewUser;
    @FXML
    private Button BEditUser;
    @FXML
    private Label LIdUser;
    @FXML
    private Label LPathApplication;
    @FXML
    private Label LHours;
    @FXML
    private Label LMinutes;
    @FXML
    private Button BConfirm;
    @FXML
    private Label LFeqSinc;
    @FXML
    private Button BEditPath;
    @FXML
    private Label LPathDownload;
    @FXML
    private Label LNextUpdate;
    @FXML
    private Button BUpdate;
    @FXML
    private Label LCheckDate;
    @FXML
    private Label LTimeUpdate;

    private Timeline freqSecuence;
    private Calendar momentoSigAct;
    @FXML
    private CheckBox CBNaster;

    //---------------------------------------------------FXML---------------------------------------------------   
    /**
     * Tratara el evento generado por el usuario en relacion a selecionar un
     * idioma para la App
     *
     * @param event
     */
    @FXML
    private void changeLanguague(ActionEvent event) {
        LogRecord logRegistro = null;
        ResourceBundle rb = main.getResource();
        ResourceBundle auxRb;
        String languagueSelected = (String) CLanguague.getValue();
        String auxLanguage;
        Locale auxLocale = null;
        if (languagueSelected != null) {
            for (Map.Entry<String, String> e : ResourceLeng.LANGUAGES.entrySet()) {
                auxLanguage = rb.getString(e.getValue());
                if (auxLanguage.compareTo(languagueSelected) == 0) {
                    auxLocale = new Locale(e.getKey());
                    break;
                }
            }

            if (auxLocale == null) {
                // No deberia saltar
                logRegistro = new LogRecord(Level.WARNING, rb.getString(ResourceLeng.TRACE_LANGUAGE_UNKNOW));
                logRegistro.setSourceClassName(this.getClass().getName());
            } else if (auxLocale.equals(rb.getLocale())) {
                //De normal el comboBox no lazaa un evento al elegir el que ya 
                //  estaba selecionado. Sin embargo cuando cambiamos el idioma y 
                //  hacemos selectValue para el idioma (en su idioma) esto genera
                //  un evento que es esta seccion.
            } else {
                logRegistro = new LogRecord(Level.INFO, String.format(
                        rb.getString(ResourceLeng.TRACE_LANGUAGE_OK), languagueSelected));
                logRegistro.setSourceClassName(this.getClass().getName());

                auxRb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", auxLocale);
//                HelloWorld.cambiarTitulo(auxRb.getString(ResourceLeng.APP_TITLE));
//                HelloWorld.setResource(auxRb);
//                setLanguague(auxRb);
                main.changeLanguague(auxRb);
            }
        }
        if (logRegistro != null) {
            LogGeneral.log(logRegistro);
        }
    }

    /**
     * Tratara el evento generado pro el usuario en relacion a crear un nuevo
     * "perfil" de usuario.
     *
     * @param event
     */
    @FXML
    private void createNewUser(ActionEvent event) {
        if(main.canUseUser()){
            ResourceBundle rb = main.getResource();
            LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_EVENT_USER_NEW));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);

            new EventosUsuario("", "", "", false, rb, true, this);
            if (freqSecuence != null) {
                freqSecuence.pause();
            }
            BNewUser.setDisable(true);
            BEditUser.setDisable(true);
        }
    }
    /**
     * Tratara el evento generado por el usuario en relacion a editar un "
     * "perfil" de usuario. Dicho evento pondra en pausa el Timer para la
     * siguiente actualizacion.
     *
     * @param event
     */
    @FXML
    private void editUser(ActionEvent event) {
        if(main.canUseUser()){
            try {
                ResourceBundle rb = main.getResource();
                LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_EVENT_USER_EDIT));
                logRegistro.setSourceClassName(this.getClass().getName());
                LogGeneral.log(logRegistro);

                new EventosUsuario(InformacionUsuario.getUser(), InformacionUsuario.getPass1(), InformacionUsuario.getPass2(),
                        InformacionUsuario.getUseNas(), rb, false, this);
                if (freqSecuence != null) {
                    freqSecuence.pause();
                }
                BNewUser.setDisable(true);
                BEditUser.setDisable(true);
            } catch (NoSuchFieldException e) {
                wrongDates();
            }
        }
    }

    /**
     * Manejara el evento generado por el usuario para seleccionar un nuevo
     * directorio para que funcione la App, iniciando la navegacion en el path
     * actual. Comprobara que el directorio selecionado se tenga permisos e
     * informando en caso contrario; y finalizara guardando el path resultante
     *
     * @param event
     */
    @FXML
    private void chooseDirectory(ActionEvent event) {
        File selectedFile = null;
        String initialPath;
        try {
            initialPath = InformacionUsuario.getPath();
            do {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File(initialPath));
                selectedFile = directoryChooser.showDialog(null);
                if (Validador.checkPermissions(selectedFile.getAbsolutePath())) {
                    initialPath = selectedFile.getAbsolutePath();
                    break;
                } else {
                    ActionTool.mostrarNotificacion(ResourceLeng.MESSAGE_TITLE_PATH_REJECT,
                            ResourceLeng.MESSAGE_INFO_PATH_REJECT, Duration.seconds(15),
                            NotificationType.ERROR);
                }

            } while (selectedFile != null);
            LPathApplication.setText(initialPath);
            InformacionUsuario.setPath(initialPath);
        } catch (NoSuchFieldException e) {
            wrongDates();
        }

    }

    /**
     * Establece un Timer "alarma" segun los valores de los Spinner y la hora
     * actual a los 00 segundos; para el Timer que aun estubiera establecido.
     *
     *
     */
    @FXML
    protected void setNextUpdate() {
        if (freqSecuence != null) {
            freqSecuence.stop();
            freqSecuence = null;
        }
        int minutos = (int) minutesSpinner.getValue();
        int horas = (int) hourSpinner.getValue();

        momentoSigAct = Calendar.getInstance();
        Calendar momentoActual = (Calendar) momentoSigAct.clone();
        momentoSigAct.add(Calendar.MINUTE, minutos);
        momentoSigAct.add(Calendar.HOUR_OF_DAY, horas); // adds hour
        momentoSigAct.set(Calendar.SECOND, 0);

        ResourceBundle rb = main.getResource();
        setNextUpdateLabel(rb, momentoActual);

        long diff = momentoSigAct.getTime().getTime() - momentoActual.getTime().getTime();
        freqSecuence = new Timeline(new KeyFrame(
                Duration.seconds(diff / 1000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                LogRecord logRegistro = new LogRecord(Level.SEVERE, rb.getString(ResourceLeng.TRACE_TIMER_END));
                logRegistro.setSourceClassName(this.getClass().getName());
                LogGeneral.log(logRegistro);
                syncroStart();
            }
        }));

        freqSecuence.setCycleCount(1);
        freqSecuence.play();
        if(main.inUseUser()){
            freqSecuence.pause();
        }
    }
    /**
     * Metodo que inicia el proceso de sincronizacion, no asociado al Timer;
     * esto implicara para el Timer.
     */
    @FXML
    private void syncroNow() {
        if(!main.inUseUser()){
            freqSecuence.stop();
            syncroStart();
        }
    }

    /**
     * Manejara el evento generado por el usuario para activar/desactivar el uso
     * de Nas-Ter en la sincronizacion. Comprovando que los datos son corectos
     *
     * @param event
     */
    @FXML
    private void useNasTer(ActionEvent event) {
        if (CBNaster.isSelected()) {
            try {
//                int resultado = EventosUsuario.validarCredencialesNaster(InformacionUsuario.getUser(), InformacionUsuario.getPass2());
                int resultado = Validador.validarCredencialesNaster(InformacionUsuario.getUser(), InformacionUsuario.getPass2());

                if (resultado == 2) {
                    CBNaster.setSelected(false);
                    ActionTool.mostrarNotificacion(ResourceLeng.MESSAGE_TITLE_NASTER_REJECT,
                            ResourceLeng.MESSAGE_INFO_NASTER_REJECT, Duration.seconds(15), NotificationType.WARNING);
                } else {
                    // El caso de naster caido resultado == 1 lo trataremos mas adelante en la syncronizacion
                    InformacionUsuario.setUseNas("true");
                }
            } catch (NoSuchFieldException e) {
                CBNaster.setSelected(false);
                wrongDates();
            }
        } else {

            InformacionUsuario.setUseNas("false");
        }
    }

    
    
    //---------------------------------------------------EVENTO-------------------------------------------------  
    /**
     * Metodo que maneja el fin de los eventos edit/new User reactivara/creara
     * el Timer para la siguiente actualizacion.y en caso de que el usuario
     * complete los eventos guardara los datos resultantes.
     *
     * @param dates
     * @param isnew
     */
    public void setUserInfo(List<String> dates, boolean isnew) {
        try {
            if (dates != null && isnew) {
                InformacionUsuario.crearFichero(dates.get(0), dates.get(1), dates.get(2), dates.get(3), String.valueOf(dates.get(4)));
                initializationUserLoad(true, dates.get(0), dates.get(3));
                CBNaster.setSelected(Boolean.parseBoolean(dates.get(4)));
                main.aparecioUsuario();
            } else if (dates != null) {
                InformacionUsuario.crearFichero(dates.get(0), dates.get(1), dates.get(2), InformacionUsuario.getPath(), String.valueOf(dates.get(4)));
                LIdUser.setText(dates.get(0));
                CBNaster.setSelected(Boolean.parseBoolean(dates.get(4)));
            }

            BEditUser.setDisable(false);
            BNewUser.setDisable(false);
            main.liberarUsuario();

            LogRecord logRegistro = new LogRecord(Level.INFO, main.getResource().getString(ResourceLeng.TRACE_EVENT_USER_END));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
        } catch (NoSuchFieldException e) {
            wrongDates();
        }
    }

    
    /**
     * Metodo que iniciara el proceso de sincronizacion. Limpiara el TreeView si
     * es la X vez que lanzamos la sincronizaion.
     *
     * Adenas desactivara las interacciones que puedan lanzar este proceso como
     * los botones de la App o la opcion en el Systray (si lo hubiera)
     */
    private void syncroStart() {
        main.canUseUser();
        main.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_SYNCRO, false);
//        BConfirm.setDisable(true);
//        BUpdate.setDisable(true);
////        BNewUser.setDisable(true);
        BEditPath.setDisable(true);
//        BEditUser.setDisable(true);
        main.loggedSyncro();

        
        LTimeUpdate.setText(main.getResource().getString(ResourceLeng.SYNCRO_NOW));
        try {
            new ProcesoSyncronizacion(InformacionUsuario.getUser(), InformacionUsuario.getPass1(),
                    InformacionUsuario.getPass2(), InformacionUsuario.getPath(),
                    main.getResource(), main, CBNaster.isSelected());
        } catch (NoSuchFieldException e) {
            wrongDates();
        }
    }
    /**
     * Metodo para manejar el fin del evento de sincronizar, reactivando todo lo
     * que este evento hubiera desactivado y estableciendo la sigueinte "alarma"
     */
    public void syncroEnd() {
        setNextUpdate();
//        BConfirm.setDisable(false);
//        BUpdate.setDisable(false);
//        BNewUser.setDisable(false);
        BEditPath.setDisable(false);
//        BEditUser.setDisable(false);
//        BActualizar.setDisable(false);
        main.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_SYNCRO, true);
//        HelloWorld.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_SYNCRO, true);
//        HelloWorld.showApp();
    }

  
    /**
     * Reactivara el Timer "alarma", en caso de que la "alarma" ya debiera haber
     * saltado lanza el proceso asociado a fin del Timer; en caso contrario
     * recalculara el tiempo que le debiera quedar a la "alarma" y la pone en
     * marcha.
     */
    private void resumeUpdate() {
        Calendar momentoActual = Calendar.getInstance();
        if (momentoActual.after(momentoSigAct)) {
            // Paso el momento de la alarma, sincronizamos ya
            LogRecord logRegistro = new LogRecord(Level.SEVERE, main.getResource().getString(ResourceLeng.TRACE_TIMER_LATE));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
            syncroStart();
        } else {
//            System.err.println("ajustando tiempo");
            // Calcular cuanto tiempo estuvimos parados y poner nueva
            freqSecuence.stop();
            long diff = momentoSigAct.getTime().getTime() - momentoActual.getTime().getTime();
            freqSecuence = new Timeline(new KeyFrame(
                    Duration.seconds(diff / 1000), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    LogRecord logRegistro = new LogRecord(Level.SEVERE, main.getResource().getString(ResourceLeng.TRACE_TIMER_END));
                    logRegistro.setSourceClassName(this.getClass().getName());
                    LogGeneral.log(logRegistro);
                    syncroStart();
                }
            }));

            freqSecuence.setCycleCount(1);
            freqSecuence.play();
        }
    }
    /**
     * Cambiara la Label informativa de la App para mostrar cuando sera la
     * sigeuinte actualizacion.
     *
     * @param rb resource del idioma
     * @param now momento actual.
     */
    private void setNextUpdateLabel(ResourceBundle rb, Calendar now) {
        String dayTime;
        if (now.get(Calendar.DAY_OF_MONTH) == momentoSigAct.get(Calendar.DAY_OF_MONTH)) {
            dayTime = rb.getString(ResourceLeng.DAY_TODAY);
        } else {
            dayTime = rb.getString(ResourceLeng.DAY_TOMORROW);
        }
        String line = rb.getString(ResourceLeng.NEXT_TIME_SEED);
        line = String.format(line, dayTime);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        line += sdf.format(momentoSigAct.getTime());
        LTimeUpdate.setText(line);
    }
    
    protected void resumeUserFree(){
        if (freqSecuence != null) {
            resumeUpdate();
            setNextUpdateLabel(main.getResource(), Calendar.getInstance());
        }else if (!LIdUser.getText().isEmpty()){
            setNextUpdate();
        }
        BConfirm.setDisable(false);
        BUpdate.setDisable(false);
    }
    protected void blockSincro(){
        if (freqSecuence != null) {
            freqSecuence.pause();
        }
        BConfirm.setDisable(true);
        BUpdate.setDisable(true);
    }
    
    
    //---------------------------------------------------UTILS-------------------------------------------------- 
    protected void setLanguague(ResourceBundle rb) {
//        this.OptionConfig.setText(rb.getString(ResourceLeng.TAB_CONFIG));
        this.LLanguague.setText(rb.getString(ResourceLeng.LANGUAGE));
        this.CLanguague.getItems().clear();
        for (Map.Entry<String, String> e : ResourceLeng.LANGUAGES.entrySet()) {
            CLanguague.getItems().add(rb.getString(e.getValue()));
        }
        String idioma = ResourceLeng.LANGUAGES.get(rb.getLocale().getLanguage());
        CLanguague.setValue(rb.getString(idioma));
        LHours.setText(rb.getString(ResourceLeng.TIME_HOUR_TEXT));
        LMinutes.setText(rb.getString(ResourceLeng.TIME_MINUT_TEXT));
        BConfirm.setText(rb.getString(ResourceLeng.TIME_BUTTON_TEXT));
        BConfirm.getTooltip().setText(rb.getString(ResourceLeng.TOOLTIP_SETTIME));
        LFeqSinc.setText(rb.getString(ResourceLeng.TIME_LABEL));
        LPathDownload.setText(rb.getString(ResourceLeng.LABEL_PATH_DOWNLOAD));
        LNextUpdate.setText(rb.getString(ResourceLeng.LABEL_NEXT_UPDATE));
        BUpdate.setText(rb.getString(ResourceLeng.BUTTON_UPDATE_MOODLE));
//        LCheckDate.setText(rb.getString(ResourceLeng.LABEL_CHECK_DATES));
        if (!BUpdate.isDisable() && !LTimeUpdate.getText().isEmpty()) {
            //Esta con una arlama
            setNextUpdateLabel(rb, Calendar.getInstance());
        } else if (!LTimeUpdate.getText().isEmpty()) {
            //Esta actualizando
            LTimeUpdate.setText(main.getResource().getString(ResourceLeng.SYNCRO_NOW));
        }
        BNewUser.getTooltip().setText(rb.getString(ResourceLeng.TOOLTIP_NEWUSER));
        BEditUser.getTooltip().setText(rb.getString(ResourceLeng.TOOLTIP_EDITUSER));
        CBNaster.setText(rb.getString(ResourceLeng.ASK_LABEL_USE_NAS));
        CBNaster.getTooltip().setText(rb.getString(ResourceLeng.ASK_TOOLTIP_NASTER));
    }

    public void wrongDates() {
        InformacionUsuario.borrarFichero();
        if (freqSecuence != null) {
            freqSecuence.stop();
            freqSecuence = null;
        }
        main.borrarRastroUsuario();
        
        // Frecuencia
        LFeqSinc.setVisible(false);
        this.minutesSpinner.setDisable(false);
        minutesSpinner.setVisible(false);
        LMinutes.setVisible(false);
        this.hourSpinner.setDisable(true);
        hourSpinner.setVisible(false);
        LHours.setVisible(false);
        this.BConfirm.setDisable(true);
        BConfirm.setVisible(false);
        //Edits
        this.BEditUser.setDisable(true);
        BEditUser.setVisible(false);
        this.BEditPath.setDisable(true);
        BEditPath.setVisible(false);
        this.LPathApplication.setDisable(true);
        LPathApplication.setVisible(false);
        LPathDownload.setVisible(false);
        // Sig Actualizacion
        LNextUpdate.setVisible(false);
        this.BUpdate.setDisable(true);
        BUpdate.setVisible(false);
        LCheckDate.setVisible(false);
        this.CBNaster.setDisable(true);
        CBNaster.setVisible(false);

        URL iconUrl = this.getClass().getResource("/Resources/Icons/User_Empty.png");
        try (InputStream op = iconUrl.openStream()) {
            IUserIcon.setImage(new Image(op));
        } catch (IOException ex) {
//            Logger.getLogger(InterfaceController.class
//                    .getNombre()).log(Level.SEVERE, null, ex);
        }

        ResourceBundle rb = main.getResource();
        ActionTool.mostrarNotificacion(rb, ResourceLeng.ERROR_DATA_TITLE, ResourceLeng.ERROR_DATA_TEXT, Duration.seconds(15), NotificationType.ERROR);

        LogRecord logRegistro = new LogRecord(Level.SEVERE, rb.getString(ResourceLeng.TRACE_ERROR_DATES_CORRUPT));
        logRegistro.setSourceClassName(this.getClass().getName());
        LogGeneral.log(logRegistro);
    }
 
    protected String getLPathApp(){
        return LPathApplication.getText();
    }
    
    //---------------------------------------------------INIT---------------------------------------------------
    protected void init(boolean user, String userId, String path, MainController main) {
        this.main = main;
        initializeSpinners();
        initializationUserLoad(user, userId, path);
    }
    /**
     * Inicializa los spinner, dando un valor inicial y añadiendole reglas
     * "logicas"
     */
    private void initializeSpinners() {
        SpinnerValueFactory.IntegerSpinnerValueFactory horasFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 23, 0);
        horasFactory.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                return value.toString();
            }

            @Override
            public Integer fromString(String string) {
                Integer respuesta = 0;
                if (!string.isEmpty() && string.chars().allMatch(Character::isDigit)) {
                    respuesta = Integer.valueOf(string);
                }

                return respuesta;
            }
        });
        this.hourSpinner.setValueFactory(horasFactory);
        this.hourSpinner.setEditable(true);

        SpinnerValueFactory.IntegerSpinnerValueFactory minutosFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 60, 5);
        minutosFactory.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                return value.toString();
            }

            @Override
            public Integer fromString(String string) {
                Integer respuesta = 0;
                if (!string.isEmpty() && string.chars().allMatch(Character::isDigit)) {
                    respuesta = Integer.valueOf(string);
                }
                return respuesta;
            }
        });
        this.minutesSpinner.setValueFactory(minutosFactory);
        this.minutesSpinner.setEditable(true);

        hourSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.compareTo("0") <= 0) {
                if ((int) minutesSpinner.getValue() < 5) {
                    minutesSpinner.getValueFactory().setValue(5);
                }
                hourSpinner.getValueFactory().setValue(0);
            }
        });

        minutesSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.compareTo("60") == 0) {
                hourSpinner.getValueFactory().setValue((int) hourSpinner.getValue() + 1);
                minutesSpinner.getValueFactory().setValue(0);
            } else if (newValue.compareTo("-1") == 0) {
//                System.out.println("3-" + newValue.toString());
                hourSpinner.getValueFactory().setValue((int) hourSpinner.getValue() - 1);
                minutesSpinner.getValueFactory().setValue(59);
            } else if (newValue.compareTo("5") < 0) {
                if ((int) hourSpinner.getValue() == 0) {
                    minutesSpinner.getValueFactory().setValue(5);
                }
//                else{
//                    minutesSpinner.getValueFactory().setValue((int)newValue);
//                }
            }
        });
    }
    /**
     * Activa o desactiva items segun la carga de usuario sea correcta o no.
     *
     * @param user
     * @param userId
     * @param path
     */
    private void initializationUserLoad(boolean user, String userId, String path) {
        if (!user) {
//            TTabpane.getSelectionModel().select(OptionConfig);
            main.borrarRastroUsuario();
        } else {
            // Carga de usuario correcta
            URL iconUrl = this.getClass().getResource("/Resources/Icons/User_Ok.png");

            try (InputStream op = iconUrl.openStream()) {
                IUserIcon.setImage(new Image(op));
            } catch (IOException ex) {
//                Logger.getLogger(InterfaceController.class
//                        .getNombre()).log(Level.SEVERE, null, ex);
            }
            LIdUser.setText(userId);
            LPathApplication.setText(path);
        }

        // Frecuencia
        LFeqSinc.setVisible(user);
        this.minutesSpinner.setDisable(!user);
        minutesSpinner.setVisible(user);
        LMinutes.setVisible(user);
        this.hourSpinner.setDisable(!user);
        hourSpinner.setVisible(user);
        LHours.setVisible(user);
        this.BConfirm.setDisable(!user);
        BConfirm.setVisible(user);
        //Edits
        this.BEditUser.setDisable(!user);
        BEditUser.setVisible(user);
        this.BEditPath.setDisable(!user);
        BEditPath.setVisible(user);
        this.LPathApplication.setDisable(!user);
        LPathApplication.setVisible(user);
        LPathDownload.setVisible(user);
        // Sig Actualizacion
        LNextUpdate.setVisible(user);
        this.BUpdate.setDisable(!user);
        BUpdate.setVisible(user);
        LCheckDate.setVisible(user);

        this.CBNaster.setDisable(!user);
        CBNaster.setVisible(user);
    }

 

}
