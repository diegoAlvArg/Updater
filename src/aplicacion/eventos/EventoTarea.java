package aplicacion.eventos;

//#1 Static import
import actualizador.tools.ActionTool;
import actualizador.tools.NotificationType;
import aplicacion.controlador.MainControlador;
import aplicacion.datosListas.Tarea;
import tools.lenguaje.ResourceLeng;
import tools.logger.LogGeneral;
//#3 Third party
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//#4 Java
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
//#5 JavaFx
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.util.Duration;

/**
 *  ---------------OJO-------------------------------------------------------------------------------------------------
 * Que la entrega esta habilitada
 * 348 
 * @author Diego Alvarez
 */
public class EventoTarea {

    /**
     * Metodo que desencadena un evento de entrega de tarea, para ello el usuario
     *  debera elegir un fichero del local y si sobre dicho fichero tenemos 
     *  permisos de lectura; lanzaremos el evento. En caso de no permisos o no 
     *  seleccion se dejara las cosas como estaban.
     * 
     * @param miTarea
     * @param control
     * @param usuario NIP del usuario
     * @param contrasenia contrasenia de Moodle
     * @param pathFile  path del fichero en local
     * @param rb Indicara el idioma en el momento actual de generar el evento
     */
    public EventoTarea(Tarea miTarea, MainControlador control, String usuario, String contrasenia, String pathFile, ResourceBundle rb) {
        File selectedFile = null;
        String initialPath;

        FileChooser fileChooser = new FileChooser();
        initialPath = miTarea.getPathFile();
        initialPath = (initialPath.isEmpty()) ? pathFile : initialPath;
        do {
            fileChooser.setInitialDirectory(new File(initialPath));
            selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile == null || selectedFile.canRead()) {
                break;
            } else {
                ActionTool.mostrarNotificacion(rb, ResourceLeng.FILECHOOSER_PERMISIONS_TITLE,
                        ResourceLeng.FILECHOOSER_PERMISIONS_TEXT, Duration.seconds(15),
                        NotificationType.ERROR);
            }
        } while (true);

