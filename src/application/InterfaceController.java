/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import Tools.userDates.UserInfo;
import Updater.tools.ActionTool;
import Updater.tools.NotificationType;
import Updater.tools.ResourceLeng;
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
import application.events.eventUser;
import application.events.procesoSyncronizacion;
import javafx.scene.control.CheckBoxTreeItem;
import wrapper.tree.TypeNode;

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
    private final int MAX_SBCLU = 2;
    private int numSyncro;
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

    //***************************************** OptionAyuda
    @FXML
    private Tab OptionAyuda;
    @FXML
    private Button BActualizar;
    @FXML
    private Label LCurrentVersion;
    @FXML
    private Hyperlink HopenHelp;
    private String url;
    @FXML
    private TextFlow TCredits;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        ResourceBundle auxRb;
        if (rb != null) {
//            System.out.println("RB no es null");
            auxRb = rb;
        } else {
            auxRb = HelloWorld.getResource();
//            System.out.println("RB ES null");
        }
        setLanguague(auxRb);

        initializeSpinners();
        if (!UserInfo.dataExits()) {
            // No hay usuario
            initializationUserLoad(false, "", "");
        } else {
            initializationUserLoad(true, UserInfo.getUser(), UserInfo.getPath());
            setNextUpdate(null);

            TreeItem<BookCategory> rootItem = new TreeItem<BookCategory>();
            TListUpdates.setRoot(rootItem);
//            TListUpdates.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateSelectedItem((TreeItem) newValue));
//            addTreeItem("JAVA-011.aa", "Spring", TypeNode.FORO);
//            addTreeItem("JAVA-011.aa", "caca", TypeNode.FORO);
//            TListUpdates.getRoot().getChildren().clear();
//            addTreeItem("JAVA-011", "Spring2", TypeNode.FORO);
//            addTreeItem("JAVA-011", "caca2", TypeNode.FORO);


//            TreeItem<BookCategory> rootItem = new TreeItem<BookCategory>();
//            // Root Item
//            BookCategory catJava = new BookCategory("JAVA-00", "Java");
//            TreeItem<BookCategory> itemCat = new TreeItem<BookCategory>(catJava);
//            rootItem.setExpanded(true);
//
//            // JSP Item
//            BookCategory catJSP = new BookCategory("JAVA-01", "Jsp");
//            TreeItem<BookCategory> itemJSP = new TreeItem<BookCategory>(catJSP);
//
//            // Spring Item
//            BookCategory catSpring = new BookCategory("JAVA-011", "Spring");
//            TreeItem<BookCategory> itemSpring = new TreeItem<>(catSpring);
//
//            // Add to Root
//            rootItem.getChildren().addAll(itemCat);//, itemJSP, itemSpring);
//            itemCat.getChildren().addAll(itemJSP, itemJSP);
//            TListUpdates.setRoot(rootItem);
        }
    }
    
    private void initializeTreeView(){
        TreeItem<BookCategory> rootItem = new TreeItem<BookCategory>();
        TListUpdates.setRoot(rootItem);     
        TListUpdates.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> handleTreeViewClick((TreeItem) newValue));
    }
    public synchronized void addTreeItem(String path, String name, TypeNode tipo) {
        URL iconUrl = null;
        Image miImage = null;
        // Averiguaar donde meter  le nuevo elemento
        String curso = path.replace(LPathApplication.getText() + File.separator, "");
        curso = curso.substring(0, curso.indexOf(File.separator));
        TreeItem<BookCategory> auxCurso;
        if(cursosTrack.containsKey(curso)){
            // Existe, lo pedimos
            auxCurso = cursosTrack.get(curso);
        }else{
            // No existe, lo creamos y metemos
            iconUrl = this.getClass().getResource("/Resources/Icons/folder.png");
            try (InputStream op = iconUrl.openStream()) {
                miImage = new Image(op);
            } catch (IOException ex) {
                Logger.getLogger(InterfaceController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            BookCategory auxbook = new BookCategory(LPathApplication.getText() + 
                    File.separator + curso, curso);
            auxCurso = new TreeItem<>(auxbook, new ImageView(miImage));
            cursosTrack.put(curso, auxCurso); 
            TListUpdates.getRoot().getChildren().add(auxCurso);
        }
        //  Creacion de un elemento para el arbol
        //  Creacion de una Imagen (icono) representativa del tipo
        int indexExt = path.lastIndexOf(".");
        String auxExtension = "";
        if(indexExt > 0){
            auxExtension = path.substring(indexExt);
        }
        switch(auxExtension){
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
                Logger.getLogger(InterfaceController.class
                        .getName()).log(Level.SEVERE, null, ex);
        }
        BookCategory auxbook = new BookCategory(path, name);
        TreeItem<BookCategory> auxItem = new TreeItem<>(auxbook, new ImageView(miImage));
        auxCurso.getChildren().add(auxItem);
        //Una vez con el elemento creado debemos averiguar donde meterlo
        
//        auxItem.getChildren().add(auxItem);
        
//        cursosTrack.get("aa").getChildren().a
//        TListUpdates.getRoot().getChildren().add(auxItem);
    }
    private Map<String, TreeItem<BookCategory>> cursosTrack;
    private void handleTreeViewClick(TreeItem newValue) {
        BookCategory aux = (BookCategory) newValue.getValue();
        System.out.println(aux.print());
//        HelloWorld.getHostService().showDocument("C:\\demo");
    }
    private void cleanTreeview(){
        TListUpdates.getRoot().getChildren().clear();
    }
    //*********************OptionConfig********************************************
    public void changeLanguague(ActionEvent _event) {
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
//                System.err.println("Lenguaje desconocido");
                // No deberia saltar, meter algo en el log por si acaso.
            } else if (auxLocale.equals(rb.getLocale())) {
//                System.err.println("Es el mismo");
                //De normal el comboBox no lazaa un evento al elegir el que ya 
                //  estaba selecionado. Sin embargo cuando cambiamos el idioma y 
                //  hacemos selectValue para el idioma (en su idioma) esto genera
                //  un evento que es esta seccion.
            } else {
//                System.err.println("Es diferente");
                auxRb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", auxLocale);
                HelloWorld.changeTitle(auxRb.getString(ResourceLeng.APP_TITLE));
                HelloWorld.setResource(auxRb);
                setLanguague(auxRb);
            }
        }
    }

    private void initializeSpinners() {
        SpinnerValueFactory.IntegerSpinnerValueFactory horasFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 24, 0);
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
//                int current = (int) minutesSpinner.getValue();
                if (!string.isEmpty() && string.chars().allMatch(Character::isDigit)) {
                    respuesta = Integer.valueOf(string);
//                    System.err.println("New valor: " + respuesta);
//                    System.err.println("Current valor: " + current);
                }
                return respuesta;
            }
        });
        this.minutesSpinner.setValueFactory(minutosFactory);
        this.minutesSpinner.setEditable(true);

        hourSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
