package aplicacion;

//#1 Static import
import tools.lenguaje.ResourceLeng;
import tools.logger.LogGeneral;
import tools.logger.LogSincronizacion;
import static actualizador.main.VersionCheck.howIsLastUpdate;
import actualizador.tools.ActionTool;
import actualizador.tools.InfoTool;
import actualizador.tools.NotificationType;
//#4 Java
import java.awt.*;
import java.awt.event.*;
import java.awt.TrayIcon;
import java.awt.SystemTray;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Duration;
//#5 JavaFx
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * T 5923   C 555
 * @author Diego Alvarez
 */
public class HelloWorld extends Application {
    // Variables de Informacion de la App.
    public static Properties internalInformation = new Properties();
    public static final double APPLICATION_VERSION = 1.0;
    static {
        //Important for Web Browser
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        //----------Properties-------------
        internalInformation.put("Version", APPLICATION_VERSION);
        internalInformation.put("ReleasedDate", "29/02/2018" );
    }
    // Varaibles de la clase
    private static HostServices servicioHost;
    private static ResourceBundle rb;
    private static Stage escenario;
    private static TrayIcon iconoSystemTray;
    private static SystemTray sytemTray;
    // Variables para llamar metodos de otras clases al finalizar tareas
    private static Object miControl;
    private static String medotoFin;
    private static String metodoGuardar;
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        LogRecord logRegistro;
        try {
            rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", Locale.getDefault());
        } catch (MissingResourceException e) {
            rb = ResourceBundle.getBundle("Resources.Languages.SystemMessages", Locale.ENGLISH);
            logRegistro = new LogRecord(Level.INFO, String.format(rb.getString(ResourceLeng.TRACE_LANGUAGUE_FAULT), Locale.getDefault()));
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
        }
        System.out.println(ResourceLeng.APP_INIT);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
        Scene scene;
        actualizarVersion(false);
        
        if (SystemTray.isSupported()) {
            Platform.setImplicitExit(false);
            iniciarSystemTray(rb);

            primaryStage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    try {
                        if (!oldValue && newValue) {
                            sytemTray.add(iconoSystemTray);
                            escenario.hide();
                            System.out.println("added to SystemTray");
                        }
//                        else if (oldValue && !newValue) {
//                            System.err.println("jeje");
//                        }
                    } catch (Exception ex) {
                        // AL volverlo a levantar desde el SystemTry generara este evento
                        System.out.println("unable to add to tray");
                        ex.printStackTrace();
                    }
//                    primaryStage.hide();
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        }

        logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_INIT_LOAD_XML));
        logRegistro.setSourceClassName(this.getClass().getName());
//        TabPane root = null;
        Parent root = null;// FXMLLoader.load(getClass().getResource("../view/Main.fxml"));
        try {
            //Carga de la interfaz y su controlador
//            root = (TabPane) FXMLLoader.load(getClass().getResource("/Resources/fxml/interface.fxml"), rb);
            root = FXMLLoader.load(getClass().getResource("/Resources/fxml/Main.fxml"), rb);
        } catch (Exception e) {
            // Esta excepcion no deberia ser necesaria, pero podria ocurrir tras
            //  mala manipulacion del controlador
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.SEVERE, "\n" + errors.toString());
            logRegistro.setSourceClassName(this.getClass().getName());
            LogGeneral.log(logRegistro);
        }
        scene = new Scene(root);
        primaryStage.setTitle(rb.getString(ResourceLeng.APP_TITLE));
