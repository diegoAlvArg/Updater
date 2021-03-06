package aplicacion.controlador;

//#1 Static import
//import aplicacion.controlador.MainControlador;
import aplicacion.datosListas.ItemHistorial;
import tools.lenguaje.ResourceLeng;
import tools.logger.LogGeneral;
//#4 Java
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
//#5 JavaFx
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** 177
 * Controlador de la tabla Historial, en la que existe un TreeView que llenaremos 
 * de elementos que representan recursos descargados
 * 
 * @author Diego Alvarez
 */
public class TabHistorialControlador {

    private MainControlador main;
    private Map<String, TreeItem<ItemHistorial>> cursosTrack = new HashMap<>();
    
    @FXML
    private TreeView tListUpdates;
    
    
    //---------------------------------------------------EVENTO------------------------------------------------- 
    /**
     *
     * Limpiara el treeView dejando solo el root
     */
    protected void limpiarTreeView() {
        tListUpdates.getRoot().getChildren().clear();
        cursosTrack.clear();
    }

    /**
     * Aniadira un nuevo item al treeView. Aniadienssolselo como "hijo" a un
     * nodo que lo represente, creando este si fuera necesario.
     *
     * @param path path del recurso que representa el item
     * @param name nombre con el que se representara
     * @param pathLocal path local donde descargaremos
     */
    protected synchronized void aniadirElementoTree(String path, String name, String pathLocal) {
        URL iconUrl = null;
        Image miImage = null;
        // Averiguaar donde meter  le nuevo elemento
        String curso = path.replace(pathLocal + File.separator, "");
        curso = curso.substring(0, curso.indexOf(File.separator));
        TreeItem<ItemHistorial> auxCurso;
        String auxExtension = "";
        int indexExt;
        
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
            ItemHistorial auxbook = new ItemHistorial(pathLocal
                    + File.separator + curso, curso);
            auxCurso = new TreeItem<>(auxbook, new ImageView(miImage));
            cursosTrack.put(curso, auxCurso);
            tListUpdates.getRoot().getChildren().add(auxCurso);
        }
        //  Creacion de un elemento para el arbol
        //  Creacion de una Imagen (icono) representativa del tipo
        indexExt = path.lastIndexOf(".");
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
            //Esto nunca saltara, la imagen es seleccionada de las existentes 
            //durante el compilado
        }
        ItemHistorial auxbook = new ItemHistorial(path, name);
        TreeItem<ItemHistorial> auxItem = new TreeItem<>(auxbook, new ImageView(miImage));

        // En el caso de que un TreeItem<BookCategory> ya este aniadido lo eliminamos
        for (TreeItem item : auxCurso.getChildren()) {
            if (item.getValue().equals(auxItem.getValue())) {
                auxCurso.getChildren().remove(item);
                break;
            }
        }
        auxCurso.getChildren().add(0, auxItem);
    }
    
    /**
     * Handle que tratara las acciones sobre el treeView, hara que el local
     * trate el path asociado al item que tratamos
     *
     * @param newValue treeItem que disparo el evento
     */
    private void manejadorTreeViewClick(TreeItem newValue) {
        ItemHistorial aux = null;
        try {
            aux = (ItemHistorial) newValue.getValue();
            main.getHostService().showDocument(aux.getPathFichero());
        } catch (Exception e) {
            LogRecord logRegistro;
            StringWriter errors = new StringWriter();
            String auxWho = main.getResource().getString(ResourceLeng.TRACE_TREE_ERROR);

            e.printStackTrace(new PrintWriter(errors));
            auxWho = String.format(auxWho, aux.print());
            logRegistro = new LogRecord(Level.SEVERE, auxWho + "\n" + errors.toString());
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
        }
    }

    //---------------------------------------------------UTILS-------------------------------------------------- 
    /**
     * Medoto para limpiar datos guardados
     */
    protected void limpiarRastro(){
        tListUpdates.getRoot().getChildren().clear();
        cursosTrack.clear();
    }
    
    
    //---------------------------------------------------INIT---------------------------------------------------
    protected void init(MainControlador mainController) {
        main = mainController;
        initializeTreeView();
    }
    /**
     * Inicializa la treeView y le aniade un listener. Esto se podria hacer con
     * el Scene builder o ha pelo pero no se.
     */
    private void initializeTreeView() {
        TreeItem<ItemHistorial> rootItem = new TreeItem<ItemHistorial>();
        tListUpdates.setRoot(rootItem);
        tListUpdates.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> manejadorTreeViewClick((TreeItem) newValue));
    }
}