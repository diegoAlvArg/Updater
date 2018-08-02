package aplicacion.controlador;

import Tools.almacen.AlmacenTareas;
import Tools.almacen.InformacionUsuario;
import Tools.lenguaje.ResourceLeng;
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
//import aplicacion.HelloWorld;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import aplicacion.controlador.MainController;
import aplicacion.datos.Tareas;
import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
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
import javafx.util.Duration;

public class TabDeliverController {

    private MainController main;

    @FXML
    private TableView<Tareas> TableDeliverys;// = new TableView<Delivery>();
    @FXML
    private TableColumn<Tareas, String> c1;
    @FXML
    private TableColumn<Tareas, String> c2;
    @FXML
    private TableColumn<Tareas, String> c3;
    @FXML
    private TableColumn<Tareas, String> c4;
    @FXML
    private TableColumn<Tareas, Button> c5;
    private Map<String, Tareas> tareasTrack = new HashMap<>();
    private Map<String, Boolean> updatable = new HashMap<>();
//    private Timeline timeline;

    //---------------------------------------------------FXML---------------------------------------------------
    @FXML
    public void testB() {

        System.err.println(TableDeliverys.getItems().toString());
    }
    
    
    //---------------------------------------------------EVENTO-------------------------------------------------
    private void gestionarEventoFichero(Tareas dataRow) {
        String pathFile = dataRow.getPathFile();
        System.out.println("cell clicked¿" + pathFile + "?");
        if (!pathFile.isEmpty() && new File(pathFile).isFile()) {
            main.getHostService().showDocument(pathFile);
        }else{
            ActionTool.mostrarNotificacion(ResourceLeng.INFO_LEGACY_FILE_TITLE,
                            ResourceLeng.INFO_LEGACY_FILE_TEXT, Duration.seconds(15),
                            NotificationType.WARNING);
        }
//        System.out.println("cell clicked!");
    }

    private void gestionarEventoAccion(Tareas dataRow) {
        System.out.println("cell clicked!");
    }

//    protected void addRow(String curso, String titulo, String fichero, String tiempo, String languague, String nota, String comenario) {
//        addRow(curso, titulo, fichero, tiempo, languague, "");
//    }

