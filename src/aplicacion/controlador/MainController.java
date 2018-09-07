package aplicacion.controlador;

//#1 Static import
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
import aplicacion.HelloWorld;
//import aplicacion.controlador.TabConfigController; // No estoy seguro si se 
//import aplicacion.controlador.TabDeliverController; //importa a lvl de 
//import aplicacion.controlador.TabHelpController;  // javafx
//import aplicacion.controlador.TabInitController;
import tools.almacen.InformacionUsuario;
import tools.lenguaje.ResourceLeng;
import tools.logger.LogGeneral;
//#4 Java
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.ResourceBundle;
//#5 JavaFx
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.util.Duration;

/**
 * Control principal, gestionara el flujo de datos entre las tablas hijas y
 * recursos que puedan necesitar
 * 
 * @author Diego Alvarez 
 */
public class MainController {

    @FXML
    private TabPane tTabPane;

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
    private int numTareas;
    private int numRecursos;
    
    @FXML
    public void initialize() {
        System.err.println("Application started");
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
        HelloWorld.setMetodosControl(this, "actualizarVesionFin", "saveData");//---------------
        HelloWorld.anidirOpcionSysTray(this, "abrirAyuda", ResourceLeng.SYS_TRAY_WIKI);
        HelloWorld.anidirOpcionSysTray(this, "actualizarVersion", ResourceLeng.SYS_TRAY_UPDATE);
        HelloWorld.anidirOpcionSysTray(this, "sincronizarAhora", ResourceLeng.SYS_TRAY_SYNCRO);
//        
        if (!InformacionUsuario.existenDatos()) {
            // No hay usuario
            tab3Controller.init(false, "", "", this);

            logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_NO));
        } else {
            try {
                tab3Controller.init(true, InformacionUsuario.getUser(), InformacionUsuario.getPath(), this);
                tab2Controller.cargarDatos(InformacionUsuario.getUser());
                tab3Controller.setSiguienteAlarma();
               
                logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_OK));
            } catch (NoSuchFieldException e) {
                tab3Controller.init(false, "", "", this);
                logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_LOST));
            }
        }
        inUse = false;//false
        
        timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(60000), //60 seg 60000
                        event -> {
                            refrescar();
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

    
    //-------------Tab Init--------------------------------------------------
    /**
     * Aniadira un nuevo item al treeView. Aniadienssolselo como "hijo" a un
     * nodo que lo represente, creando este si fuera necesario.
     *
     * @param path path del recurso que representa el item
     * @param name nombre con el que se representara
     * @param tipo tipo del item, para asignarle un icono
     */
    public synchronized void aniadirRecurso(String path, String name) {
        tab1Controller.aniadirElementoTree(path, name, tab3Controller.getLPathApp());
        numRecursos++;
    }
    
    
    //-------------Tab Deliver--------------------------------------------------
    /**
     * 
     */
    public void finalizarEntregaTarea(){
        tab2Controller.refrescar();
        liberarUsuario();
    }
    
    public void aniadirTarea(String curso, String titulo, String fichero, String tiempo, String languague, String nota, String comentario, String url){
        tab2Controller.anidirTarea(curso, titulo, fichero, tiempo, languague, nota, comentario, url);
        numTareas++;
    }
    
    public void guardarDatos(){
        tab2Controller.guardarDatos();
    }
    //-------------Tab Config---------------------------------------------------
    /**
     * Metodo que sera llamado al finalizar una sincronizacion
     */
    public void finalizarSincronizacion() {  
        ResourceBundle rb = getResource();
        if(numRecursos == 0 && numTareas == 0){
            //No hay nada nuevo
            ActionTool.mostrarNotificacion(rb, ResourceLeng.SYNCRO_END_TITLE, ResourceLeng.SYNCRO_END_NO_NEWS, 
                    Duration.seconds(10), NotificationType.INFORMATION);
        }else{
            String text = (numRecursos > 0) ? rb.getString(ResourceLeng.SYNCRO_END_RESOURCES) : null;
            if(text != null){
                text = String.format(text, numRecursos) + "\n";
            }else{
                text = "";
            }
            text = (numTareas > 0) ? text + rb.getString(ResourceLeng.SYNCRO_END_DELIVERY) : text;
            if(numTareas > 0){
                text = String.format(text, numTareas);
            }
            ActionTool.mostrarNotificacionConParam(rb.getString(ResourceLeng.SYNCRO_END_TITLE),
                    text, Duration.seconds(10), NotificationType.INFORMATION);
        }
            
        liberarUsuario();
    }
    
    /**
     * Metodo que sera llamado antes de inicar una sincronizacion
     */
    protected void iniciarSincronizacion() {
        if (numSyncro == ConfigControl.MAX_SBCLU) {
            tab1Controller.limpiarTreeView();
            numSyncro = 0;
        } else {
            numSyncro++;
        }
        numTareas = 0;
        numRecursos = 0;
    }

    /**
     * Metodo que sera llamado cuando haya que borrar el usuario 
     * o no haya usuario
     */
    protected void borrarRastroUsuario() {
        tab01.setDisable(true);
        tab02.setDisable(true);
        tTabPane.getSelectionModel().select(tab03);
    }
    /**
     * Metodo para iniciar el borrado de un usuario
     * wrongDates
     */
    public void borrarUsuario() {
        tab3Controller.borrarUsuario();
    }
    /**
     * Medoo que sera llamado cuando exista un usuario
     * aparecioUsuarioa
     */
    protected void establecerUsuario() {
        tab01.setDisable(false);
        tab02.setDisable(false);
    }

    protected void cambiarLenguage(ResourceBundle rb) {
        HelloWorld.cambiarTitulo(rb.getString(ResourceLeng.APP_TITLE));
        HelloWorld.setResource(rb);
        setLanguague(rb);
    }
    
    public void sincronizarAhora(){
        tab3Controller.sincronizarSysTray();
    }

    
    //--------------Tab Help
    public void actualizarVersion(){
        actualizarVersion(true);
    }
    protected void actualizarVersion(boolean mostrarMensaje){
        HelloWorld.actualizarVersion(mostrarMensaje);
    }
    public void actualizarVesionFin() {
        tab4Controller.actualizarVesionFin();
    }
    public void abrirAyuda(){
        tab4Controller.abrirAyuda();
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
    protected double getVersion(){
        return (double) HelloWorld.internalInformation.get("Version");
    }
    
    
    private void refrescar() {
        tab2Controller.refrescar();
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
    
    /**
     * Metodo para ocupar el perfil del usuario. 
     * canUseUser
     * 
     * @return 
     *  TRUE: si como resultado hemos ocupado el usuario
     *  FALSE: si el usuario no puede ser ocupado
     */
    protected synchronized boolean OcuparUsuario(){
        if(!inUse){
            //desactivar todo
            inUse = true;
            tab3Controller.ocuparUsuario();
            return true;
        }else{
            ActionTool.mostrarNotificacion(ResourceLeng.BUSSY_USER_TITLE, ResourceLeng.BUSSY_USER_TEXT,
                    Duration.seconds(10), NotificationType.INFORMATION);
            return false;
        }
    }
    /**
     * Metodo para liberar la ocupacion del usuario
     */
    protected void liberarUsuario(){
        inUse = false;
        tab3Controller.liberarUsuario();
    }
    /**
     * Metodo que indicara el estado de ocupado/libre del usuario
     * 
     * @return 
     */
    protected boolean disponibleUsuario(){
        return inUse;
    }
    
}
