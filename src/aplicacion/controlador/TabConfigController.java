package aplicacion.controlador;

//#1 Static import
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
//import aplicacion.controlador.MainController;
import aplicacion.eventos.EventosUsuario;
import aplicacion.eventos.ProcesoSyncronizacion;
import aplicacion.eventos.Validador;
import tools.almacen.InformacionUsuario;
import tools.lenguaje.ResourceLeng;
import tools.logger.LogGeneral;
//#4 Java
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
import java.util.ResourceBundle;
//#5 JavaFx
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * 680
 * @author Diego Alvarez
 */
public class TabConfigController {// implements Initializable{

    private MainController main;

    @FXML
    private Label lTituloLanguague;
    @FXML
    private ComboBox cLanguague;
    @FXML
    private Label lHoras;
    @FXML
    private Spinner sHoras;
    @FXML
    private Label lMinutos;
    @FXML
    private Spinner sMinutos;
    @FXML
    private ImageView iUsuario;
    @FXML
    private Button bNuevoUsuario;
    @FXML
    private Button bEditarUsuario;
    @FXML
    private Label lIdUsuario;
    @FXML
    private Label lTituloPathAplicacion;
    @FXML
    private Label lPathAplicacion;
    @FXML
    private CheckBox cUsoNaster;
    @FXML
    private Button bConfirmar;
    @FXML
    private Label lTituloFrecuenciaSinc;
    @FXML
    private Button bEditPath;
    @FXML
    private Label lTituloSiguienteActualizacion;
    @FXML
    private Button bActualizar;
    @FXML
    private Label lSiguienteActualizacion;
    
    private Timeline alarmaSincronizacion;
    private Calendar momentoSiguienteActualizacion;
   

