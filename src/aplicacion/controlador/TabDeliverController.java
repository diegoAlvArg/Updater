package aplicacion.controlador;

//#1 Static import
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
//import aplicacion.controlador.MainController;
import aplicacion.datos.Tareas;
import aplicacion.eventos.EventoTarea;
import tools.almacen.AlmacenTareas;
import tools.almacen.InformacionUsuario;
import tools.lenguaje.ResourceLeng;
//#3 Third party
import com.sun.javafx.scene.control.skin.TableHeaderRow;
//#4 Java
import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.ResourceBundle;
//#5 JavaFx
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.Duration;

/** 633
 * Controlador de la tabla Deliver, en la que hay una tabla en la cual recogemos
 *  y representamos las Tareas 
 * 
 * @author Diego Alvarez 
 */
public class TabDeliverController {

    private MainController main;

    @FXML
    private TableView<Tareas> tablaTareas;// = new TableView<Delivery>();
    @FXML
    private TableColumn<Tareas, String> c1;     //Nombre de la tarea
    @FXML
    private TableColumn<Tareas, String> c2;     // Estadp de la tarea
    @FXML
    private TableColumn<Tareas, String> c3;     // Tiempo restante
    @FXML
    private TableColumn<Tareas, String> c4;     // Fichero asociado & nota
    @FXML
    private TableColumn<Tareas, Button> c5;     // Boton de accion
    private Map<String, Tareas> tareasTrack = new HashMap<>();
    private Map<String, Boolean> updatable = new HashMap<>();
    private HashSet<Tareas> tareasExcedidas=new HashSet<Tareas>();  
    private int DIA_MILLIS = 86400000; // 86.400 1 dia
    private int ARCHIV_CORREGIDO = 2;
    private int ARCHIV_POR_CORREGIR = 7;
    
//    
//    @FXML
//    private Button tbut;
//    @FXML
//    private void testButo(ActionEvent event) {
//        System.err.println("eii");
//        limpiarRastro();
//    }
//    
//    
//    @FXML
//    private Button tbutB;
//    @FXML
//    private void testButoB(ActionEvent event) {
//        System.err.println("jaja");
//        loadDummys();
//    }
//      @FXML
//    private Button tbutC;
//    private static int i = 0;
//    @FXML
//    private void testButoC(ActionEvent event) {
//        System.err.println("button c");
//        if(i == 0){
//            updatable.put("practica 7 - Bases de datos", Boolean.TRUE);
//            i++;
//        }
//        
//        aniadirTarea("Bases de datos", "practica 7", "", "Wednesday, 12 September 2018, 11:56 AM", "en", "6", "Buena practica", "");
//    }
    
    
    //---------------------------------------------------EVENTO-------------------------------------------------
    /**
     * Metodo para gestiona el click en la columna 4
     * 
     * @param dataRow 
     */
    private void gestionarEventoFichero(Tareas dataRow) {
        String pathFile = dataRow.getPathFile();
//        System.out.println("cell clicked¿" + pathFile + "?"); 

        if (!pathFile.isEmpty() && new File(pathFile).isFile()) {
            main.getHostService().showDocument(pathFile);
        } else if (!pathFile.isEmpty()) {
            ActionTool.mostrarNotificacion(ResourceLeng.INFO_LEGACY_FILE_TITLE,
                    ResourceLeng.INFO_LEGACY_FILE_TEXT, Duration.seconds(15),
                    NotificationType.WARNING);
        }
    }
    