//                + internalInformation.get("Version"));    
        servicioHost = getHostServices();
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            //Usar Platfomr en vez de System, Platform llamara al metodo Stop()
            //  inherente; mientras que System para de raiz el proceso.
            Platform.exit();
        });
        escenario = primaryStage;
    }

    /**
     *  Metodo para cambiar el titulo a la escena actual
     * 
     * @param titulo 
     */
    public static void cambiarTitulo(String titulo) {
        escenario.setTitle(titulo);// + internalInformation.get("Version"));
    }

    /**
     * 
     * @return 
     */
    public static HostServices getHostService() {
        return servicioHost;
    }
    
    /**
     * 
     * @return 
     */
    public static ResourceBundle getResource() {
        return rb;
    }
    /**
     * 
     * @param newrb 
     */
    public static void setResource(ResourceBundle newrb) {
        rb = newrb;
        if (iconoSystemTray != null) {
            cambiarIdiomaSysTray();
        }
    }
  
    @Override
    public void stop() {
        guardarDatos();
        LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_END_APP));
        logRegistro.setSourceClassName(this.getClass().getName());
        LogGeneral.log(logRegistro);
        LogSincronizacion.cerrar();
        LogGeneral.cerrar();
    }
    //--------------------ACTUALIZACION-------------------------------------------------------------------
    /**
     * Calling this method to start the main Application which is XR3Player
     */
    public static void inicarAplicacionExterna(String appName, double version) {
//        System.out.println(ResourceLeng.APP_INIT); //-
        // Se ha quitado la ejecucion en hilo, evita solapamientos con el 
        //  escenario de arranque normal y evita eventos en segundo plano
        // Restart XR3Player
        String path = InfoTool.getBasePathForClass(HelloWorld.class);
        String[] applicationPath = {new File(path + appName + ".jar").getAbsolutePath()};
        //Show message that application is restarting
        try {
            //Create a process builder
//            ProcessBuilder builder = new ProcessBuilder("java", "-jar", applicationPath[0], String.valueOf(version));
            ProcessBuilder builder = new ProcessBuilder("./jre/bin/java.exe", "-jar", applicationPath[0], String.valueOf(version));
            LogRecord logRegistro = new LogRecord(Level.INFO, "CMD: " + builder.command());
            LogGeneral.log(logRegistro);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Continuously Read Output to check if the main application started
            String line;
            String mark_OK = ResourceLeng.APP_UPDATER_INIT;
            String mark_ERROR = ResourceLeng.ERROR;
            while (process.isAlive()) {
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.isEmpty()) {
                        break;
                    } else if (line.contains(mark_OK)) {
                        //This line is being printed when XR3Player Starts 
                        //So the AutoUpdater knows that it must exit
                        LogSincronizacion.cerrar();
                        LogGeneral.cerrar();
                        System.exit(0);
                    } else if (line.contains(mark_ERROR)) {
                        //Some kind of problem
                        throw new InterruptedException();
                    }
                }
            }
        } catch (IOException | InterruptedException ex) {
            // Show failed message
            Platform.runLater(() -> ActionTool.mostrarNotificacionConParam(String.format(rb.getString(ResourceLeng.MESSAGE_TITLE_UPDATE_FAIL), appName),
                    String.format(rb.getString(ResourceLeng.MESSAGE_TEXT_UPDATE_FAIL), applicationPath[0]),
                    Duration.seconds(10), NotificationType.ERROR));
        }
    }
 
    /**
     * Metodo para iniciar el proceso de actualizacion
     * 
     * @param mostrarMensaje mostrar mensaje en caso de misma version
     */
    public static void actualizarVersion(boolean mostrarMensaje) {
        double currentVersion = (double) internalInformation.get("Version");//---------------
        double lastVersion = howIsLastUpdate();
        System.out.println("Version " + lastVersion);
        if(lastVersion == -1.0){
            ActionTool.mostrarNotificacion(rb, ResourceLeng.UPDATE_NO_ETHERNET,
                    ResourceLeng.UPDATE_NO_ETHERNET_TEXT, Duration.seconds(10), NotificationType.ERROR);
        }else if(lastVersion == -2.0){
            ActionTool.mostrarNotificacion(rb, ResourceLeng.UPDATE_ERROR_FILE,
                    ResourceLeng.UPDATE_ERROR_FILE_TEXT, Duration.seconds(10), NotificationType.WARNING);
        }else if (currentVersion < lastVersion) {
            guardarDatos();
            if (Platform.isFxApplicationThread()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION); //Idioma en botones
                alert.setTitle(rb.getString(ResourceLeng.UPDATE_TITLE));
                alert.setHeaderText(String.format(rb.getString(ResourceLeng.UPDATE_HEADER),
                        currentVersion, lastVersion));
                alert.setContentText(rb.getString(ResourceLeng.UPDATE_CONTENT));

                ButtonType buttonYES = new ButtonType(rb.getString(ResourceLeng.ASK_BUTTON_ACCEPT));
                ButtonType buttonNO = new ButtonType(rb.getString(ResourceLeng.ASK_BUTTON_CANCEL));

                alert.getButtonTypes().setAll(buttonYES, buttonNO);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonYES) {
                    inicarAplicacionExterna("XR3PlayerUpdater", lastVersion);
                }
            } else {
                // Preguntar actualizar desde SystemTray. ESTO NO ES BLOQUEANTE
                //  Confiamos un poco en el usuario, y no influye mucho
                Object[] options = {rb.getString(ResourceLeng.ASK_BUTTON_ACCEPT),
                    rb.getString(ResourceLeng.ASK_BUTTON_CANCEL)};
                int n = JOptionPane.showOptionDialog(new JFrame("DialogDemo"),
                        String.format(rb.getString(ResourceLeng.UPDATE_HEADER),
                                currentVersion, lastVersion),
                        rb.getString(ResourceLeng.UPDATE_TITLE),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, //do not use a custom Icon
                        options, //the titles of buttons
                        options[0]); //default button title
                if (n == JOptionPane.YES_OPTION) {
                    inicarAplicacionExterna("XR3PlayerUpdater", lastVersion);
                }
            }
        } else if (mostrarMensaje) {
            ActionTool.mostrarNotificacion(rb, ResourceLeng.UPDATE_INFO,
                    ResourceLeng.UPDATE_INFO_TEXT, Duration.seconds(10), NotificationType.INFORMATION);
//            cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_UPDATE, true);
        }
        finalizarActualizacion();
    }

    //--------------------ACTION4CONTROL---------------------------------------------------------------------
    /**
     * Metodo para setear los metodos que la App debe llamar para Cerrar y 
     *  guardar Datos.
     * 
     * @param control
     * @param metEnd
     * @param metSave 
     */
    public static void setMetodosControl(Object control, String metEnd, String metSave) {
        miControl = control;
        medotoFin = metEnd;
        metodoGuardar = metSave;
    }
    /**
     * Metodo que ejecuta el fin de una actualizacion no realizada.
     * 
     */
    private static void finalizarActualizacion() {
        cambiarDisponibilidadOpcionSysTray(ResourceLeng.SYS_TRAY_UPDATE, true);
        if (miControl != null) {
            try {
                final Method method = miControl.getClass().getMethod(medotoFin);
                method.invoke(miControl);
            } catch (NoSuchMethodException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Metodo que llamara al controlador para que almacene los datos 
     *  que contenga
     */
    private static void guardarDatos() {
        if (miControl != null) {
            try {
                final Method method = miControl.getClass().getMethod(metodoGuardar);
                method.invoke(miControl);
            } catch (NoSuchMethodException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
//              Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //--------------------SYSTRAY---------------------------------------------------------------------------
    /**
     * Metodo para construir el SystemTray, el cual esta soportado
     * 
     * @param rb 
     */
    private void iniciarSystemTray(ResourceBundle rb) {
//        System.out.println("system tray supported");
        sytemTray = SystemTray.getSystemTray();
        URL imageURL = this.getClass().getResource("/Resources/Icons/logo_moodle.png");
//        System.err.println(imageURL);

        Image image = (new ImageIcon(imageURL)).getImage();//Toolkit.getDefaultToolkit().getImage("./logo_moodle.png");  
        PopupMenu popup = new PopupMenu();
        MenuItem defaultItem = new MenuItem(rb.getString(ResourceLeng.SYS_TRAY_EXIT));
        defaultItem.setName(ResourceLeng.SYS_TRAY_EXIT);
        defaultItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LogRecord logRegistro = new LogRecord(Level.INFO, rb.getString(ResourceLeng.TRACE_END_SYSTRAY));
                logRegistro.setSourceClassName(this.getClass().getName());
                LogGeneral.log(logRegistro);
                System.exit(0);
            }
        });
        popup.add(defaultItem);

        defaultItem = new MenuItem(rb.getString(ResourceLeng.SYS_TRAY_OPEN));
        defaultItem.setName(ResourceLeng.SYS_TRAY_OPEN);
        defaultItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sytemTray.remove(iconoSystemTray);
                            escenario.show();
                            escenario.setIconified(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        popup.insert(defaultItem, 0);

        iconoSystemTray = new TrayIcon(image, rb.getString(ResourceLeng.SYS_TRAY_TOOLTIP), popup);
        iconoSystemTray.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sytemTray.remove(iconoSystemTray);
                            escenario.show();
                            escenario.setIconified(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        iconoSystemTray.setImageAutoSize(true);
    }

    /**
     * Metodo que devuelve el icono del Systray
     * 
     * @return 
     */
    public static TrayIcon getSysTray() {
        return iconoSystemTray;
    }

    /**
     * Metodo para aniadir un accion a las que se presentaran al Systray, si no
     *  lo esta ya
     * 
     * 
     * @param control Objeto que invocara la accion
     * @param nombreMetodo nombre del metodo que se invocara
     * @param tagTexto tag del texto que se presenta
     *
     * @see
     * https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#getMethod-java.lang.String-java.lang.Class...-
     * @see
     * https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Method.html#invoke-java.lang.Object-java.lang.Object...-
     */
    public static void anidirOpcionSysTray(Object control, String nombreMetodo, String tagTexto ){
        boolean noEstaAniadido = true;
        MenuItem auxItem;
        // Si es distinto de null es porque el SystemTray esta soportado y por tanto inicializado
        if (iconoSystemTray != null) {

            PopupMenu popup = iconoSystemTray.getPopupMenu();
            MenuItem defaultItem = new MenuItem(rb.getString(tagTexto));
            defaultItem.setName(tagTexto);
//            System.err.println(defaultItem.getName());
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        LogRecord logRegistro = new LogRecord(Level.INFO, String.format(
                                rb.getString(ResourceLeng.TRACE_USE_SYSTRAY),
                                rb.getString(tagTexto), tagTexto));
                        logRegistro.setSourceClassName("SystemTray");
                        LogGeneral.log(logRegistro);

                        defaultItem.setEnabled(false);
                        final Method method = control.getClass().getMethod(nombreMetodo);
                        method.invoke(control);
                    } catch (NoSuchMethodException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
//                        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            for (int i = 0; i < iconoSystemTray.getPopupMenu().getItemCount(); i++) {
                auxItem = iconoSystemTray.getPopupMenu().getItem(i);
                if (auxItem.getName().equals(tagTexto)) {
                    noEstaAniadido = false;
                    break;
                }
            }
            if (noEstaAniadido) {
                popup.insert(defaultItem, 0);
            }
        }
    }
    /**
     * PRE: SysTray soportado.
     * Metodo que actualiza el texto de las opciones del Systray, en funciona 
     *  de su "TAG"
     */
    private static void cambiarIdiomaSysTray() {
        MenuItem auxItem;
        for (int i = 0; i < iconoSystemTray.getPopupMenu().getItemCount(); i++) {
            auxItem = iconoSystemTray.getPopupMenu().getItem(i);
            auxItem.setLabel(rb.getString(auxItem.getName()));
        }
    }
    /**
     * Metodo para inhabilitar o habilitar una opcion del Systray, esto se debe 
     *  a que el usuario provoco el evento gestionado por el control y aunque 
     *  minimize (y se muestre el SysTray) el usuario no podra volver a lanzar 
     *  el mismo evento
     * 
     * @param tagTexto tag de la opcion sobre la que se interactua
     * @param activo disponibilidad
     */
    public static void cambiarDisponibilidadOpcionSysTray(String tagTexto, boolean activo) {
//        System.err.println("Desactivando" + option);
//        String aux;
        if (iconoSystemTray != null) {
            MenuItem auxItem;
            for (int i = 0; i < iconoSystemTray.getPopupMenu().getItemCount(); i++) {
                auxItem = iconoSystemTray.getPopupMenu().getItem(i);
                if (auxItem.getName().equals(tagTexto)) {
                    System.err.println("lo encontre");
                    auxItem.setEnabled(activo);
                    break;
                }
            }
        }

    }

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
