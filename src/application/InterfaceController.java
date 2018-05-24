package application;

import Tools.userDates.UserInfo;
import Updater.tools.ActionTool;
import Updater.tools.NotificationType;
import Tools.language.ResourceLeng;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;
import application.events.validator;
import application.events.eventUser_semaphore;
import application.events.procesoSyncronizacion;
import java.util.HashMap;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import Sincronice.moodle.tree.TypeNode;
import zzParaBorrar.eventUser;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;
import javafx.concurrent.Task;

/**
 * FXML Controller class
 *
 * @author Usuario
 */
public class InterfaceController implements Initializable {

    @FXML
    private TabPane TTabpane;
    //*************************************** OptionInit
    @FXML
    private Tab OptionInit;
    @FXML
    private TreeView TListUpdates;
    /**
     * Max Syncronization Before Clean List Update
     */
    private final int MAX_SBCLU = 1;
    private int numSyncro;
    private Map<String, TreeItem<BookCategory>> cursosTrack = new HashMap<>();
    //*************************************** OptionConfig
    @FXML
    private Tab OptionConfig;
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

    //***************************************** OptionAyuda
    @FXML
    private Tab OptionAyuda;
    @FXML
    private Button BActualizar;
    @FXML
    private Label LCurrentVersion;
    @FXML
    private Hyperlink HopenHelp;
    private String urlWiki;
    @FXML
    private TextFlow TCredits;
    @FXML
    private Hyperlink HnasterHelp;
    private String urlNas;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LogRecord logRegistro;
        