    /**
     * Metodo para gestionar el click en la columna 5. En la celda de dicha 
     *  columna guardamos un boton, no "asociado directamente" a una tarea 
     *  (representada en la fila); por lo que para acceder a dicha fila lo
     *  hacemos mediante el index.
     * 
     * @param dataRow 
     */
    private void gestionarEventoAccion(int dataRow) {
//        System.out.println("cell clicked!" + dataRow);
        Tareas aux = tablaTareas.getItems().get(dataRow);
        String status = aux.getEstado();
        if (main.OcuparUsuario()) {
            switch (status) {
                case "1":
                case "2":
                case "6":
                case "7":
                case "9":
                    main.getHostService().showDocument(aux.getUrlWeb());
                    break;
                case "0":
                case "3":
                case "5":
                case "8":
                    try {
                        aux.setEstado("4");
                        tablaTareas.refresh();
                        new EventoTarea(aux, main, InformacionUsuario.getUsuario(), InformacionUsuario.getPassM(),
                                InformacionUsuario.getPath(), main.getResource());
                    } catch (NoSuchFieldException ex) {
                        aux.resetearEstado();
//                        Logger.getLogger(TabDeliverController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                default:
            }

        }
    }
    
    /**
     * Metodo para aniadir una tarea de forma que se aniadira una fila a la 
     * tabla
     * 
     * @param curso curso al que pertenece la entrega
     * @param titulo nombre que se le ha dado a la entrega
     * @param fichero fichero asociado a la entrega
     * @param tiempo fecha limite de la entrega
     * @param languague lenguaje en el que se recogen los datos, para la fecha
     * @param nota calificacion asociada a la tarea
     * @param comentario comentario/feedback asociado a la tarea
     * @param url URL donde encontramos la tarea
     */
    protected void aniadirTarea(String curso, String titulo, String fichero, String tiempo, String languague, String nota, String comentario, String url) {
        try {
            Tareas del = new Tareas(curso, titulo, fichero, tiempo, languague, nota, comentario, url);
            Tareas aux;
            boolean auxB = tareasTrack.containsKey(del.getIdentificador());
//            boolean auxC = updatable.get(del.getIdentificador());
//            boolean auxD = auxB && auxC;
//            auxB = updatable.get(del.getIdentificador());
            if(tareasTrack.containsKey(del.getIdentificador())){
                aux = tareasTrack.get(del.getIdentificador());
                if(updatable.get(del.getIdentificador())){
                    if(!aux.equals(del)){
                        aux.updateInfo(del);
                        tablaTareas.refresh();
                        updatable.put(del.getIdentificador(), Boolean.FALSE);
                    }
                }else{
                    aux.setEstado("9");
                    tablaTareas.refresh();
                }
            }else{
                tareasTrack.put(del.getIdentificador(), del);
                tablaTareas.getItems().add(del);
                updatable.put(del.getIdentificador(), Boolean.FALSE);
            }
        } catch (ParseException e) {
//            e.printStackTrace();
        }
    }

    //---------------------------------------------------UTILS-------------------------------------------------- 
    /**
     * Metodo que cargara las Tareas existentes de una sesion anterior
     * 
     * @param key 
     */
    protected void cargarDatos(String key) {
        HashMap<String, Tareas> map = AlmacenTareas.cargarDatos(key);
        ResourceBundle rb = main.getResource();
        if (map != null) {
            tareasTrack = map;

            for (Map.Entry<String, Tareas> entry : tareasTrack.entrySet()) {
                if (tareasTrack.get(entry.getKey()).getEstado().equals("4")) {
                    ActionTool.mostrarNotificacionConParam(rb.getString(ResourceLeng.ERROR_RECOVER_TITLE),
                            String.format(rb.getString(ResourceLeng.ERROR_RECOVER_TEXT),
                                    tareasTrack.get(entry.getKey()).getIdentificador()), Duration.seconds(15),
                            NotificationType.WARNING);
                    //No lo volvemos a meter en tabla por asegurar su estado
                } else {
                    tablaTareas.getItems().add(tareasTrack.get(entry.getKey()));
                }
            }
        }
    }
    
    /**
     * Metodo que guardara las Tareas existentes de una sesion anterior
     */
    protected void guardarDatos() {
        try {
            if (InformacionUsuario.existenDatos()) {
                AlmacenTareas.guardarDatos(tareasTrack, InformacionUsuario.getUsuario());
            }
        } catch (NoSuchFieldException ex) {
            //Si no existe el perfil del usuario, es indiferente tratar el error
            // porque no hay usuario al que guardarle datos
        }
    }

    protected void refrescar() {
        tablaTareas.getColumns().get(2).setVisible(false);
        tablaTareas.getColumns().get(2).setVisible(true);
        archivarTareas(tareasExcedidas);
    }
    protected void limpiarRastro(){
        tablaTareas.getItems().clear();
        tareasTrack.clear();
        updatable.clear();
    }
    private void archivarTareas(HashSet<Tareas> candidatos){
        String aux;
        long diff;
        String auxEstado;
        //Parece que al hacer clean o renovar tarasExcedidas habia un error que 
        // no referenciaba correctamente. De forma que clonamos la lista y segun
        // la acccion eliminamos de la lista original
        HashSet<Tareas> copyCandidatos = (HashSet<Tareas>) candidatos.clone();
       
        
        
        for(Tareas miTarea: copyCandidatos){
            aux = miTarea.getTiempo();
            diff = Long.valueOf(aux);
            auxEstado = miTarea.getEstado();
            
            if(auxEstado.equals("0") || auxEstado.equals("5")){
                tablaTareas.getItems().remove(miTarea);
                tareasTrack.remove(miTarea.getIdentificador());
                tareasExcedidas.remove(miTarea);
            }else if(auxEstado.equals("1") || auxEstado.equals("6")){
                if(diff <= (DIA_MILLIS * ARCHIV_CORREGIDO)){
                    tablaTareas.getItems().remove(miTarea);
                    tareasTrack.remove(miTarea.getIdentificador());
                    tareasExcedidas.remove(miTarea);
                }
            }else if(auxEstado.equals("2") || auxEstado.equals("7")){
                if(diff <= (DIA_MILLIS * ARCHIV_POR_CORREGIR)){
                    tablaTareas.getItems().remove(miTarea);
                    tareasTrack.remove(miTarea.getIdentificador());
                    tareasExcedidas.remove(miTarea);
                }
            }
        }
        
    }
    
    protected void setLanguague(ResourceBundle rb) {
        //******* Tab OpcionTareas
        this.c1.setText(rb.getString(ResourceLeng.C1_TEXT));
        this.c2.setText(rb.getString(ResourceLeng.C2_TEXT));
        this.c3.setText(rb.getString(ResourceLeng.C3_TEXT));
        this.c4.setText(rb.getString(ResourceLeng.C4_TEXT));
        this.c5.setText(rb.getString(ResourceLeng.C5_TEXT));

        ConfigControl.setLanguage(rb);
        tablaTareas.refresh();

    }
    
    /**
     *
     */
    protected void loadUpdatable() {
        if (!tareasTrack.isEmpty()) {
            for (Map.Entry<String, Tareas> entry : tareasTrack.entrySet()) {
                updatable.put(entry.getKey(), Boolean.TRUE);
            }
        }
    }
    //---------------------------------------------------INIT---------------------------------------------------
    protected void init(MainController mainController) {
        main = mainController;
        initializeTableView();
//        loadDummys();
    }

    /**
     *
     */
    private void initializeTableView() {
        // Por debajo parece tener N punteros hacia elementos de la lista, de forma 
        //  que cuando refrescas la tabla el elemento que ocupa la posicion mas alta
        //  salta N veces su refresco 1 normal y los N por cada columna.
        c1.setCellValueFactory(new Callback<CellDataFeatures<Tareas, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<Tareas, String> p) {
                String aux = p.getValue().getIdentificador();

                if (aux != null) {
                    aux = p.getValue().getEstado() + "::" + aux;
                    return new ReadOnlyStringWrapper(aux);
                }
                return null;
            }
        });
        c1.setCellFactory(c -> {
            return new TableCell<Tareas, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String estado = item.substring(0, item.indexOf("::"));
                        item = item.substring(item.indexOf("::") + 2);
                        if (estado.equals("9")) {
                            setStyle(ConfigControl.styleError);
                        } else {
                            setStyle(ConfigControl.styleNormal);
                        }
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
                        Tooltip auxTool = null;
                        String textCell = null;
                        String styleCell = ConfigControl.styleNormal;
                        switch (item) {
                            case "0":
                                textCell = ConfigControl.state0Text;
                                auxTool = new Tooltip(ConfigControl.state0Tool);
                                break;
                            case "1":
                                textCell = ConfigControl.state1Text;
                                auxTool = new Tooltip(ConfigControl.state1Tool);
                                break;
                            case "2":
                                textCell = ConfigControl.state2Text;
                                auxTool = new Tooltip(ConfigControl.state2Tool);
                                break;
                            case "3":
                                textCell = ConfigControl.state3Text;
                                auxTool = new Tooltip(ConfigControl.state3Tool);
                                break;
                            case "4":
                                textCell = ConfigControl.state4Text;
                                auxTool = new Tooltip(ConfigControl.state4Tool);
                                break;
                            case "5":
                                textCell = ConfigControl.state5Text;
                                auxTool = new Tooltip(ConfigControl.state5Tool);
                                break;
                            case "6":
                                textCell = ConfigControl.state6Text;
                                auxTool = new Tooltip(ConfigControl.state6Tool);
                                break;
                            case "7":
                                textCell = ConfigControl.state7Text;
                                auxTool = new Tooltip(ConfigControl.state7Tool);
                                break;
                            case "8":
                                textCell = ConfigControl.state8Text;
                                auxTool = new Tooltip(ConfigControl.state8Tool);
                                break;
                            case "9":
                                textCell = ConfigControl.state9Text;
                                auxTool = new Tooltip(ConfigControl.state9Tool);
                                styleCell = ConfigControl.styleError;
                                break;
                            default:
                        }
                        super.setTooltip(auxTool);
                        setText(textCell);
                        setStyle(styleCell);
                    }
                }
            };
        });

        c3.setCellValueFactory(new Callback<CellDataFeatures<Tareas, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<Tareas, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                String respuesta = "";
                String aux;
                //NOTA se cambia el refresco a minutos porque no da tiempo a los 
                // ToolTips a existir/aparecer. Por lo que en el tiempo restante 
                // despreciaremos los segundos
                if (p != null) {
//                    System.err.println("refreshing");
                    aux = p.getValue().getTiempo();
                    if (aux != null) {
                        long diff = Long.valueOf(aux);
                        if (diff <= 0) {
                            tareasExcedidas.add(p.getValue());
                            respuesta = null;
                        } else if (diff < DIA_MILLIS) { // 86.400 1 dia
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
                        if(respuesta != null){
                            respuesta = p.getValue().getEstado() + "::" + respuesta;
                            return new ReadOnlyStringWrapper(respuesta); 
                        }else{
                            return new ReadOnlyStringWrapper(p.getValue().getEstado() + "::"); 
//                            return null;
                        }
                        
                    }
                }

                return null;
            }
        });
        c3.setCellFactory(c -> {
            return new TableCell<Tareas, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String estado = item.substring(0, item.indexOf("::"));
                        item = item.substring(item.indexOf("::") + 2);
                        if (estado.equals("9")) {
                            setStyle(ConfigControl.styleError);
                        } else {
                            setStyle(ConfigControl.styleNormal);
                        }
                        if(item.isEmpty()){
                            setText("");
                        }else{
                            super.setTooltip(new Tooltip("hh:mm"));
                            setText(item);  
                        }
                        
                        
                        
                    }
                }
            };
        });

        c4.setCellValueFactory(new Callback<CellDataFeatures<Tareas, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<Tareas, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                String aux = p.getValue().getFeedBack();

                if (aux != null) {
                    aux = p.getValue().getEstado() + "::" + aux;
                    return new ReadOnlyStringWrapper(aux);
                }
                return null;
            }
        });
        c4.setCellFactory(c -> {
            return new TableCell<Tareas, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String estado = item.substring(0, item.indexOf("::"));
                        item = item.substring(item.indexOf("::") + 2);
                        String[] listInfo = item.split("/n");
                        String cellText = "";
                        String cellTool = "";
                        String auxString = null;

                        for (String line : listInfo) {
                            if (line.contains("S: ")) {
                                auxString = line.replaceAll("S: ", "");
                                if (auxString != null && !auxString.isEmpty()) {
                                    if (new File(auxString).isFile()) {
                                        cellTool = ConfigControl.tableFile + auxString;
                                        auxString = ".." + auxString.substring(auxString.lastIndexOf("\\"));
                                        cellText = ConfigControl.tableFile + auxString;
                                    } else {
                                        cellTool = ConfigControl.tableFile + auxString;
                                        cellText = ConfigControl.tableFile + auxString;
                                    }

                                }
                            } else if (line.contains("N: ")) {
                                if (auxString != null && !auxString.isEmpty()) {
                                    cellText += "\n";
                                    cellTool += "\n";
                                }
                                auxString = line.replaceAll("N: ", "");
                                if (auxString != null && !auxString.isEmpty()) {
                                    cellTool += ConfigControl.tableNote + auxString;
                                    cellText += ConfigControl.tableNote + auxString;
                                }
                            } else if(line.contains("C: ")){
                                if (auxString != null && !auxString.isEmpty()) {
                                    cellText += "\n";
                                    cellTool += "\n";
                                }
                                auxString = line.replaceAll("C: ", "");
                                if (auxString != null && !auxString.isEmpty()) {
                                    //El comentario / correccion es sobre un fichero en la web
                                    if(auxString.equals(ResourceLeng.FEEDBACK_FILE)){
                                        auxString = ConfigControl.feedFile; 
                                    }
                                    cellTool += ConfigControl.tableFeed + auxString;
                                    cellText += ConfigControl.tableFeed + auxString;
                                }
                            }
                        }
                        if (estado.equals("9")) {
                            setStyle(ConfigControl.styleError);
                        } else {
                            setStyle(ConfigControl.styleNormal);
                        }
                        if (cellText.length() > 1) {
                            super.setTooltip(new Tooltip(cellTool));
                            setText(cellText);
                            addEventFilter(MouseEvent.MOUSE_CLICKED, event -> gestionarEventoFichero(tablaTareas.getSelectionModel().getSelectedItem()));
                        } else {
                            setText("");
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
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                        setGraphic(null);
                        
                    } else {
                        Tooltip auxTool = null;
                        String textStatus = item.getText();
                        String textCell = null;
                        String styleCell = ConfigControl.styleNormal;
                        switch (textStatus) {
                            case "0":
                            case "5":
                                textCell = ConfigControl.tButton05Text;
                                auxTool = new Tooltip(ConfigControl.tButton05Tool);
                                break;
                            case "1":
                            case "2":
                            case "6":
                            case "7":
                                textCell = ConfigControl.tButton1267Text;
                                auxTool = new Tooltip(ConfigControl.tButton1267Tool);
                                break;
                            case "3":
                            case "8":
                                textCell = ConfigControl.tButton38Text;
                                auxTool = new Tooltip(ConfigControl.tButton38Text);
                                break;
                            case "4":
                                textCell = ConfigControl.tButton4Text;
                                auxTool = new Tooltip(ConfigControl.tButton4Tool);
                                break;
                            case "9":
                                textCell = ConfigControl.tButton9Text;
                                auxTool = new Tooltip(ConfigControl.tButton9Tool);
                                styleCell = ConfigControl.styleError;
                                break;
                            default:
                        }
                        item.setText(textCell);
                        item.setTooltip(auxTool);
                        setStyle(styleCell);
                        item.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> gestionarEventoAccion(super.getIndex()/*TableDeliverys.getSelectionModel().getSelectedIndex()*/));
                        super.setGraphic(item);

                    }
                }
            };
        });