    protected void addRow(String curso, String titulo, String fichero, String tiempo, String languague, String nota, String comentario, String url) {
        try {
            Tareas del = new Tareas(curso, titulo, fichero, tiempo, languague, nota, comentario, url);
            Tareas aux;
            if (tareasTrack.containsKey(del.getFuente()) && updatable.get(del.getFuente())) {
                aux = tareasTrack.get(del.getFuente());
                if (!aux.equals(del)) {
                    aux.updateInfo(del);
//                    tareasTrack.replace(del.getFuente(), aux, del);
                    //Es posible que si tenemos la referencia esto no sea necesario
                    // por si acaso
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
    

    //---------------------------------------------------UTILS-------------------------------------------------- 
    protected void setLanguague(ResourceBundle rb) {
        //******* Tab OpcionTareas
        this.c1.setText(rb.getString(ResourceLeng.C1_TEXT));
        this.c2.setText(rb.getString(ResourceLeng.C2_TEXT));
        this.c3.setText(rb.getString(ResourceLeng.C3_TEXT));
        this.c4.setText(rb.getString(ResourceLeng.C4_TEXT));
        this.c5.setText(rb.getString(ResourceLeng.C5_TEXT));

        ConfigControl.setLanguage(rb);
        TableDeliverys.refresh();

    }

    protected void saveData() {
        try {
            if (InformacionUsuario.existenDatos()) {
                AlmacenTareas.guardarDatos(tareasTrack, InformacionUsuario.getUser());
            }
        } catch (NoSuchFieldException ex) {
            // Logger.getLogger(InterfaceController.class.getNombre()).log(Level.SEVERE, null, ex);
        }
    }

    protected void loadData(String key) {
        System.err.println("loading data");
        HashMap<String, Tareas> map = AlmacenTareas.cargarDatos(key);
//        Tareas del;
        ResourceBundle rb = main.getResource();
        if (map != null) {
            tareasTrack = map;

            for (Map.Entry<String, Tareas> entry : tareasTrack.entrySet()) {
                if (tareasTrack.get(entry.getKey()).getEstado().equals("4")) {
                    ActionTool.mostrarNotificacionConParam(rb.getString(ResourceLeng.ERROR_RECOVER_TITLE),
                            String.format(rb.getString(ResourceLeng.ERROR_RECOVER_TEXT),
                                    tareasTrack.get(entry.getKey()).getFuente()), Duration.seconds(15),
                            NotificationType.WARNING);
                } else {
                    TableDeliverys.getItems().add(tareasTrack.get(entry.getKey()));
                }
//                del = tareasTrack.get(entry.getKey());
//                System.err.println(del.toString());
//                TableDeliverys.getItems().add(del);

            }
        }

    }
    
    protected void refresh(){
        TableDeliverys.getColumns().get(2).setVisible(false);
        TableDeliverys.getColumns().get(2).setVisible(true);
    }
    //---------------------------------------------------INIT---------------------------------------------------
    protected void init(MainController mainController) {
        main = mainController;
        initializeTableView();
        loadDummys();
    }
    /**
     *
     */
    private void initializeTableView() {
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
                            case "0":
                                textCell = ConfigControl.STATE_0_TEXT;
                                auxTool = new Tooltip(ConfigControl.STATE_0_TOOL);
                                break;
                            case "1":
                                textCell = ConfigControl.STATE_1_TEXT;
                                auxTool = new Tooltip(ConfigControl.STATE_1_TOOL);
                                break;
                            case "2":
                                textCell = ConfigControl.STATE_2_TEXT;
                                auxTool = new Tooltip(ConfigControl.STATE_2_TOOL);
                                break;
                            case "3":
                                textCell = ConfigControl.STATE_3_TEXT;
                                auxTool = new Tooltip(ConfigControl.STATE_3_TOOL);
                                break;
                            case "4":
                                textCell = ConfigControl.STATE_4_TEXT;
                                auxTool = new Tooltip(ConfigControl.STATE_4_TOOL);
                                break;
                            case "5":
                                textCell = ConfigControl.STATE_5_TEXT;
                                auxTool = new Tooltip(ConfigControl.STATE_5_TOOL);
                                break;
                            case "6":
                                textCell = ConfigControl.STATE_6_TEXT;
                                auxTool = new Tooltip(ConfigControl.STATE_6_TOOL);
                                break;
                            case "7":
                                textCell = ConfigControl.STATE_7_TEXT;
                                auxTool = new Tooltip(ConfigControl.STATE_7_TOOL);
                                break;
                            case "8":
                                textCell = ConfigControl.STATE_8_TEXT;
                                auxTool = new Tooltip(ConfigControl.STATE_8_TOOL);
                                break;
                            default:
                        }

                        super.setTooltip(auxTool);
                        setText(textCell);
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
                            respuesta = null;
                        } else if (diff < 86400000) { // 86.400 1 dia
                            String hms = String.format(ConfigControl.seedTimeNoDays,
                                    TimeUnit.MILLISECONDS.toHours(diff) % 24,
                                    TimeUnit.MILLISECONDS.toMinutes(diff) % 60);//,
//                                TimeUnit.MILLISECONDS.toSeconds(diff) % 60);
                            respuesta = hms;
                        } else {
                            String hms = String.format(ConfigControl.seedTime,
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
                    if (item == null ||  empty) {
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
                                    if(new File(auxString).isFile()){
                                        cellTool = ConfigControl.TABLE_FILE + auxString;
                                        auxString = ".." + auxString.substring(auxString.lastIndexOf("\\"));
                                        cellText = ConfigControl.TABLE_FILE + auxString; 
                                    }else{
                                        cellTool = ConfigControl.TABLE_FILE + auxString;
                                        cellText = ConfigControl.TABLE_FILE + auxString; 
                                    }
                                    
                                }
                            } else if (line.contains("N: ")) {
                                if (auxString != null) {
                                    cellText += "\n";
                                    cellTool += "\n";
                                }
                                auxString = line.replaceAll("N: ", "");
                                if (auxString != null && !auxString.isEmpty()) {
                                    cellTool += ConfigControl.TABLE_NOTE + auxString;
                                    cellText += ConfigControl.TABLE_NOTE + auxString;
                                }
                            } else {

                            }
                        }
                        if(cellText.length() > 1){
                            super.setTooltip(new Tooltip(cellTool));
                            setText(cellText);
                            addEventFilter(MouseEvent.MOUSE_CLICKED, event -> gestionarEventoFichero(TableDeliverys.getSelectionModel().getSelectedItem()));
                        }else{
                            setText(null);
                            setStyle("");
                        }
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
//                        System.err.println("jaj? " + textStatus);
                        switch (textStatus) {
                            case "0":
                            case "5":
                                textCell = ConfigControl.TBUTTON_05_TEXT;
                                auxTool = new Tooltip(ConfigControl.TBUTTON_05_TOOL);
                                break;
                            case "1":
                            case "2":
                            case "6":
                            case "7":
                                textCell = ConfigControl.TBUTTON_1267_TEXT;
                                auxTool = new Tooltip(ConfigControl.TBUTTON_1267_TOOL);
                                break;
                            case "3":
                            case "8":
                                textCell = ConfigControl.TBUTTON_38_TEXT;
                                auxTool = new Tooltip(ConfigControl.TBUTTON_38_TEXT);
                                break;
                            case "4":
                                textCell = ConfigControl.TBUTTON_4_TEXT;
                                auxTool = new Tooltip(ConfigControl.TBUTTON_4_TOOL);
                                break;
                            default:
                        }
                        item.setText(textCell);
                        item.setTooltip(auxTool);
                        super.setGraphic(item);
                        addEventFilter(MouseEvent.MOUSE_CLICKED, event -> gestionarEventoAccion(TableDeliverys.getSelectionModel().getSelectedItem()));
                    }
                }
            };
        });
        
        