        ResourceBundle auxRb;
        if (rb != null) {
            auxRb = rb;
        } else {
            auxRb = HelloWorld.getResource();
        }
        logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_INIT));
        logRegistro.setSourceClassName(this.getClass().getName());
        LoggGen.log(logRegistro);
        setLanguague(auxRb);

        initializeSpinners();
        if (!UserInfo.dataExits()) {
            // No hay usuario
            initializationUserLoad(false, "", "");
            logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_NO));
        } else {
            try{
                initializationUserLoad(true, UserInfo.getUser(), UserInfo.getPath());
                setNextUpdate();

                TreeItem<BookCategory> rootItem = new TreeItem<BookCategory>();
                CBNaster.setSelected(UserInfo.getUseNas());
                TListUpdates.setRoot(rootItem);
                logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_OK));
            }catch(NoSuchFieldException e){
                
            }
        }
        if(logRegistro != null){
            logRegistro.setSourceClassName(this.getClass().getName());
            LoggGen.log(logRegistro);
   
        }
    }
    /**
     * Inicializa la treeView y le aniade un listener. Esto se podria hacer con 
     *  el Scene builder o ha pelo pero no se.
     */
    private void initializeTreeView() {
        TreeItem<BookCategory> rootItem = new TreeItem<BookCategory>();
        TListUpdates.setRoot(rootItem);
        TListUpdates.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> handleTreeViewClick((TreeItem) newValue));
    }
    /**
     * Aniadira un nuevo item al treeView. Aniadienssolselo como "hijo" a un nodo
     *  que lo represente, creando este si fuera necesario.
     * 
     * @param path path del recurso que representa el item
     * @param name nombre con el que se representara
     * @param tipo tipo del item, para asignarle un icono
     */
    public synchronized void addTreeItem(String path, String name, TypeNode tipo) {
        URL iconUrl = null;
        Image miImage = null;
        // Averiguaar donde meter  le nuevo elemento
        String curso = path.replace(LPathApplication.getText() + File.separator, "");
        curso = curso.substring(0, curso.indexOf(File.separator));
        TreeItem<BookCategory> auxCurso;
        if (cursosTrack.containsKey(curso)) {
            // Existe, lo pedimos
            auxCurso = cursosTrack.get(curso);
        } else {
            // No existe, lo creamos y metemos
            iconUrl = this.getClass().getResource("/Resources/Icons/folder.png");
            try (InputStream op = iconUrl.openStream()) {
                miImage = new Image(op);
            } catch (IOException ex) {
                Logger.getLogger(InterfaceController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            BookCategory auxbook = new BookCategory(LPathApplication.getText()
                    + File.separator + curso, curso);
            auxCurso = new TreeItem<>(auxbook, new ImageView(miImage));
            cursosTrack.put(curso, auxCurso);
            TListUpdates.getRoot().getChildren().add(auxCurso);
        }
        //  Creacion de un elemento para el arbol
        //  Creacion de una Imagen (icono) representativa del tipo
        int indexExt = path.lastIndexOf(".");
        String auxExtension = "";
        if (indexExt > 0) {
            auxExtension = path.substring(indexExt);
        }
        switch (auxExtension) {
            case ".pdf":
                iconUrl = this.getClass().getResource("/Resources/Icons/pdf.png");
                break;
            case ".zip":
                iconUrl = this.getClass().getResource("/Resources/Icons/zip.png");
                break;
            case ".avi":
                iconUrl = this.getClass().getResource("/Resources/Icons/video.png");
                break;
            case ".htm":
                iconUrl = this.getClass().getResource("/Resources/Icons/web.png");
                break;
            default:
                iconUrl = this.getClass().getResource("/Resources/Icons/other.png");
        }

        try (InputStream op = iconUrl.openStream()) {
            miImage = new Image(op);
        } catch (IOException ex) {
//            Logger.getLogger(InterfaceController.class
//                    .getName()).log(Level.SEVERE, null, ex);
        }
        BookCategory auxbook = new BookCategory(path, name);
        TreeItem<BookCategory> auxItem = new TreeItem<>(auxbook, new ImageView(miImage));
        auxCurso.getChildren().add(0, auxItem);
    }
    /**
     * Handle que tratara las acciones sobre el treeView, hara que el local trate
     *  el path asociado al item que tratamos
     * 
     * @param newValue treeItem que disparo el evento
     */
    private void handleTreeViewClick(TreeItem newValue) {
        BookCategory aux = null;
        try {
            aux = (BookCategory) newValue.getValue();
            HelloWorld.getHostService().showDocument(aux.getCode());
        } catch (Exception e) {
            LogRecord logRegistro;
            StringWriter errors = new StringWriter();
            String auxWho = HelloWorld.getResource().getString(ResourceLeng.TRACE_TREE_ERROR);
            
            e.printStackTrace(new PrintWriter(errors));
            auxWho = String.format(auxWho, aux.print());
            logRegistro = new LogRecord(Level.SEVERE, auxWho + "\n" + errors.toString());
            logRegistro.setSourceClassName(this.getClass().getName());
            LoggGen.log(logRegistro);
        }
    }
    /**
     * 
     * Limpiara el treeView dejando solo el root
     */
    private void cleanTreeview() {
        TListUpdates.getRoot().getChildren().clear();
        cursosTrack.clear();
    }

    //*********************OptionConfig********************************************
    /**
     * Tratara el evento generado por el usuario en relacion a selecionar un 
     *  idioma para la App
     * 
     * @param _event 
     */
    public void changeLanguague(ActionEvent _event) {
        LogRecord logRegistro = null;
        ResourceBundle rb = HelloWorld.getResource();
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
                HelloWorld.changeTitle(auxRb.getString(ResourceLeng.APP_TITLE));
                HelloWorld.setResource(auxRb);
                setLanguague(auxRb);
            }
        }
        if (logRegistro != null) {
            LoggGen.log(logRegistro);
        }
    }

    /**
     * Inicializa los spinner, dando un valor inicial y añadiendole reglas "logicas"
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
            TTabpane.getSelectionModel().select(OptionConfig);
        } else {
            // Carga de usuario correcta
            URL iconUrl = this.getClass().getResource("/Resources/Icons/User_Ok.png");

            try (InputStream op = iconUrl.openStream()) {
                IUserIcon.setImage(new Image(op));
            } catch (IOException ex) {
//                Logger.getLogger(InterfaceController.class
//                        .getName()).log(Level.SEVERE, null, ex);
            }
            LIdUser.setText(userId);
            LPathApplication.setText(path);
            initializeTreeView();
            HelloWorld.addOptionPopup(this, "openHelp", ResourceLeng.SYS_TRAY_WIKI);
            HelloWorld.addOptionPopup(this, "actualizarVersion", ResourceLeng.SYS_TRAY_UPDATE);
            HelloWorld.addOptionPopup(this, "syncroNow", ResourceLeng.SYS_TRAY_SYNCRO);
        }

        OptionInit.setDisable(!user);
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

    /**
     * Tratara el evento generado pro el usuario en relacion a crear un nuevo
     *  "perfil" de usuario.
     * 
     * @param event 
     */
    public void createNewUser(ActionEvent event) {
        new eventUser_semaphore("", "", "", false, HelloWorld.getResource(), true, this);
        BNewUser.setDisable(true);
    }
    /**
     * Tratara el evento generado por el usuario en relacion a editar un "
     *  "perfil" de usuario. Dicho evento pondra en pausa el Timer para la 
     *  siguiente actualizacion.
     * 
     * @param event 
     */
    public void editUser(ActionEvent event) {
        try{
            new eventUser_semaphore(UserInfo.getUser(), UserInfo.getPass1(), UserInfo.getPass2(),
                    UserInfo.getUseNas(), HelloWorld.getResource(), false, this);
            if (freqSecuence != null) {
                freqSecuence.pause();
            }
            BEditUser.setDisable(true);
        }catch(NoSuchFieldException e){
            wrongDates();
        }
    }
    /**
     * Metodo que maneja el fin de los eventos edit/new User reactivara/creara 
     *  el Timer para la siguiente actualizacion.y en caso de que el usuario 
     *  complete los eventos guardara los datos resultantes.
     * 
     * @param dates
     * @param isnew 
     */
    public void setUserInfo(List<String> dates, boolean isnew) {
        try{
            if (dates != null && isnew) {
                UserInfo.createFile(dates.get(0), dates.get(1), dates.get(2), dates.get(3), String.valueOf(dates.get(4)));
                initializationUserLoad(true, dates.get(0), dates.get(3));
                CBNaster.setSelected(Boolean.parseBoolean(dates.get(4)));
            } else if (dates != null) {
                UserInfo.createFile(dates.get(0), dates.get(1), dates.get(2), UserInfo.getPath(), String.valueOf(dates.get(4)));
                LIdUser.setText(dates.get(0));
                CBNaster.setSelected(Boolean.parseBoolean(dates.get(4)));
            }

            BEditUser.setDisable(false);
            BNewUser.setDisable(false);
            if (freqSecuence != null) {
                resumeUpdate();
            } else if (dates != null && freqSecuence == null) {
                setNextUpdate();
            }
        }catch(NoSuchFieldException e){
            wrongDates();
        }
    }
    
    /**
     * Establece un Timer "alarma" segun los valores de los Spinner y 
     *  la hora actual a los 00 segundos; para el Timer que aun estubiera establecido. 
     * 
     * 
     */
    public void setNextUpdate() {
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

        ResourceBundle rb = HelloWorld.getResource();
        setNextUpdateLabel(rb, momentoActual);

        long diff = momentoSigAct.getTime().getTime() - momentoActual.getTime().getTime();
        freqSecuence = new Timeline(new KeyFrame(
                Duration.seconds(diff / 1000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                LogRecord logRegistro = new LogRecord(Level.SEVERE, rb.getString(ResourceLeng.TRACE_TIMER_END));
                logRegistro.setSourceClassName(this.getClass().getName());
                LoggGen.log(logRegistro);
                syncroStart();
            }
        }));

        freqSecuence.setCycleCount(1);
        freqSecuence.play();
    }
    /**
     * Reactivara el Timer "alarma", en caso de que la "alarma" ya debiera haber 
     *  saltado lanza el proceso asociado a fin del Timer; en caso contrario 
     *  recalculara el tiempo que le debiera quedar a la "alarma" 
     *  y la pone en marcha.
     */
    private void resumeUpdate() {
        Calendar momentoActual = Calendar.getInstance();
        if (momentoActual.after(momentoSigAct)) {
            // Paso el momento de la alarma, sincronizamos ya
            LogRecord logRegistro = new LogRecord(Level.SEVERE, HelloWorld.getResource().getString(ResourceLeng.TRACE_TIMER_LATE));
            logRegistro.setSourceClassName(this.getClass().getName());
            LoggGen.log(logRegistro);
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
                    LogRecord logRegistro = new LogRecord(Level.SEVERE, HelloWorld.getResource().getString(ResourceLeng.TRACE_TIMER_END));
                    logRegistro.setSourceClassName(this.getClass().getName());
                    LoggGen.log(logRegistro);
                    syncroStart();
                }
            }));

            freqSecuence.setCycleCount(1);
            freqSecuence.play();
        }
    }
    /**
     * Cambiara la Label informativa de la App para mostrar cuando sera 
     *  la sigeuinte actualizacion.
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

    /**
     * Metodo que inicia el proceso de sincronizacion, no asociado al Timer; 
     *  esto implicara para el Timer.
     */
    public void syncroNow() {
        freqSecuence.stop();
        syncroStart();
    }
    /**
     * Metodo que iniciara el proceso de sincronizacion. Limpiara el TreeView si
     *  es la X vez que lanzamos la sincronizaion. 
     * 
     * Adenas desactivara las interacciones que puedan lanzar este proceso como 
     *  los botones de la App o la opcion en el Systray (si lo hubiera)
     */
    private void syncroStart() {
        HelloWorld.changeEnable(ResourceLeng.SYS_TRAY_SYNCRO, false);
        BConfirm.setDisable(true);
        BUpdate.setDisable(true);
        BNewUser.setDisable(true);
        BEditPath.setDisable(true);
        BEditUser.setDisable(true);
        BActualizar.setDisable(true);
        if (numSyncro == MAX_SBCLU) {
            cleanTreeview();
            numSyncro = 0;
        } else {
            numSyncro++;
        }
        
        LTimeUpdate.setText(HelloWorld.getResource().getString(ResourceLeng.SYNCRO_NOW));
        try{
            new procesoSyncronizacion(UserInfo.getUser(), UserInfo.getPass1(),
                    UserInfo.getPass2(), UserInfo.getPath(),
                    HelloWorld.getResource(), this, CBNaster.isSelected());
        }catch(NoSuchFieldException e){
            wrongDates();
        }
    }
    /**
     * Metodo para manejar el fin del evento de sincronizar, reactivando todo lo
     *  que este evento hubiera desactivado y estableciendo la sigueinte "alarma"
     */
    public void syncroEnd() {
        setNextUpdate();
        BConfirm.setDisable(false);
        BUpdate.setDisable(false);
        BNewUser.setDisable(false);
        BEditPath.setDisable(false);
        BEditUser.setDisable(false);
        BActualizar.setDisable(false);
        HelloWorld.changeEnable(ResourceLeng.SYS_TRAY_SYNCRO, true);
//        HelloWorld.showApp();
    }

    /**
     * Manejara el evento generado por el usuario para seleccionar un nuevo 
     *  directorio para que funcione la App, iniciando la navegacion en el path 
     *  actual. Comprobara que el directorio selecionado se tenga permisos e
     *  informando en caso contrario; y finalizara guardando el path resultante
     * 
     * @param event 
     */
    public void chooseDirectory(ActionEvent event) {
        File selectedFile = null;
        String initialPath;
        try{
            initialPath = UserInfo.getPath();
            do {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File(initialPath));
                selectedFile = directoryChooser.showDialog(null);
                if (validator.checkPermissions(selectedFile.getAbsolutePath())) {
                    initialPath = selectedFile.getAbsolutePath();
                    break;
                } else {
                    ActionTool.customNotification(ResourceLeng.MESSAGE_TITLE_PATH_REJECT,
                        ResourceLeng.MESSAGE_INFO_PATH_REJECT, Duration.seconds(15),
                        NotificationType.ERROR);
                }

            } while (selectedFile != null);
            LPathApplication.setText(initialPath);
            UserInfo.setPath(initialPath);
        }catch(NoSuchFieldException e){
           wrongDates();
        }
        
    }
    
    /**
     * Manejara el evento generado por el usuario para activar/desactivar el uso
     *  de Nas-Ter en la sincronizacion. Comprovando que los datos son corectos
     * 
     * @param event 
     */
    public void useNasTer(ActionEvent event) {
        if(CBNaster.isSelected()){
            try{
            int resultado = eventUser_semaphore.validateCredentialsNaster(UserInfo.getUser(), UserInfo.getPass2());
 
            if (resultado == 2) {
//                ResourceBundle rb = HelloWorld.getResource();
                CBNaster.setSelected(false);
                ActionTool.customNotification(ResourceLeng.MESSAGE_TITLE_NASTER_REJECT,
                        ResourceLeng.MESSAGE_INFO_NASTER_REJECT, Duration.seconds(15), NotificationType.WARNING);
            } else {
                // El caso de naster caido resultado == 1 lo trataremos mas adelante en la syncronizacion
                UserInfo.setUseNas("true");
            }
            }catch(NoSuchFieldException e){
                CBNaster.setSelected(false);
                wrongDates();
            }
        }else{
            
            UserInfo.setUseNas("false");
        }
    }
    
    public void wrongDates(){
        UserInfo.deleteFile();
        if(freqSecuence != null){
            freqSecuence.stop();
            freqSecuence = null;
        }
        
        OptionInit.setDisable(true);
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
//                    .getName()).log(Level.SEVERE, null, ex);
        }
        
        ResourceBundle rb = HelloWorld.getResource();
        ActionTool.customNotification(rb, ResourceLeng.ERROR_DATA_TITLE, ResourceLeng.ERROR_DATA_TEXT
                , Duration.seconds(15), NotificationType.ERROR);
        
        LogRecord logRegistro = new LogRecord(Level.SEVERE, rb.getString(ResourceLeng.TRACE_ERROR_DATES_CORRUPT));
        logRegistro.setSourceClassName(this.getClass().getName());
        LoggGen.log(logRegistro);
    }

    //*********************OptionAyuda*********************************************
    /**
     * 
     * Metodo que genera el evento de actualizar la App
     */
    public void actualizarVersion() {
        HelloWorld.changeEnable(ResourceLeng.SYS_TRAY_UPDATE, false);
        HelloWorld.actualizarVersion(true);
//        System.err.println("called ok");
    }

    /**
     * 
     * @param _text 
     */
    public void credictContact(Hyperlink _text) {
        String idioma = HelloWorld.getResource().getLocale().getLanguage().toUpperCase();
        HelloWorld.getHostService().showDocument("mailto:" + _text.getText() + "?Subject=[" + idioma + "] ");
        _text.setVisited(false);
    }

    public void openHelpWiki() {
        HelloWorld.getHostService().showDocument(this.urlWiki);
    }

    public void openHelpNas() {
        HelloWorld.getHostService().showDocument(this.urlNas);
    }