//      AL reordenar las columnas parece haber un fallo con los Eventlistener
//        MemoryLeak? revisar
        tablaTareas.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) {
                TableHeaderRow header = (TableHeaderRow) tablaTareas.lookup("TableHeaderRow");
                header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        header.setReordering(false);
                    }
                });
            }
        });
    }
    
    public void loadDummys() {
//        aniadirTarea("Proyecto Software (2017-2018)", "==> A. Documentación INDIVIDUAL - SEPTIEMBRE-18", "", "Monday, 17 September 2018, 4:00 PM", "en", "", "", "https://moodle2.unizar.es/add/mod/assign/view.php?id=1148926");
//        aniadirTarea("Proyecto Software (2017-2018)", "==> B1. Fuentes EQUIPO - SEPTIEMBRE-18", "", "Monday, 17 de September de 2018, 16:00", "es", "", "", "https://moodle2.unizar.es/add/mod/assign/view.php?id=1148927");
//        aniadirTarea("Bases de datos", "practica 5", "C:\\demo\\TestB.pdf", "Monday, 17 September 2018, 6:00 PM", "en", "9", "Buena practica", "");
//        aniadirTarea("Bases de datos", "practica 0", "TestB.pdf", "Monday, 3 September 2018, 1:00 PM", "en", "9", "", "");
//        aniadirTarea("Bases de datos", "practica 6", "", "Monday, 17 September 2018, 1:00 PM", "en", "9", "", "");
//        aniadirTarea("Bases de datos", "practica 7", "", "Wednesday, 12 September 2018, 11:56 AM", "en", "9", "Buena practica", "");
    }
}
