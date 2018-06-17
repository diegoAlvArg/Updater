package aplicacion.controlador;

import Tools.logger.LogGeneral;
import Tools.almacen.InformacionUsuario;
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
import Tools.lenguaje.ResourceLeng;
import Tools.almacen.AlmacenTareas;
import aplicacion.datos.ItemArbol;
import aplicacion.datos.Tareas;
import aplicacion.HelloWorld;
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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.layout.Border;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;
import aplicacion.eventos.Validador;
import aplicacion.eventos.EventosUsuario;
import aplicacion.eventos.ProcesoSyncronizacion;
import java.util.HashMap;
import javafx.scene.control.CheckBox;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;
import javafx.animation.Animation;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

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
    private Map<String, TreeItem<ItemArbol>> cursosTrack = new HashMap<>();
    //*************************************** OptionInit
    @FXML
    private Tab OptionDelivery;
    @FXML
    private TableView<Tareas> TableDeliverys;// = new TableView<Delivery>();
    @FXML
    private TableColumn<Tareas, String> c1;
    @FXML
    private TableColumn<Tareas, String> c2;
    private String STATE_1_TEXT;
    private String STATE_1_TOOL;
    private String STATE_2_TEXT;
    private String STATE_2_TOOL;
    private String STATE_3_TEXT;
    private String STATE_3_TOOL;
    private String STATE_4_TEXT;
    private String STATE_4_TOOL;
    private String STATE_5_TEXT;
    private String STATE_5_TOOL;
    private String seedTime;
    private String seedTimeNoDays;
    private Timeline timeline;
    private String TABLE_FILE;
    private String TABLE_NOTE;
    private String TBUTTON_1_TEXT;
    private String TBUTTON_1_TOOL;
    private String TBUTTON_24_TEXT;
    private String TBUTTON_24_TOOL;
    private String TBUTTON_35_TEXT;
    private String TBUTTON_35_TOOL;

    @FXML
    private TableColumn<Tareas, String> c3;
    @FXML
    private TableColumn<Tareas, String> c4;
    @FXML
    private TableColumn<Tareas, Button> c5;
    private Map<String, Tareas> tareasTrack = new HashMap<>();
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
        // Esta comprobacion no deberia ser necesaria, pero por si alguien no 
        //  manipulara correctamente javafx
        if (rb != null) {
            auxRb = rb;
        } else {
            auxRb = HelloWorld.getResource();
        }
        logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_INIT_LOAD_CONTROL));
        logRegistro.setSourceClassName(this.getClass().getName());
        LogGeneral.log(logRegistro);

        setLanguague(auxRb);
        initializeSpinners();

        if (!InformacionUsuario.existenDatos()) {
            // No hay usuario
            initializationUserLoad(false, "", "");

            logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_NO));
        } else {
            try {
                initializationUserLoad(true, InformacionUsuario.getUser(), InformacionUsuario.getPath());
                loadData(InformacionUsuario.getUser());
                setNextUpdate();

                TreeItem<ItemArbol> rootItem = new TreeItem<ItemArbol>();
                CBNaster.setSelected(InformacionUsuario.getUseNas());
                TListUpdates.setRoot(rootItem);
                logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_OK));
            } catch (NoSuchFieldException e) {
                initializationUserLoad(false, "", "");
                logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_LOST));
            }
        }
        if (logRegistro != null) {
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);

        }
        HelloWorld.setMetodosControl(this, "actualizarVesionEnd", "saveData");
        initializeTableView();