        if (selectedFile != null) {
            procesoSubida(usuario, contrasenia, selectedFile.getAbsolutePath(), miTarea, rb, control);
        } else {
            miTarea.resetearEstado();
            control.finalizarEntregaTarea();
        }
    }
    
    /**
     * Metodo que creara un hilo para gestionar el evento tarea. Entregra un
     *  fichero a la entrega.
     * 
     * @param usuario NIP del usuario
     * @param contrasenia contrasenia de Moodle
     * @param pathFile path del fichero en local
     * @param miTarea Nodo representativo de la tarea, que refleja la informacion
     * @param rb Indicara el idioma en el momento actual de generar el evento
     * @param control Clase de control que gestionara la finalizacion del evento
     */
    public void procesoSubida(String usuario, String contrasenia, String pathFile, Tarea miTarea, ResourceBundle rb, MainControlador control) {
        new Thread(() -> {
            int estado = navegarAEdicion(usuario, contrasenia, miTarea.getUrlWeb(), pathFile, miTarea.getPathFile().equals(""), rb);   
            switch(estado){
                case 0:
                    //No se pudo gestionar la entrega, intentelo mas tarde
                    Platform.runLater(() ->ActionTool.mostrarNotificacion(ResourceLeng.DELIVER_ERROR_TITLE, ResourceLeng.DELIVER_ERROR_TEXT,
                            Duration.seconds(10), NotificationType.ERROR));
                    break;
                case 1:
                    //Entrega exitosa
                    miTarea.setPathFile(pathFile);
                    break;
                case 2:
                    //Posible desactualizacion
                    Platform.runLater(() ->ActionTool.mostrarNotificacion(ResourceLeng.DELIVER_NO_ACTUAL_TITLE, ResourceLeng.DELIVER_NO_ACTUAL_TEXT,
                            Duration.seconds(10), NotificationType.WARNING));
                    break;
                default:
            }
            miTarea.resetearEstado();
            Platform.runLater(() -> control.finalizarEntregaTarea());
        }).start();
    }
   
    /**
     * Metodo que dado una Tarea y un fichero gestionara la entrega del mismo, 
     *  desde la navegacion de la misma, subida del archivo al S.F unizar y 
     *  "borrado" de un fichero que hubiera sido entregado previamente.
     * 
     * @param usuario NIP del usuario
     * @param contrasenia contrasenia de Moodle
     * @param urlTarea Url de la tarea
     * @param pathFile path del fichero en local
     * @param isNew 
     *  True- Sí solo hay que subir el fichero
     *  False- Sí antes de subir el fichero hay que "borrar" un fichero7entrega
     *  previa
     * @param rb Indicara el idioma en el momento actual de generar el evento
     * @return 
     *  0- Error durante el proceso
     *  1- Entrega realizada con exito
     *  2- Entrega no realizada, posiblemente moodle entregado y local no actualizado.
     */
    private static int navegarAEdicion(String usuario, String contrasenia, String urlTarea, String pathFile, boolean isNew, ResourceBundle rb) {
        Connection.Response res;
        Document doc;
        Map<String, String> cookies;
        Element dato;
        Elements datos;
        String datosFormEdicion[][] = new String[2][2];
        HashMap<String, String> datosFormEntrega = new HashMap<String, String>() {
                {
                    put("id", "");
                    put("userid", "");
                    put("action", "");
                    put("sesskey", "");
                    put("_qf__mod_assign_submission_form", "");
                }
            };
        String idFile; 
        int index = 0;
        int respuesta = 0;
        String tagLenguaje = rb.getLocale().getCountry().toLowerCase();
        String urlEdit;    
           
        try {
            //Login
            res = Jsoup.connect("https://moodle2.unizar.es/add/login/index.php")
                    .header("Accept-Language", tagLenguaje)
                    .data("username", usuario, "password", contrasenia)
                    .method(Connection.Method.POST).timeout(180 * 1000)
                    .execute();
            doc = res.parse();
            cookies = res.cookies();
 
            
            //Tarea
            doc = Jsoup.connect(urlTarea).header("Accept-Language", tagLenguaje)
                    .timeout(180 * 1000).cookies(cookies).post();
            dato = doc.selectFirst("form");
            urlEdit = dato.attr("action");

            //Zona de Edicion
            datos = dato.select("input[name]");
            for (Element input : datos) {
                datosFormEdicion[index][0] = input.attr("name");
                datosFormEdicion[index][1] = input.attr("value");
                index++;
            }

            doc = Jsoup.connect(urlEdit)
                    .header("Accept-Language", tagLenguaje)
                    .data(datosFormEdicion[0][0], datosFormEdicion[0][1])
                    .data(datosFormEdicion[1][0], datosFormEdicion[1][1])
                    .timeout(180 * 1000).cookies(cookies).get();

            datos = doc.select("form>div>input[name]");
            //Guardando los datos dle form para entrega
            for (Element a : datos) {
                if (datosFormEntrega.containsKey(a.attr("name"))) {
                    datosFormEntrega.put(a.attr("name"), a.attr("value"));
                }
            }

            //-----------Sistema de ficheros de unizar
            idFile = doc.selectFirst("noscript>div>object").attr("data");

            if (isNew) {
                idFile = borrarArchivo(cookies, idFile, rb, tagLenguaje);
            }

            //"Entrega" del fichero
            if (idFile != null) {
                idFile = subirArchivo(cookies, idFile, pathFile, rb, tagLenguaje);

                doc = Jsoup.connect("https://moodle2.unizar.es/add/mod/assign/view.php?id=1048532&action=editsubmission")
                        .header("Accept-Language", tagLenguaje)
                        .data("id", datosFormEntrega.get("id"))
                        .data("userid", datosFormEntrega.get("userid"))
                        .data("action", datosFormEntrega.get("action"))
                        .data("sesskey", datosFormEntrega.get("sesskey"))
                        .data("_qf__mod_assign_submission_form", datosFormEntrega.get("_qf__mod_assign_submission_form"))
                        .data("files_filemanager", idFile) //La id del archivo
                        .timeout(180 * 1000).cookies(cookies).post();
                respuesta = 1;
            } else {
                respuesta = 2;
            }
        } catch (IOException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            LogRecord logRegistro = new LogRecord(Level.SEVERE, "\n" + errors.toString());
            logRegistro.setSourceClassName("EventoTarea");
            logRegistro.setSourceMethodName("navegarAEdicion");
            LogGeneral.log(logRegistro);
        }finally{
            return respuesta;
        }
    }

    /**
     * Metodo que conectara a una Url del S.F. de unizar, relacionada a un 
     *  fichero, y "borrara" el fichero devolviendo una Url donde podremos
     *  gestionar la subida del fichero
     *  
     * @param cookies Map de cookies que mantiene la sesion
     * @param url Url del S.F unizar
     * @param rb Indicara el idioma en el momento actual de generar el evento
     * @param tagLenguaje Tag del idioma actual (ISO 639-1)
     * @return 
     *  Url util. Null en caso de error.
     */
    private static String borrarArchivo(Map<String, String> cookies, String url, ResourceBundle rb, String tagLenguaje) {
        Document doc;
        Elements opciones;
        String respuesta = null;
        String tagGoal;
        try {
            doc = Jsoup.connect(url)
                    .header("Accept-Language", tagLenguaje)
                    .timeout(180 * 1000).cookies(cookies).post();

            opciones = doc.select("li>a");

            tagGoal = rb.getString(ResourceLeng.DELIVER_DELETE);

            for (Element aux : opciones) {
                if (aux.text().contains(tagGoal)) {
                    respuesta = aux.attr("href");
                    break;
                }
            }

        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            LogRecord logRegistro = new LogRecord(Level.SEVERE, "\n" + errors.toString());
            logRegistro.setSourceClassName("EventoTarea");
            logRegistro.setSourceMethodName("borrarArchivo");
            LogGeneral.log(logRegistro);
        } finally {
            return respuesta;
        }
    }

    /**
     * Metodo que gestiona la subida de un fichero al S.F de unizar, 
     *  dada una Url que esta asociada a una tarea, usuario y demas parametros
     * 
     * @param cookies Map de cookies que mantiene la sesion
     * @param url Url del S.F unizar
     * @param pathFile path del fichero en local
     * @param rb Indicara el idioma en el momento actual de generar el evento
     * @param tagLenguaje Tag del idioma actual (ISO 639-1)
     * @return Devuelve la Id, asociada al Fichero que acabamos de subir.
     * O null en caso de error
     */
    private static String subirArchivo(Map<String, String> cookies, String url, String pathFile, ResourceBundle rb, String tagLenguaje) {
        Document doc;
        File miFichero;
        Element opcion;
        Elements opciones;
        String respuesta = null;
        String urlAux = null;
        
        try {
            doc = Jsoup.connect(url)
                    .header("Accept-Language", tagLenguaje)
                    .timeout(180 * 1000).cookies(cookies).post();
            opciones = doc.select("div>a");            
            for (Element aux : opciones) {
                if (aux.text().contains(rb.getString(ResourceLeng.DELIVER_ADD))) {
                    urlAux = aux.attr("href");
                }
            }
            
            if (urlAux != null) {
                doc = Jsoup.connect(urlAux)
                        .header("Accept-Language", tagLenguaje)
                        .timeout(180 * 1000).cookies(cookies).post();
                opcion = doc.selectFirst(rb.getString(ResourceLeng.DELIVER_UPLOAD));
                urlAux = opcion.attr("href");
                
                if (urlAux != null) {
                    doc = Jsoup.connect(urlAux)
                            .timeout(180 * 1000).cookies(cookies).post();
                    
                    opcion = doc.selectFirst("form");
                    if (opcion.hasText()) {
                        urlAux = opcion.attr("action");
                        miFichero = new File(pathFile);
                        doc = Jsoup.connect(urlAux)
                                .header("Accept-Language", tagLenguaje)
                                .cookies(cookies)
                                .data("file", miFichero.getName(), new FileInputStream(miFichero))
                                .post();
                        urlAux = doc.selectFirst("div>a[target]").attr("href");
                        
                        //Extraer la ID del archivo
                        urlAux = urlAux.substring(urlAux.indexOf("itemid=") + "itemid=".length());
                        urlAux = urlAux.substring(0, urlAux.indexOf("&"));
                        respuesta = urlAux;
                    }
                }
            }
        } catch (IOException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            LogRecord logRegistro = new LogRecord(Level.SEVERE, "\n" + errors.toString());
            logRegistro.setSourceClassName("EventoTarea");
            logRegistro.setSourceMethodName("subirArchivo");
            LogGeneral.log(logRegistro);
        }finally{
            return respuesta;
        }
    }
}