//******************************** Utils*******************************************
    private void setLanguague(ResourceBundle rb) {
        //******* Tab OptionInit
        this.OptionInit.setText(rb.getString(ResourceLeng.TAB_INIT));
        //******* Tab OptionConfig
        this.OptionConfig.setText(rb.getString(ResourceLeng.TAB_CONFIG));
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
            LTimeUpdate.setText(HelloWorld.getResource().getString(ResourceLeng.SYNCRO_NOW));
        }
        BNewUser.getTooltip().setText(rb.getString(ResourceLeng.TOOLTIP_NEWUSER));
        BEditUser.getTooltip().setText(rb.getString(ResourceLeng.TOOLTIP_EDITUSER));
        CBNaster.setText(rb.getString(ResourceLeng.ASK_LABEL_USE_NAS));
        CBNaster.getTooltip().setText(rb.getString(ResourceLeng.ASK_TOOLTIP_NASTER));
        //******* Tab OptionAyuda
        this.OptionAyuda.setText(rb.getString(ResourceLeng.TAB_HELP));
        this.BActualizar.setText(rb.getString(ResourceLeng.BUTTON_UPDATE));
        int version = (int) HelloWorld.internalInformation.get("Version");
        this.LCurrentVersion.setText(String.format(rb.getString(ResourceLeng.LABEL_CURRENT_VERSION_INFO), version));
        this.urlWiki = rb.getString(ResourceLeng.WIKI_URL);
        this.HopenHelp.setText(rb.getString(ResourceLeng.WIKI_TEXT));
        this.urlNas = rb.getString(ResourceLeng.NAS_URL);
        this.HnasterHelp.setText(rb.getString(ResourceLeng.NAS_TEXT));
        TCredits.getChildren().clear();
        TCredits.getChildren().add(new Text(rb.getString(ResourceLeng.CREDITS_TEXT)));
        int codeSymbol = 8729;
        char symbol = (char) Character.toLowerCase(codeSymbol);
        Hyperlink link;
        for (Map.Entry<String, String[]> e : ResourceLeng.CREDITS.entrySet()) {
            TCredits.getChildren().add(new Text("\n\t" + symbol + e.getKey()));
            for (int i = 0; i < e.getValue().length; i++) {
                TCredits.getChildren().add(new Text("\n\t\t"));
                link = new Hyperlink(e.getValue()[i]);
                link.setOnMouseClicked(actions -> {
                    credictContact((Hyperlink) actions.getSource());
                });
                link.setBorder(Border.EMPTY);
                TCredits.getChildren().add(link);
            }
        }
    }

    public void testmethod() {
        System.err.println("eeeeeeeeeeeee");
    }
    /**
     * @deprecated
     */
    public void showLabelCheck() {
        System.err.println("show");
        LCheckDate.setVisible(true);
    }
    /**
     * @deprecated
     */
    public void hideLabelCheck() {
        System.err.println("hide");
        LCheckDate.setVisible(false);
        //El usuario lanzo %s (%s) (TR050)
    }

}