//        TableDeliverys.setItems(loadDummy());
//    
    }

    //*********************OptionInit**********************************************
    /**
     * Inicializa la treeView y le aniade un listener. Esto se podria hacer con
     * el Scene builder o ha pelo pero no se.
     */
    private void initializeTreeView() {
        TreeItem<ItemArbol> rootItem = new TreeItem<ItemArbol>();
        TListUpdates.setRoot(rootItem);
        TListUpdates.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> handleTreeViewClick((TreeItem) newValue));
    }

    /**
     * Handle que tratara las acciones sobre el treeView, hara que el local
     * trate el path asociado al item que tratamos
     *
     * @param newValue treeItem que disparo el evento
     */
    private void handleTreeViewClick(TreeItem newValue) {
        ItemArbol aux = null;
        try {
            aux = (ItemArbol) newValue.getValue();
            HelloWorld.getHostService().showDocument(aux.getPathFichero());
        } catch (Exception e) {
            LogRecord logRegistro;
            StringWriter errors = new StringWriter();
            String auxWho = HelloWorld.getResource().getString(ResourceLeng.TRACE_TREE_ERROR);

            e.printStackTrace(new PrintWriter(errors));
            auxWho = String.format(auxWho, aux.print());
            logRegistro = new LogRecord(Level.SEVERE, auxWho + "\n" + errors.toString());
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
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

    /**
     * Aniadira un nuevo item al treeView. Aniadienssolselo como "hijo" a un
     * nodo que lo represente, creando este si fuera necesario.
     *
     * @param path path del recurso que representa el item
     * @param name nombre con el que se representara
     * @param tipo tipo del item, para asignarle un icono
     */
    public synchronized void addTreeItem(String path, String name) {
        URL iconUrl = null;
        Image miImage = null;
        // Averiguaar donde meter  le nuevo elemento
        String curso = path.replace(LPathApplication.getText() + File.separator, "");
        curso = curso.substring(0, curso.indexOf(File.separator));
        TreeItem<ItemArbol> auxCurso;
        if (cursosTrack.containsKey(curso)) {
            // Existe, lo pedimos
            auxCurso = cursosTrack.get(curso);
        } else {
            // No existe, lo creamos y metemos
            iconUrl = this.getClass().getResource("/Resources/Icons/folder.png");
            try (InputStream op = iconUrl.openStream()) {
                miImage = new Image(op);
            } catch (IOException ex) {
//                Logger.getLogger(InterfaceController.class
//                        .getNombre()).log(Level.SEVERE, null, ex);
            }
            ItemArbol auxbook = new ItemArbol(LPathApplication.getText()
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
//                    .getNombre()).log(Level.SEVERE, null, ex);
        }
        ItemArbol auxbook = new ItemArbol(path, name);
        TreeItem<ItemArbol> auxItem = new TreeItem<>(auxbook, new ImageView(miImage));

        // En el caso de que un TreeItem<BookCategory> ya este aniadido lo eliminamos
        for (TreeItem item : auxCurso.getChildren()) {
            if (item.getValue().equals(auxItem.getValue())) {
                auxCurso.getChildren().remove(item);
                break;
            }
        }
        auxCurso.getChildren().add(0, auxItem);
    }

    //*********************OptionDelivery*******************************************
    /**
     *
     */
    public void initializeTableView() {
        // Por debajo parece tener 3 punteros hacia elementos de la lista, de forma 
        //  que cuando refrescas la tabla el elemento que ocupa la posicion mas alta
        //  salta 4 veces su refresco 1 normal y los 3 aniadidos.
        // Son 3 refrescos, no deberia haber problema; sino habira que investigar 
        //  el solo refrescar solo la "tabla"

        c1.setCellValueFactory(new PropertyValueFactory<Tareas, String>("fuente"));
        c1.setCellFactory(c -> {
            return new TableCell<Tareas, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        super.setTooltip(new Tooltip(item));
                        setText(item);
                    }
                }
            };
        });

        c2.setCellValueFactory(new PropertyValueFactory<Tareas, String>("estado"));
        c2.setCellFactory(c -> {
            return new TableCell<Tareas, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
//                        System.err.println("jaja?");
                        Tooltip auxTool = null;
                        String textCell = null;
                        switch (item) {
                            case "1":
                                textCell = STATE_1_TEXT;
                                auxTool = new Tooltip(STATE_1_TOOL);
                                break;
                            case "2":
                            case "4":
                                textCell = STATE_2_TEXT;
                                auxTool = new Tooltip(STATE_2_TOOL);
                                break;
                            case "3":
                            case "5":
                                textCell = STATE_3_TEXT;
                                auxTool = new Tooltip(STATE_3_TOOL);
                                break;
                            default:
                        }

                        super.setTooltip(auxTool);
                        setText(textCell);
//                        System.out.println(item);
                    }
                }
            };
        });

        //5 eventos por seg
        c3.setCellValueFactory(new Callback<CellDataFeatures<Tareas, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<Tareas, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                String respuesta = "";
                String aux;// = p.getValue().getTiempo();
                if (p != null) {
                    aux = p.getValue().getTiempo();
                    if (aux != null) {
                        long diff = Long.valueOf(aux);
                        if (diff <= 0) {
                            respuesta = " ";
                        } else if (diff < 86400000) {
                            String hms = String.format(seedTimeNoDays,
                                    TimeUnit.MILLISECONDS.toHours(diff) % 24,
                                    TimeUnit.MILLISECONDS.toMinutes(diff) % 60);//,
//                                TimeUnit.MILLISECONDS.toSeconds(diff) % 60);
                            respuesta = hms;
                        } else {
                            String hms = String.format(seedTime,
                                    TimeUnit.MILLISECONDS.toDays(diff),
                                    TimeUnit.MILLISECONDS.toHours(diff) % 24,
                                    TimeUnit.MILLISECONDS.toMinutes(diff) % 60);//,
//                                TimeUnit.MILLISECONDS.toSeconds(diff) % 60);
                            respuesta = hms;
                        }
                        return new ReadOnlyStringWrapper(respuesta);
                    }
                }
                return null;
            }
        });
        c3.setCellFactory(c -> {
            return new TableCell<Tareas, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        super.setTooltip(new Tooltip("hh:mm"));
                        setText(item);
                    }
                }
            };
        });