//            System.out.println("1-" + obs.toString());
//            System.out.println("2-" + oldValue.toString());
            if (newValue.compareTo("0") <= 0) {
                if ((int) minutesSpinner.getValue() < 5) {
                    minutesSpinner.getValueFactory().setValue(5);
                }
                hourSpinner.getValueFactory().setValue(0);
            }
        });

        minutesSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
//            System.out.println("1-" + obs.toString());
//            System.out.println("2-" + oldValue.toString());
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
    private void initializationUserLoad(boolean user, String userId, String path) {
        if (!user) {
            TTabpane.getSelectionModel().select(OptionConfig);
        } else {
            // Carga de usuario correcta
            URL iconUrl = this.getClass().getResource("/Resources/Icons/User_Ok.png");

            try (InputStream op = iconUrl.openStream()) {
                IUserIcon.setImage(new Image(op));
//                IUserIcon.setImage(new Image(iconUrl.openStream()));

            } catch (IOException ex) {
                Logger.getLogger(InterfaceController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            LIdUser.setText(userId);
            LPathApplication.setText(path);
            initializeTreeView();
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
        this.BActualizar.setDisable(!user);
        BActualizar.setVisible(user);
        LCheckDate.setVisible(user);
    }
   
    public void createNewUser(ActionEvent event) {
        eventUser service = new eventUser();
        service.setRb(HelloWorld.getResource());
        service.setIu(this);
        service.setAskPath(true);
        service.start();
        BNewUser.setDisable(true);
    }
    public void editUser(ActionEvent event) {
        eventUser service = new eventUser();
        service.setRb(HelloWorld.getResource());
        service.setIu(this);
        service.setUser(UserInfo.getUser());
        service.setPass1(UserInfo.getPass1());
        service.setPass2(UserInfo.getPass2());
        service.setAskPath(false);

        service.start();
        if (freqSecuence != null) {
            freqSecuence.pause();
        }
        BEditUser.setDisable(true);
    }
    public void setUserInfo(List<String> dates, boolean isnew) {
        if (dates != null && isnew) {
            UserInfo.createFile(dates.get(0), dates.get(1), dates.get(2), dates.get(3));
            initializationUserLoad(true, dates.get(0), dates.get(3));
        } else if (dates != null) {
            UserInfo.createFile(dates.get(0), dates.get(1), dates.get(2), UserInfo.getPath());
            LIdUser.setText(dates.get(0));
        }

        BEditUser.setDisable(false);
        BNewUser.setDisable(false);
        if (freqSecuence != null) {
            resumeUpdate();
        } else {
            setNextUpdate(null);
        }
    }

    public void setNextUpdate(ActionEvent event) {
        if (freqSecuence != null) {
            freqSecuence.stop();
            freqSecuence = null;
        }
        int minutos = (int) minutesSpinner.getValue();
        int horas = (int) hourSpinner.getValue();
//        int segundos = 0;
        momentoSigAct = Calendar.getInstance();
        Calendar momentoActual = (Calendar) momentoSigAct.clone();
        momentoSigAct.add(Calendar.MINUTE, minutos);
        momentoSigAct.add(Calendar.HOUR_OF_DAY, horas); // adds hour
        momentoSigAct.set(Calendar.SECOND, 0);

        String dayTime;
        ResourceBundle rb = HelloWorld.getResource();
        if (momentoActual.get(Calendar.DAY_OF_MONTH) == momentoSigAct.get(Calendar.DAY_OF_MONTH)) {
            dayTime = rb.getString(ResourceLeng.DAY_TODAY);
        } else {
            dayTime = rb.getString(ResourceLeng.DAY_TOMORROW);
        }
        String line = rb.getString(ResourceLeng.NEXT_TIME_SEED);
        line = String.format(line, dayTime);//, momentoSigAct.get(Calendar.HOUR_OF_DAY), momentoSigAct.get(Calendar.MINUTE));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        line += sdf.format(momentoSigAct.getTime());
        LTimeUpdate.setText(line);
        long diff = momentoSigAct.getTime().getTime() - momentoActual.getTime().getTime();
        freqSecuence = new Timeline(new KeyFrame(
                Duration.seconds(diff / 1000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
//                System.out.println("this is called every 5 seconds on UI thread");
                syncroStart();
            }
        }));

        freqSecuence.setCycleCount(1);
        freqSecuence.play();
    }
    private void resumeUpdate() {
//        System.err.println("ajustando tiempo");
        Calendar momentoActual = Calendar.getInstance();
        if (momentoActual.after(momentoSigAct)) {
            System.err.println("El arroz que se pasa");
            // Paso el momento de la alarma, sincronizamos ya
            syncroStart();
        } else {
            System.err.println("ajustando tiempo");
            // Calcular cuanto tiempo estuvimos parados y poner nueva
            freqSecuence.stop();
            long diff = momentoSigAct.getTime().getTime() - momentoActual.getTime().getTime();
            freqSecuence = new Timeline(new KeyFrame(
                    Duration.seconds(diff / 1000), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("this is called every 5 seconds on UI thread");
                    syncroStart();
                }
            }));

            freqSecuence.setCycleCount(1);
            freqSecuence.play();
        }
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

    }

    public void syncroNow(ActionEvent event) {
        freqSecuence.stop();
        if(numSyncro == MAX_SBCLU){
            cleanTreeview();
            numSyncro = 0;
        }else{
            numSyncro++;
        }
        syncroStart();
    }
    private void syncroStart() {
        BConfirm.setDisable(true);
        BUpdate.setDisable(true);
        BNewUser.setDisable(true);
        BEditPath.setDisable(true);
        BEditUser.setDisable(true);
        BActualizar.setDisable(true);

        LTimeUpdate.setText(HelloWorld.getResource().getString(ResourceLeng.SYNCRO_NOW));
//        HelloWorld.hideApp();
        new procesoSyncronizacion(UserInfo.getUser(), UserInfo.getPass1(),
                UserInfo.getPass2(), UserInfo.getPath(),
                HelloWorld.getResource(), this);
    }
    public void syncroEnd() {
        setNextUpdate(null);
        BConfirm.setDisable(false);
        BUpdate.setDisable(false);
        BNewUser.setDisable(false);
        BEditPath.setDisable(false);
        BEditUser.setDisable(false);
        BActualizar.setDisable(false);
//        HelloWorld.showApp();
    }

    public void chooseDirectory(ActionEvent event) {
        File selectedFile = null;
        ResourceBundle rb = HelloWorld.getResource();
        String initialPath = UserInfo.getPath();
        do {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(initialPath));
            selectedFile = directoryChooser.showDialog(null);
            if (validator.checkPermissions(selectedFile.getAbsolutePath())) {
                initialPath = selectedFile.getAbsolutePath();
                break;
            } else {
                Platform.runLater(() -> Platform.runLater(()
                        -> ActionTool.showNotification(rb.getString(ResourceLeng.MESSAGE_TITLE_PATH_REJECT),
                                rb.getString(ResourceLeng.MESSAGE_INFO_PATH_REJECT),
                                Duration.seconds(15), NotificationType.ERROR)));
            }

        } while (selectedFile != null);
        LPathApplication.setText(initialPath);
        UserInfo.setPath(initialPath);
    }

    //*********************OptionAyuda*********************************************
    public void actualizarVersion(ActionEvent event) {
        HelloWorld.actualizarVersion(true);
    }

    public void credictContact(Hyperlink _text) {
        String idioma = HelloWorld.getResource().getLocale().getLanguage().toUpperCase();
//        System.err.println("mailto:" + _text.getText() + "?Subject=["+idioma+"] ");
        HelloWorld.getHostService().showDocument("mailto:" + _text.getText() + "?Subject=[" + idioma + "] ");
        _text.setVisited(false);
    }

    public void openHelp() {
        HelloWorld.getHostService().showDocument(this.url);
    }

