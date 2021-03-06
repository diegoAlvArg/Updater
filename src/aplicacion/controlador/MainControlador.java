package aplicacion.controlador;

//#1 Static import
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
import aplicacion.MainClass;
//import aplicacion.controlador.TabConfiguracionControlador; // No estoy seguro si se 
//import aplicacion.controlador.TabEntregaControlador; //importa a lvl de 
//import aplicacion.controlador.TabAyudaControlador;  // javafx
//import aplicacion.controlador.TabHistorialControlador;
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

/** 334
 * Control principal, gestionara el flujo de datos entre las tablas hijas y
 * recursos que puedan necesitar
 * 
 * @author Diego Alvarez 
 */
public class MainControlador {

    @FXML
    private TabPane tTabPane;

    @FXML
    private Tab tab01;
    @FXML
    private TabHistorialControlador tab1Controller;
    private int numSincro;

    @FXML
    private Tab tab02;
    @FXML
    private TabEntregaControlador tab2Controller;

    @FXML
    private Tab tab03;
    @FXML
    private TabConfiguracionControlador tab3Controller;

    @FXML
    private Tab tab04;
    @FXML
    private TabAyudaControlador tab4Controller;

    private Timeline timeline;
    private boolean enUso;
    private int numTareas;
    private int numRecursos;
    
    @FXML
    public void initialize() {
        LogRecord logRegistro;
        ResourceBundle rb = MainClass.getResource();

        logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_INIT_LOAD_CONTROL));
        logRegistro.setSourceClassName(this.getClass().getName());
        LogGeneral.log(logRegistro);

        tab1Controller.init(this);  //Tab init
        tab2Controller.init(this);  //Tab deliverys
        tab4Controller.init(this);  //Tab help
        //Tab 3 depende si usuario, que son las lineas siguientes.

        setLanguague(rb);
        MainClass.setMetodosControl(this, "actualizarVesionFin", "guardarDatos");//-------------------------------------------
        MainClass.anidirOpcionSysTray(this, "abrirAyuda", ResourceLeng.SYS_TRAY_WIKI);
        MainClass.anidirOpcionSysTray(this, "actualizarVersion", ResourceLeng.SYS_TRAY_UPDATE);
        MainClass.anidirOpcionSysTray(this, "sincronizarAhora", ResourceLeng.SYS_TRAY_SYNCRO);
//        
        if (!InformacionUsuario.existenDatos()) {
            // No hay usuario
            tab3Controller.init(false, "", "", this);

            logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_NO));
        } else {
            try {
                tab3Controller.init(true, InformacionUsuario.getUsuario(), InformacionUsuario.getPath(), this);
                tab2Controller.cargarDatos(InformacionUsuario.getUsuario());
                tab3Controller.setSiguienteAlarma();
               
                logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_OK));
            } catch (NoSuchFieldException e) {
                tab3Controller.init(false, "", "", this);
                logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_USER_LOST));
            }
        }
        enUso = false;//false
        Timeline autoRegister = new Timeline(
                new KeyFrame(
                        //Con 2 seg. un poco justo para algunos ordenadores
                        Duration.millis(2500), //2.5 seg 2500 
                        event2 -> {
                            if(tab01.isDisable()){
                                tab3Controller.preguntarUsuario();
                            }
                        }
                )
        );
        autoRegister.setCycleCount(1);
        autoRegister.play();
        
        
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
     * @param nombre nombre con el que se representara
     */
    public synchronized void aniadirRecurso(String path, String nombre) {
        tab1Controller.aniadirElementoTree(path, nombre, tab3Controller.getLPathApp());
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
        if(tab2Controller.aniadirTarea(curso, titulo, fichero, tiempo, languague, nota, comentario, url)){
            numTareas++;
        };
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
        enUso = false;
        tab3Controller.sincronizarFin();
    }
    
    /**
     * Metodo que sera llamado antes de inicar una sincronizacion
     */
    protected void iniciarSincronizacion() {
        if (numSincro == ConfigControl.MAX_SBCLU) {
            tab1Controller.limpiarTreeView();
            numSincro = 0;
        } else {
            numSincro++;
        }
        numTareas = 0;
        numRecursos = 0;
        tab2Controller.cargarActualizables();
    }

    /**
     * Metodo que sera llamado cuando haya que borrar el usuario 
     * o no haya usuario
     */
    protected void borrarRastroUsuario() {
        tab01.setDisable(true);
        tab1Controller.limpiarRastro();
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
        MainClass.cambiarTitulo(rb.getString(ResourceLeng.APP_TITLE));
        MainClass.setResource(rb);
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
        MainClass.actualizarVersion(mostrarMensaje);
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
        return MainClass.getHostService();
    }
    /**
     *
     * @return
     */
    protected ResourceBundle getResource() {
        return MainClass.getResource();
    }
    protected double getVersion(){
        return (double) MainClass.internalInformation.get("Version");
    }
    
    
    private void refrescar() {
        if(!tab02.isDisable()){
            tab2Controller.refrescar();
        }
    }
    
    /**
     *
     * @param metodo
     * @param estado 
     */
    protected void cambiarDisponibilidadOpcionSysTray(String metodo, boolean estado){
        MainClass.cambiarDisponibilidadOpcionSysTray(metodo, estado);
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
        if(!enUso){
            //desactivar todo
            enUso = true;
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
        enUso = false;
        tab3Controller.liberarUsuario();
    }
    /**
     * Metodo que indicara el estado de ocupado/libre del usuario
     * 
     * @return 
     */
    protected boolean disponibleUsuario(){
        return enUso;
    }
    
}