    //---------------------------------------------------FXML---------------------------------------------------   
    /**
     * Tratara el evento generado por el usuario en relacion a selecionar un
     * idioma para la App
     *
     * @param event
     */
    @FXML
    private void cambiarLenguague(ActionEvent event) {
        LogRecord logRegistro = null;
        ResourceBundle rb = main.getResource();
        ResourceBundle auxRb;
        String languagueSelected = (String) cLanguague.getValue();
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
                main.cambiarLenguage(auxRb);
            }
        }
        if (logRegistro != null) {
            LogGeneral.log(logRegistro);
        }
    }

    /**
     * Tratara el evento generado por el usuario en relacion a crear un nuevo
     * "perfil" de usuario.
     *
     * @param event
     */
    @FXML
    private void crearNuevoUsuario(ActionEvent event) {
        if(main.OcuparUsuario()){
            ResourceBundle rb = main.getResource();
            LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_EVENT_USER_NEW));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);

            new EventosUsuario("", "", "", false, rb, true, this);
            if (alarmaSincronizacion != null) {
                alarmaSincronizacion.pause();
            }
            bNuevoUsuario.setDisable(true);
            bEditarUsuario.setDisable(true);
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
    private void editarUsuario(ActionEvent event) {
        if(main.OcuparUsuario()){
            try {
                ResourceBundle rb = main.getResource();
                LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_EVENT_USER_EDIT));
                logRegistro.setSourceClassName(this.getClass().getName());
                LogGeneral.log(logRegistro);

                new EventosUsuario(InformacionUsuario.getUser(), InformacionUsuario.getPass1(), InformacionUsuario.getPass2(),
                        InformacionUsuario.getUseNas(), rb, false, this);
                if (alarmaSincronizacion != null) {
                    alarmaSincronizacion.pause();
                }
                bNuevoUsuario.setDisable(true);
                bEditarUsuario.setDisable(true);
            } catch (NoSuchFieldException e) {
                borrarUsuario();
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
    private void cambiarDirectorio(ActionEvent event) {
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
            lPathAplicacion.setText(initialPath);
            InformacionUsuario.setPath(initialPath);
        } catch (NoSuchFieldException e) {
            borrarUsuario();
        }

    }

    /**
     * Establece un Timer "alarma" segun los valores de los Spinner y la hora
     * actual a los 00 segundos; para el Timer que aun estubiera establecido.
     *
     */
    @FXML
    protected void setSiguienteAlarma() {
        if (alarmaSincronizacion != null) {
            alarmaSincronizacion.stop();
            alarmaSincronizacion = null;
        }
        int minutos = (int) sMinutos.getValue();
        int horas = (int) sHoras.getValue();

        momentoSiguienteActualizacion = Calendar.getInstance();
        Calendar momentoActual = (Calendar) momentoSiguienteActualizacion.clone();
        momentoSiguienteActualizacion.add(Calendar.MINUTE, minutos);
        momentoSiguienteActualizacion.add(Calendar.HOUR_OF_DAY, horas); // adds hour
        momentoSiguienteActualizacion.set(Calendar.SECOND, 0);

        ResourceBundle rb = main.getResource();
        setSigueinteAlarmaLabel(rb, momentoActual);

        long diff = momentoSiguienteActualizacion.getTime().getTime() - momentoActual.getTime().getTime();
        alarmaSincronizacion = new Timeline(new KeyFrame(
                Duration.seconds(diff / 1000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                LogRecord logRegistro = new LogRecord(Level.SEVERE, rb.getString(ResourceLeng.TRACE_TIMER_END));
                logRegistro.setSourceClassName(this.getClass().getName());
                LogGeneral.log(logRegistro);
                sincronizar();
            }
        }));

        alarmaSincronizacion.setCycleCount(1);
        alarmaSincronizacion.play();
        if(main.disponibleUsuario()){
            alarmaSincronizacion.pause();
        }
    }
   
    /**
     * Metodo que inicia el proceso de sincronizacion, no asociado al Timer;
     * esto implicara para el Timer.
     */
    @FXML
    private void sincronizarAhora() {
        if(!main.disponibleUsuario()){
            alarmaSincronizacion.stop();
            sincronizar();
        }
    }

    /**
     * Manejara el evento generado por el usuario para activar/desactivar el uso
     * de Nas-Ter en la sincronizacion. Comprovando que los datos son corectos
     *
     * @param event
     */
    @FXML
    private void utilizarNasTer(ActionEvent event) {
        if (cUsoNaster.isSelected()) {
            try {
//                int resultado = EventosUsuario.validarCredencialesNaster(InformacionUsuario.getUser(), InformacionUsuario.getPass2());
                int resultado = Validador.validarCredencialesNaster(InformacionUsuario.getUser(), InformacionUsuario.getPass2());

                if (resultado == 2) {
                    cUsoNaster.setSelected(false);
                    ActionTool.mostrarNotificacion(ResourceLeng.MESSAGE_TITLE_NASTER_REJECT,
                            ResourceLeng.MESSAGE_INFO_NASTER_REJECT, Duration.seconds(15), NotificationType.WARNING);
                } else {
                    // El caso de naster caido resultado == 1 lo trataremos mas adelante en la syncronizacion
                    InformacionUsuario.setUseNas("true");
                }
            } catch (NoSuchFieldException e) {
                cUsoNaster.setSelected(false);
                borrarUsuario();
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
    public void establecerUsuario(List<String> dates, boolean isnew) {
        try {
            if (dates != null && isnew) {
                InformacionUsuario.crearFichero(dates.get(0), dates.get(1), dates.get(2), dates.get(3), String.valueOf(dates.get(4)));
                initializationUserLoad(true, dates.get(0), dates.get(3));
                cUsoNaster.setSelected(Boolean.parseBoolean(dates.get(4)));
                main.establecerUsuario();
            } else if (dates != null) {
                InformacionUsuario.crearFichero(dates.get(0), dates.get(1), dates.get(2), InformacionUsuario.getPath(), String.valueOf(dates.get(4)));
                lIdUsuario.setText(dates.get(0));
                cUsoNaster.setSelected(Boolean.parseBoolean(dates.get(4)));
            }

            bEditarUsuario.setDisable(false);
            bNuevoUsuario.setDisable(false);
            main.liberarUsuario();

            LogRecord logRegistro = new LogRecord(Level.INFO, main.getResource().getString(ResourceLeng.TRACE_EVENT_USER_END));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
        } catch (NoSuchFieldException e) {
            borrarUsuario();
        }
    }

    
    /**
     * Metodo que iniciara el proceso de sincronizacion. 
     *
     * Adenas desactivara las interacciones que puedan lanzar este proceso como
     * los botones de la App o la opcion en el Systray (si lo hubiera)
     */
    private void sincronizar() {
        main.OcuparUsuario();
        main.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_SYNCRO, false);
        bEditPath.setDisable(true);
        main.iniciarSincronizacion();

        lSiguienteActualizacion.setText(main.getResource().getString(ResourceLeng.SYNCRO_NOW));
        try {
            new ProcesoSyncronizacion(InformacionUsuario.getUser(), InformacionUsuario.getPass1(),
                    InformacionUsuario.getPass2(), InformacionUsuario.getPath(),
                    main.getResource(), main, cUsoNaster.isSelected());
        } catch (NoSuchFieldException e) {
            borrarUsuario();
        }
    }
    /**
     * Metodo para manejar el fin del evento de sincronizar, reactivando todo lo
     * que este evento hubiera desactivado y estableciendo la sigueinte "alarma"
     */
    public void sincronizarFin() {
        setSiguienteAlarma();
        bEditPath.setDisable(false);
        main.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_SYNCRO, true);
    }

  
    /**
     * Reactivara el Timer "alarma", en caso de que la "alarma" ya debiera haber
     * saltado lanza el proceso asociado a fin del Timer; en caso contrario
     * recalculara el tiempo que le debiera quedar a la "alarma" y la pone en
     * marcha.
     */
    private void reactivarAlarma() {
        Calendar momentoActual = Calendar.getInstance();
        if (momentoActual.after(momentoSiguienteActualizacion)) {
            // Paso el momento de la alarma, sincronizamos ya
            LogRecord logRegistro = new LogRecord(Level.SEVERE, main.getResource().getString(ResourceLeng.TRACE_TIMER_LATE));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
            sincronizar();
        } else {
            // Calcular cuanto tiempo estuvimos parados y poner nueva
            alarmaSincronizacion.stop();
            long diff = momentoSiguienteActualizacion.getTime().getTime() - momentoActual.getTime().getTime();
            alarmaSincronizacion = new Timeline(new KeyFrame(
                    Duration.seconds(diff / 1000), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    LogRecord logRegistro = new LogRecord(Level.SEVERE, main.getResource().getString(ResourceLeng.TRACE_TIMER_END));
                    logRegistro.setSourceClassName(this.getClass().getName());
                    LogGeneral.log(logRegistro);
                    sincronizar();
                }
            }));
            alarmaSincronizacion.setCycleCount(1);
            alarmaSincronizacion.play();
        }
    }
    /**
     * Cambiara la Label informativa de la App para mostrar cuando sera la
     * sigeuinte actualizacion.
     *
     * @param rb resource del idioma
     * @param now momento actual.
     */
    private void setSigueinteAlarmaLabel(ResourceBundle rb, Calendar now) {
        String dayTime;
        if (now.get(Calendar.DAY_OF_MONTH) == momentoSiguienteActualizacion.get(Calendar.DAY_OF_MONTH)) {
            dayTime = rb.getString(ResourceLeng.DAY_TODAY);
        } else {
            dayTime = rb.getString(ResourceLeng.DAY_TOMORROW);
        }
        String line = rb.getString(ResourceLeng.NEXT_TIME_SEED);
        line = String.format(line, dayTime);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        line += sdf.format(momentoSiguienteActualizacion.getTime());
        lSiguienteActualizacion.setText(line);
    }
    
    /**
     * Metodo para liberar un usuario, el perfil ha dejado de estar ocupado
     * implicando la reactivacion de las cosas que pudieran hacer uso de este
     */
    protected void liberarUsuario(){
        if (alarmaSincronizacion != null) {
            reactivarAlarma();
            setSigueinteAlarmaLabel(main.getResource(), Calendar.getInstance());
        }else if (!lIdUsuario.getText().isEmpty()){
            setSiguienteAlarma();
        }
        bConfirmar.setDisable(false);
        bActualizar.setDisable(false);
    }
    /**
     * Metodo para ocupar un usuario, el perfil va a ser ocupado implicando la 
     *  desactivacion de las cosas qu epudieran hacer uso de este
     * 
     * blockSyncro
     */
    protected void ocuparUsuario(){
        if (alarmaSincronizacion != null) {
            alarmaSincronizacion.pause();
        }
        bConfirmar.setDisable(true);
        bActualizar.setDisable(true);
    }
    
    
    //---------------------------------------------------UTILS-------------------------------------------------- 
    protected void setLanguague(ResourceBundle rb) {
        this.lTituloLanguague.setText(rb.getString(ResourceLeng.LANGUAGE));
        this.cLanguague.getItems().clear();
        for (Map.Entry<String, String> e : ResourceLeng.LANGUAGES.entrySet()) {
            cLanguague.getItems().add(rb.getString(e.getValue()));
        }
        String idioma = ResourceLeng.LANGUAGES.get(rb.getLocale().getLanguage());
        cLanguague.setValue(rb.getString(idioma));
        lHoras.setText(rb.getString(ResourceLeng.TIME_HOUR_TEXT));
        lMinutos.setText(rb.getString(ResourceLeng.TIME_MINUT_TEXT));
        bConfirmar.setText(rb.getString(ResourceLeng.TIME_BUTTON_TEXT));
        bConfirmar.getTooltip().setText(rb.getString(ResourceLeng.TOOLTIP_SETTIME));
        lTituloFrecuenciaSinc.setText(rb.getString(ResourceLeng.TIME_LABEL));
        lTituloPathAplicacion.setText(rb.getString(ResourceLeng.LABEL_PATH_DOWNLOAD));
        lTituloSiguienteActualizacion.setText(rb.getString(ResourceLeng.LABEL_NEXT_UPDATE));
        bActualizar.setText(rb.getString(ResourceLeng.BUTTON_UPDATE_MOODLE));
//        LCheckDate.setText(rb.getString(ResourceLeng.LABEL_CHECK_DATES));
        if (!bActualizar.isDisable() && !lSiguienteActualizacion.getText().isEmpty()) {
            //Esta con una arlama
            setSigueinteAlarmaLabel(rb, Calendar.getInstance());
        } else if (!lSiguienteActualizacion.getText().isEmpty()) {
            //Esta actualizando
            lSiguienteActualizacion.setText(main.getResource().getString(ResourceLeng.SYNCRO_NOW));
        }
        bNuevoUsuario.getTooltip().setText(rb.getString(ResourceLeng.TOOLTIP_NEWUSER));
        bEditarUsuario.getTooltip().setText(rb.getString(ResourceLeng.TOOLTIP_EDITUSER));
        cUsoNaster.setText(rb.getString(ResourceLeng.ASK_LABEL_USE_NAS));
        cUsoNaster.getTooltip().setText(rb.getString(ResourceLeng.ASK_TOOLTIP_NASTER));
    }

    /**
     * Metodo para borrar el perfil de usuario, y parando la alarma y elementos 
  asociados
 borrarUsuario
     */
    public void borrarUsuario() {
        InformacionUsuario.borrarFichero();
        if (alarmaSincronizacion != null) {
            alarmaSincronizacion.stop();
            alarmaSincronizacion = null;
        }
        main.borrarRastroUsuario();
        
        // Frecuencia
        lTituloFrecuenciaSinc.setVisible(false);
        this.sMinutos.setDisable(false);
        sMinutos.setVisible(false);
        lMinutos.setVisible(false);
        this.sHoras.setDisable(true);
        sHoras.setVisible(false);
        lHoras.setVisible(false);
        this.bConfirmar.setDisable(true);
        bConfirmar.setVisible(false);
        //Edits
        this.bEditarUsuario.setDisable(true);
        bEditarUsuario.setVisible(false);
        this.bEditPath.setDisable(true);
        bEditPath.setVisible(false);
        this.lPathAplicacion.setDisable(true);
        lPathAplicacion.setVisible(false);
        lTituloPathAplicacion.setVisible(false);
        // Sig Actualizacion
        lTituloSiguienteActualizacion.setVisible(false);
        this.bActualizar.setDisable(true);
        bActualizar.setVisible(false);
        this.cUsoNaster.setDisable(true);
        cUsoNaster.setVisible(false);

        URL iconUrl = this.getClass().getResource("/Resources/Icons/User_Empty.png");
        try (InputStream op = iconUrl.openStream()) {
            iUsuario.setImage(new Image(op));
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
        return lPathAplicacion.getText();
    }
    
    public void sincronizarSysTray(){
        this.sincronizarAhora();
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
        this.sHoras.setValueFactory(horasFactory);
        this.sHoras.setEditable(true);

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
        this.sMinutos.setValueFactory(minutosFactory);
        this.sMinutos.setEditable(true);

        sHoras.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.compareTo("0") <= 0) {
                if ((int) sMinutos.getValue() < 5) {
                    sMinutos.getValueFactory().setValue(5);
                }
                sHoras.getValueFactory().setValue(0);
            }
        });

        sMinutos.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.compareTo("60") == 0) {
                sHoras.getValueFactory().setValue((int) sHoras.getValue() + 1);
                sMinutos.getValueFactory().setValue(0);
            } else if (newValue.compareTo("-1") == 0) {
//                System.out.println("3-" + newValue.toString());
                sHoras.getValueFactory().setValue((int) sHoras.getValue() - 1);
                sMinutos.getValueFactory().setValue(59);
            } else if (newValue.compareTo("5") < 0) {
                if ((int) sHoras.getValue() == 0) {
                    sMinutos.getValueFactory().setValue(5);
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
            main.borrarRastroUsuario();
        } else {
            // Carga de usuario correcta
            URL iconUrl = this.getClass().getResource("/Resources/Icons/User_Ok.png");

            try (InputStream op = iconUrl.openStream()) {
                iUsuario.setImage(new Image(op));
            } catch (IOException ex) {
//                Logger.getLogger(InterfaceController.class
//                        .getNombre()).log(Level.SEVERE, null, ex);
            }
            lIdUsuario.setText(userId);
            lPathAplicacion.setText(path);
        }

        // Frecuencia
        lTituloFrecuenciaSinc.setVisible(user);
        this.sMinutos.setDisable(!user);
        sMinutos.setVisible(user);
        lMinutos.setVisible(user);
        this.sHoras.setDisable(!user);
        sHoras.setVisible(user);
        lHoras.setVisible(user);
        this.bConfirmar.setDisable(!user);
        bConfirmar.setVisible(user);
        //Edits
        this.bEditarUsuario.setDisable(!user);
        bEditarUsuario.setVisible(user);
        this.bEditPath.setDisable(!user);
        bEditPath.setVisible(user);
        this.lPathAplicacion.setDisable(!user);
        lPathAplicacion.setVisible(user);
        lTituloPathAplicacion.setVisible(user);
        // Sig Actualizacion
        lTituloSiguienteActualizacion.setVisible(user);
        this.bActualizar.setDisable(!user);
        bActualizar.setVisible(user);

        this.cUsoNaster.setDisable(!user);
        cUsoNaster.setVisible(user);
    }
}