//******************************** Utils*******************************************
    private void setLanguague(ResourceBundle rb) {
        //******* Tab OptionConfig
        this.OptionConfig.setText(rb.getString(ResourceLeng.TAB_CONFIG));
        this.LLanguague.setText(rb.getString(ResourceLeng.LANGUAGE));
//        this.CLanguague = new ComboBox();
        this.CLanguague.getItems().clear();
        for (Map.Entry<String, String> e : ResourceLeng.LANGUAGES.entrySet()) {
            CLanguague.getItems().add(rb.getString(e.getValue()));
        }
        String idioma = ResourceLeng.LANGUAGES.get(rb.getLocale().getLanguage());
        CLanguague.setValue(rb.getString(idioma));
        LHours.setText(rb.getString(ResourceLeng.TIME_HOUR_TEXT));
        LMinutes.setText(rb.getString(ResourceLeng.TIME_MINUT_TEXT));
        BConfirm.setText(rb.getString(ResourceLeng.TIME_BUTTON_TEXT));
        LFeqSinc.setText(rb.getString(ResourceLeng.TIME_LABEL));
        LPathDownload.setText(rb.getString(ResourceLeng.LABEL_PATH_DOWNLOAD));
        LNextUpdate.setText(rb.getString(ResourceLeng.LABEL_NEXT_UPDATE));
        BActualizar.setText(rb.getString(ResourceLeng.BUTTON_UPDATE_MOODLE));
//        LCheckDate.setText(rb.getString(ResourceLeng.LABEL_CHECK_DATES));
        //******* Tab OptionAyuda
        this.OptionAyuda.setText(rb.getString(ResourceLeng.TAB_HELP));
        this.BActualizar.setText(rb.getString(ResourceLeng.BUTTON_UPDATE));
        int version = (int) HelloWorld.internalInformation.get("Version");
        this.LCurrentVersion.setText(String.format(rb.getString(ResourceLeng.LABEL_CURRENT_VERSION_INFO), version));
        this.url = rb.getString(ResourceLeng.WIKI_URL);
        this.HopenHelp.setText(rb.getString(ResourceLeng.WIKI_TEXT));
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
}