//        c4.setCellValueFactory(new PropertyValueFactory<Delivery, String>("info"));
        c4.setCellValueFactory(new Callback<CellDataFeatures<Tareas, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<Tareas, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                String respuesta = "";
                String aux = p.getValue().getInfo();

                if (aux != null) {

                    return new ReadOnlyStringWrapper(aux);
                }
                return null;
            }
        });
        c4.setCellFactory(c -> {
            return new TableCell<Tareas, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String[] listInfo = item.split("/n");
                        String cellText = "";
                        String cellTool = "";
                        String auxString = null;

                        for (String line : listInfo) {
                            if (line.contains("S: ")) {
                                auxString = line.replaceAll("S: ", "");
                                if (auxString != null && !auxString.isEmpty()) {
                                    cellTool = TABLE_FILE + auxString;
                                    auxString = ".." + auxString.substring(auxString.lastIndexOf("//"));
                                    cellText = TABLE_FILE + auxString;
                                }
                            } else if (line.contains("N: ")) {
                                if (auxString != null) {
                                    cellText += "\n";
                                    cellTool += "\n";
                                }
                                auxString = line.replaceAll("N: ", "");
                                if (auxString != null && !auxString.isEmpty()) {
                                    cellTool += TABLE_NOTE + auxString;
                                    cellText += TABLE_NOTE + auxString;
                                }
                            } else {

                            }
                        }

                        super.setTooltip(new Tooltip(cellTool));
                        setText(cellText);
                        addEventFilter(MouseEvent.MOUSE_CLICKED, event -> eventColumn(TableDeliverys.getSelectionModel().getSelectedItem()));
                    }
                }
            };
        });

        c5.setCellValueFactory(new Callback<CellDataFeatures<Tareas, Button>, ObservableValue<Button>>() {
            public ObservableValue<Button> call(CellDataFeatures<Tareas, Button> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                String aux = p.getValue().getEstado();
                if (aux != null) {

                    return new ReadOnlyObjectWrapper(new Button(aux));
                }
                return null;
            }
        });
        c5.setCellFactory(c -> {
            return new TableCell<Tareas, Button>() {
                @Override
                protected void updateItem(Button item, boolean empty) {
//                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
//                        System.err.println("jaja?");
                        Tooltip auxTool = null;
                        String textStatus = item.getText();
                        String textCell = null;
                        System.err.println("jaj? " + textStatus);
                        switch (textStatus) {
                            case "1":
                                textCell = TBUTTON_1_TEXT;
                                auxTool = new Tooltip(TBUTTON_1_TOOL);
                                break;
                            case "2":
                            case "4":
                                textCell = TBUTTON_24_TEXT;
                                auxTool = new Tooltip(TBUTTON_24_TOOL);
                                break;
                            case "3":
                            case "5":
                                textCell = TBUTTON_35_TEXT;
                                auxTool = new Tooltip(TBUTTON_35_TOOL);
                                break;
                            default:
                        }
                        item.setText(textCell);
//                        System.err.println("jaja? " + item.getText());
                        item.setTooltip(auxTool);
//                        item.setVisible(true);
                        super.setGraphic(item);
                        addEventFilter(MouseEvent.MOUSE_CLICKED, event -> eventColumnAction(TableDeliverys.getSelectionModel().getSelectedItem()));
                    }
                }
            };
        });
        loadDummys();

        timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(60000), //60 seg 60000
                        event -> {
                            TableDeliverys.getColumns().get(2).setVisible(false);
                            TableDeliverys.getColumns().get(2).setVisible(true);
//                            TableDeliverys.refresh();
                        }
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        System.err.println("---Setters");

    }

    public void eventColumn(Tareas dataRow) {
        String pathFile = dataRow.getPathFile();
        if (!pathFile.isEmpty()) {
            HelloWorld.getHostService().showDocument(pathFile);
        }
//        System.out.println("cell clicked!");
    }

    public void eventColumnAction(Tareas dataRow) {
        System.out.println("cell clicked!");
    }

    public void loadDummys() {
//        addRow("Bases de datos", "practica 5", 1, "C://demo//TestB.pdf", "Friday, 22 June 2018, 19:30 AM", "en", "7");
//        addRow("Bases de datos", "practica 4", 1, "", "Friday, 22 June 2018, 19:30 AM", "en");
//        addRow("Bases de datos", "practica 5", 1, "C://demo//TestB.pdf", "Friday, 22 June 2018, 19:30 AM", "en", "9");
    }

    public void addRow(String curso, String nombre, int estado, String fichero, String tiempo, String languague) {
        addRow(curso, nombre, estado, fichero, tiempo, languague, "");
    }

    public void addRow(String curso, String nombre, int estado, String fichero, String tiempo, String languague, String nota) {
        try {
            Tareas del = new Tareas(curso, nombre, estado, fichero, tiempo, languague, nota);
            Tareas aux;
            if (tareasTrack.containsKey(del.getFuente())) {
                aux = tareasTrack.get(del.getFuente());
                if (!aux.equals(del)) {
                    tareasTrack.replace(del.getFuente(), aux, del);
                    TableDeliverys.getItems().remove(aux);
                    TableDeliverys.getItems().add(del);
                }
            } else {
                tareasTrack.put(del.getFuente(), del);
                TableDeliverys.getItems().add(del);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void testB() {

        System.err.println(TableDeliverys.getItems().toString());
    }

    //*********************OptionConfig********************************************
    /**
     * Tratara el evento generado por el usuario en relacion a selecionar un
     * idioma para la App
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
                HelloWorld.cambiarTitulo(auxRb.getString(ResourceLeng.APP_TITLE));
                HelloWorld.setResource(auxRb);
                setLanguague(auxRb);
            }
        }
        if (logRegistro != null) {
            LogGeneral.log(logRegistro);
        }
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
            TTabpane.getSelectionModel().select(OptionConfig);
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
            initializeTreeView();
            HelloWorld.anidirOpcionSysTray(this, "openHelp", ResourceLeng.SYS_TRAY_WIKI);
            HelloWorld.anidirOpcionSysTray(this, "actualizarVersion", ResourceLeng.SYS_TRAY_UPDATE);
            HelloWorld.anidirOpcionSysTray(this, "syncroNow", ResourceLeng.SYS_TRAY_SYNCRO);
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
     * "perfil" de usuario.
     *
     * @param event
     */
    public void createNewUser(ActionEvent event) {
        ResourceBundle rb = HelloWorld.getResource();
        LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_EVENT_USER_NEW));
        logRegistro.setSourceClassName(this.getClass().getName());
        LogGeneral.log(logRegistro);

        new EventosUsuario("", "", "", false, rb, true, this);
        BNewUser.setDisable(true);
    }

    /**
     * Tratara el evento generado por el usuario en relacion a editar un "
     * "perfil" de usuario. Dicho evento pondra en pausa el Timer para la
     * siguiente actualizacion.
     *
     * @param event
     */
    public void editUser(ActionEvent event) {
        try {
            ResourceBundle rb = HelloWorld.getResource();
            LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_EVENT_USER_EDIT));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);

            new EventosUsuario(InformacionUsuario.getUser(), InformacionUsuario.getPass1(), InformacionUsuario.getPass2(),
                    InformacionUsuario.getUseNas(), rb, false, this);
            if (freqSecuence != null) {
                freqSecuence.pause();
            }
            BEditUser.setDisable(true);
        } catch (NoSuchFieldException e) {
            wrongDates();
        }
    }

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
            } else if (dates != null) {
                InformacionUsuario.crearFichero(dates.get(0), dates.get(1), dates.get(2), InformacionUsuario.getPath(), String.valueOf(dates.get(4)));
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

            LogRecord logRegistro = new LogRecord(Level.INFO, HelloWorld.getResource().getString(ResourceLeng.TRACE_EVENT_USER_END));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
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
                LogGeneral.log(logRegistro);
                syncroStart();
            }
        }));

        freqSecuence.setCycleCount(1);
        freqSecuence.play();
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
            LogRecord logRegistro = new LogRecord(Level.SEVERE, HelloWorld.getResource().getString(ResourceLeng.TRACE_TIMER_LATE));
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
                    LogRecord logRegistro = new LogRecord(Level.SEVERE, HelloWorld.getResource().getString(ResourceLeng.TRACE_TIMER_END));
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

    /**
     * Metodo que inicia el proceso de sincronizacion, no asociado al Timer;
     * esto implicara para el Timer.
     */
    public void syncroNow() {
        freqSecuence.stop();
        syncroStart();
    }

    /**
     * Metodo que iniciara el proceso de sincronizacion. Limpiara el TreeView si
     * es la X vez que lanzamos la sincronizaion.
     *
     * Adenas desactivara las interacciones que puedan lanzar este proceso como
     * los botones de la App o la opcion en el Systray (si lo hubiera)
     */
    private void syncroStart() {
        HelloWorld.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_SYNCRO, false);
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
        try {
            new ProcesoSyncronizacion(InformacionUsuario.getUser(), InformacionUsuario.getPass1(),
                    InformacionUsuario.getPass2(), InformacionUsuario.getPath(),
                    HelloWorld.getResource(), this, CBNaster.isSelected());
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
        BConfirm.setDisable(false);
        BUpdate.setDisable(false);
        BNewUser.setDisable(false);
        BEditPath.setDisable(false);
        BEditUser.setDisable(false);
        BActualizar.setDisable(false);
        HelloWorld.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_SYNCRO, true);
