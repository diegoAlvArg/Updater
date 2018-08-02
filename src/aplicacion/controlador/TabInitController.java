package aplicacion.controlador;


import Tools.lenguaje.ResourceLeng;
import Tools.logger.LogGeneral;
import javafx.fxml.FXML;
import aplicacion.controlador.MainController;
import aplicacion.datos.ItemArbol;
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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TabInitController {

    private MainController main;

    @FXML
    private TreeView TListUpdates;
//    private int numSyncro;
    private Map<String, TreeItem<ItemArbol>> cursosTrack = new HashMap<>();
    
    
    //---------------------------------------------------EVENTO------------------------------------------------- 
    /**
     *
     * Limpiara el treeView dejando solo el root
     */
    protected void cleanTreeView() {
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
    protected synchronized void addTreeItem(String path, String name, String sourceLocal) {
        URL iconUrl = null;
        Image miImage = null;
        // Averiguaar donde meter  le nuevo elemento
        String curso = path.replace(sourceLocal + File.separator, "");
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
            ItemArbol auxbook = new ItemArbol(sourceLocal
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
    //---------------------------------------------------INIT---------------------------------------------------
    protected void init(MainController mainController) {
        main = mainController;
        initializeTreeView();
    }
    /**
     * Inicializa la treeView y le aniade un listener. Esto se podria hacer con
     * el Scene builder o ha pelo pero no se.
     */
    private void initializeTreeView() {
        TreeItem<ItemArbol> rootItem = new TreeItem<ItemArbol>();
        TListUpdates.setRoot(rootItem);
        TListUpdates.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> handleTreeViewClick((TreeItem) newValue));
    }

}