//        c1.setStyle("-fx-background-color: transparent;");
    }
    public void loadDummys() {
        addRow("Proyecto Software (2017-2018)", "==> A. Documentación INDIVIDUAL - SEPTIEMBRE-18", "", "Monday, 10 September 2018, 4:00 PM", "en", "", "", "https://moodle2.unizar.es/add/mod/assign/view.php?id=1148926");
        addRow("Proyecto Software (2017-2018)", "==> B1. Fuentes EQUIPO - SEPTIEMBRE-18", "", "Monday, 10 de September de 2018, 16:00", "es", "", "", "https://moodle2.unizar.es/add/mod/assign/view.php?id=1148927");
        addRow("Bases de datos", "practica 5", "C:\\demo\\TestB.pdf", "Monday, 17 September 2018, 6:00 PM", "en", "9", "Buena practica", "");
        addRow("Bases de datos", "practica 0", "TestB.pdf", "Monday, 3 September 2018, 1:00 PM", "en", "9", "", "");
    }
    public void loadUpdatable() {
        if (!tareasTrack.isEmpty()) {
            for (Map.Entry<String, Tareas> entry : tareasTrack.entrySet()) {
                updatable.put(entry.getKey(), Boolean.TRUE);
            }
        }
    }
    
    
    
    //---------------------------------------------------FXML---------------------------------------------------
    //---------------------------------------------------EVENTO-------------------------------------------------  
    //---------------------------------------------------UTILS-------------------------------------------------- 
    //---------------------------------------------------INIT---------------------------------------------------
}