//        HelloWorld.showApp();
    }

    /**
     * Manejara el evento generado por el usuario para seleccionar un nuevo
     * directorio para que funcione la App, iniciando la navegacion en el path
     * actual. Comprobara que el directorio selecionado se tenga permisos e
     * informando en caso contrario; y finalizara guardando el path resultante
     *
     * @param event
     */
    public void chooseDirectory(ActionEvent event) {
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
     * Manejara el evento generado por el usuario para activar/desactivar el uso
     * de Nas-Ter en la sincronizacion. Comprovando que los datos son corectos
     *
     * @param event
     */
    public void useNasTer(ActionEvent event) {
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

    public void wrongDates() {
        InformacionUsuario.deleteFile();
        if (freqSecuence != null) {
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
//                    .getNombre()).log(Level.SEVERE, null, ex);
        }

        ResourceBundle rb = HelloWorld.getResource();
        ActionTool.mostrarNotificacion(rb, ResourceLeng.ERROR_DATA_TITLE, ResourceLeng.ERROR_DATA_TEXT, Duration.seconds(15), NotificationType.ERROR);

        LogRecord logRegistro = new LogRecord(Level.SEVERE, rb.getString(ResourceLeng.TRACE_ERROR_DATES_CORRUPT));
        logRegistro.setSourceClassName(this.getClass().getName());
        LogGeneral.log(logRegistro);
    }

    //*********************OptionAyuda*********************************************
    /**
     *
     * Metodo que genera el evento de actualizar la App
     */
    public void actualizarVersion() {
        BActualizar.setDisable(true);

        HelloWorld.cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_UPDATE, false);
        HelloWorld.actualizarVersion(true);
    }

    public void saveData() {
        try {
            AlmacenTareas.guardarDatos(tareasTrack, InformacionUsuario.getUser());
        } catch (NoSuchFieldException ex) {
            // Logger.getLogger(InterfaceController.class.getNombre()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadData(String key) {

        HashMap<String, Tareas> map = AlmacenTareas.cargarDatos(key);
        Tareas del;
        if (map != null) {
            tareasTrack = map;

            for (Map.Entry<String, Tareas> entry : tareasTrack.entrySet()) {
//                del = tareasTrack.get(entry.getKey());
//                System.err.println(del.toString());
//                TableDeliverys.getItems().add(del);
                TableDeliverys.getItems().add(tareasTrack.get(entry.getKey()));
            }
        }

    }
//
    public void actualizarVesionEnd() {
//        System.err.println("Reactivando....");
        BActualizar.setDisable(false);
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
        //******* Tab OpcionTareas
        this.OptionDelivery.setText(rb.getString(ResourceLeng.TAB_DELIVERY));
        this.c1.setText(rb.getString(ResourceLeng.C1_TEXT));
        STATE_1_TEXT = rb.getString(ResourceLeng.STATE_1_TEXT);
        STATE_1_TOOL = rb.getString(ResourceLeng.STATE_1_TOOL);

        this.c2.setText(rb.getString(ResourceLeng.C2_TEXT));
        STATE_2_TEXT = rb.getString(ResourceLeng.STATE_2_TEXT);
        STATE_2_TOOL = rb.getString(ResourceLeng.STATE_2_TOOL);

        this.c3.setText(rb.getString(ResourceLeng.C3_TEXT));
        STATE_3_TEXT = rb.getString(ResourceLeng.STATE_3_TEXT);
        STATE_3_TOOL = rb.getString(ResourceLeng.STATE_3_TOOL);

        this.c4.setText(rb.getString(ResourceLeng.C4_TEXT));
        STATE_4_TEXT = rb.getString(ResourceLeng.STATE_4_TEXT);
        STATE_4_TOOL = rb.getString(ResourceLeng.STATE_4_TOOL);

        this.c5.setText(rb.getString(ResourceLeng.C5_TEXT));
        STATE_5_TEXT = rb.getString(ResourceLeng.STATE_5_TEXT);
        STATE_5_TOOL = rb.getString(ResourceLeng.STATE_5_TOOL);

        this.seedTime = rb.getString(ResourceLeng.FORMAT_TIME);
        this.seedTimeNoDays = rb.getString(ResourceLeng.FORMAT_TIME_NO_DAYS);
        this.TABLE_FILE = rb.getString(ResourceLeng.INFO_FILE);
        this.TABLE_NOTE = rb.getString(ResourceLeng.INFO_NOTE);

        this.TBUTTON_1_TEXT = rb.getString(ResourceLeng.BUTTON_1_TEXT);
        this.TBUTTON_1_TOOL = rb.getString(ResourceLeng.BUTTON_1_TOOL);
        this.TBUTTON_24_TEXT = rb.getString(ResourceLeng.BUTTON_24_TEXT);
        this.TBUTTON_24_TOOL = rb.getString(ResourceLeng.BUTTON_24_TOOL);
        this.TBUTTON_35_TEXT = rb.getString(ResourceLeng.BUTTON_35_TEXT);
        this.TBUTTON_35_TOOL = rb.getString(ResourceLeng.BUTTON_35_TOOL);

        TableDeliverys.refresh();
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

    /**
     * @deprecated
     */
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
