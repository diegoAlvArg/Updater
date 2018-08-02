package aplicacion.controlador;

import Tools.almacen.InformacionUsuario;
import Tools.lenguaje.ResourceLeng;
import Tools.logger.LogGeneral;
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
import aplicacion.HelloWorld;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import aplicacion.controlador.TabInitController;
import aplicacion.controlador.TabDeliverController;
import aplicacion.controlador.TabHelpController;
import aplicacion.controlador.TabConfigController;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.scene.control.TabPane;
import javafx.util.Duration;

public class MainController {

    @FXML
    private TabPane TTabpane;

    @FXML
    private Tab tab01;
    @FXML
    private TabInitController tab1Controller;
    private int numSyncro;

    @FXML
    private Tab tab02;
    @FXML
    private TabDeliverController tab2Controller;

    @FXML
    private Tab tab03;
    @FXML
    private TabConfigController tab3Controller;

    @FXML
    private Tab tab04;
    @FXML
    private TabHelpController tab4Controller;

    private Timeline timeline;
    private boolean inUse;
    
    @FXML
    public void initialize() {
        System.err.println("Application started");
//        tab1Controller.init(this);
//        tab2Controller.init(this);
//        tab01.setText("bbb");
//
//        tab3Controller.init(null, this);
//        tab4Controller.init(this);
//        

        LogRecord logRegistro;

        ResourceBundle rb = HelloWorld.getResource();

        logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_INIT_LOAD_CONTROL));
        logRegistro.setSourceClassName(this.getClass().getName());
        LogGeneral.log(logRegistro);

        tab1Controller.init(this);  //Tab init
        tab2Controller.init(this);  //Tab deliverys
        tab4Controller.init(this);  //Tab help
        //Tab 3 depende si usuario, que son las lineas siguientes.

        setLanguague(rb);
        HelloWorld.setMetodosControl(this, "actualizarVesionEnd", "saveData");
        HelloWorld.anidirOpcionSysTray(this, "openHelp", ResourceLeng.SYS_TRAY_WIKI);
        HelloWorld.anidirOpcionSysTray(this, "actualizarVersion", ResourceLeng.SYS_TRAY_UPDATE);
        HelloWorld.anidirOpcionSysTray(this, "syncroNow", ResourceLeng.SYS_TRAY_SYNCRO);
//        
        if (!InformacionUsuario.existenDatos()) {
            // No hay usuario
            tab3Controller.init(false, "", "", this);

            logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_NO));
        } else {
            try {
                tab3Controller.init(true, InformacionUsuario.getUser(), InformacionUsuario.getPath(), this);
                tab3Controller.setNextUpdate();
                
                logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_OK));
            } catch (NoSuchFieldException e) {
                tab3Controller.init(false, "", "", this);
//                initializationUserLoad(false, "", "");
                logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_LOST));
            }
        }
        inUse = false;//false
        
        timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(60000), //60 seg 60000
                        event -> {
                            refresh();
//                            TableDeliverys.refresh();
                        }
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        if (logRegistro != null) {
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);

        }
    }

//    /**
//     * @deprecated 
//     * @param text 
//     */
//    public void testButton(String text) {
//        System.out.println("Call from " + text);
//    }

    /**
     * Aniadira un nuevo item al treeView. Aniadienssolselo como "hijo" a un
     * nodo que lo represente, creando este si fuera necesario.
     *
     * @param path path del recurso que representa el item
     * @param name nombre con el que se representara
     * @param tipo tipo del item, para asignarle un icono
     */
    public synchronized void addTreeItem(String path, String name) {
        tab1Controller.addTreeItem(path, name, tab3Controller.getLPathApp());
    }

    //-------------Tab Config
    public void syncroEnd() {
        liberarUsuario();
    }

    protected void loggedSyncro() {
        if (numSyncro == ConfigControl.MAX_SBCLU) {
            tab1Controller.cleanTreeView();
            numSyncro = 0;
        } else {
            numSyncro++;
        }
    }

    protected void borrarRastroUsuario() {
        tab01.setDisable(true);
        tab02.setDisable(true);
        TTabpane.getSelectionModel().select(tab03);
    }

    public void wrongDates() {
        tab3Controller.wrongDates();
    }

    protected void aparecioUsuario() {
        tab01.setDisable(false);
        tab02.setDisable(false);
    }

    protected void changeLanguague(ResourceBundle rb) {
        HelloWorld.cambiarTitulo(rb.getString(ResourceLeng.APP_TITLE));
        HelloWorld.setResource(rb);
        setLanguague(rb);
    }
    
    

    
    //--------------Tab Help
    public void actualizarVersion(){
        actualizarVersion(true);
    }
    protected void actualizarVersion(boolean mostrarMensaje){
        HelloWorld.actualizarVersion(mostrarMensaje);
    }
    public void actualizarVesionEnd() {
        tab4Controller.actualizarVesionEnd();
    }

    
    //--------------UTILS
    private void setLanguague(ResourceBundle rb) {
        //Tab Init
        tab01.setText(rb.getString(ResourceLeng.TAB_INIT));

        //Tab Delivery's
        tab02.setText(rb.getString(ResourceLeng.TAB_DELIVERY));
        tab2Controller.setLanguague(rb);

        //Tab Config
        tab03.setText(rb.getString(ResourceLeng.TAB_CONFIG));
        tab3Controller.setLanguague(rb);

        //Tab Help
        tab04.setText(rb.getString(ResourceLeng.TAB_HELP));
        tab4Controller.setLanguague(rb);
    }

    protected HostServices getHostService() {
        return HelloWorld.getHostService();
    }
    /**
     *
     * @return
     */
    protected ResourceBundle getResource() {
        return HelloWorld.getResource();
    }
    protected int getVersion(){
        return (int) HelloWorld.internalInformation.get("Version");
    }
    
    
    private void refresh() {
        tab2Controller.refresh();
        System.err.println("popo");
    }
    
    /**
     *
     * @param method
     * @param state 
     */
    protected void cambiarDisponibilidadOpcionSysTray(String method, boolean state){
        HelloWorld.cambiarDisponibilidadOpcionSysTray(method, state);
    }
    
    protected synchronized boolean canUseUser(){
        if(!inUse){
            //desactivar todo
            inUse = true;
            tab3Controller.blockSincro();
            return true;
        }else{
            ActionTool.mostrarNotificacion(ResourceLeng.BUSSY_USER_TITLE, ResourceLeng.BUSSY_USER_TEXT,
                    Duration.seconds(10), NotificationType.INFORMATION);
            return false;
        }
    }
    protected void liberarUsuario(){
        inUse = false;
        tab3Controller.resumeUserFree();
    }
    protected boolean inUseUser(){
        return inUse;
    }
    
}
