package sincronizacion.moodle.inicio;

//#1 Static import
import aplicacion.controlador.MainController;
import sincronizacion.moodle.estructura.Nodo;
import sincronizacion.moodle.estructura.TipoNodo;
import tools.logger.LogSincronizacion;
//#3 Third party
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//#4 Java
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * 192
 * @author Diego Alvarez
 *
 * @version 1.0 OpcionesSyncMoodle a las que se le da soporte, una actualizacion
 * total o una parcial.
 *
 * @version 1.0.1 Al incorporarlo a la aplicacion han cambiado varias cosas,
 * como los parametros y la privacidad. Como lo lanzamos a traves de un Thread
 * no contruimos el objeto y debemos establecer el path para que luego los Task
 * puedan pedir el dato.
 *
 * @version 1.0.2 El tema de la IU(controlador) se podria hacer de otra mas
 * elegante, sin embargo creo que de esta forma no hacemos tantos cuellos de
 * botella, por lo que se imita el mismo modelo que el path.
 */
public class OpcionesSyncMoodle {
    private static String pathLocal;
    private static MainController iuControl;

    /**
     *
     * @param usuario identificador del usuario con el que logeamos en moodle
     * @param contrasenia passwd del usuario con el que logeamos en moodle
     * @param anio anio sobre el que se realiza la actuializacion
     * @param pathLocal path del local sobre el que realizaremos la
     * sincronizacion
     * @param iuControl clase que maneja el control de la IU
     *
     *
     * @throws ClientProtocolException - in case of an http protocol error
     * @throws IOException - in case of a problem or the connection was aborted
     */
    public static void realizarActualizacionTotal(String usuario, String contrasenia, String anio, String pathLocal, MainController iuControl)
            throws ClientProtocolException, IOException {
        OpcionesSyncMoodle.pathLocal = pathLocal;
        OpcionesSyncMoodle.iuControl = iuControl;
        LogRecord logRegistro = null;
        
        Connection.Response res = Jsoup.connect("https://moodle2.unizar.es/add/")
                .method(Connection.Method.GET)
                .execute();
        Map<String, String> cookies = res.cookies();
        String autLogin = res.parse().selectFirst("input[name=logintoken]").attr("value");
        
        res = Jsoup.connect("https://moodle2.unizar.es/add/login/index.php")
                .cookies(cookies)
                .data("username", usuario)
                .data("password", contrasenia)
                .data("logintoken", autLogin)
                .timeout(180000)
                .method(Connection.Method.POST)
                .execute();
        
        cookies = res.cookies();
//        System.out.println(cookies);
//        System.out.println("-----");
        if (cookies.size() != 1) {
            Document doc = res.parse();
//            System.out.println(doc);
            Elements titles = doc.select("li>a:contains" + anio);
            List<Nodo> matriculas = new ArrayList();
            
            if (!titles.isEmpty()) {
                for (int indexA = 0; indexA < titles.size(); indexA++) {
                    Element d = (Element) titles.get(indexA);
                    String name = d.select("a").text();
                    String url = d.select("a").attr("href");
                    matriculas.add(new Nodo(url, name, TipoNodo.CURSO, cookies));
                }
                //**********CONSTRUCCION DE UN TASK PARA CADA CURSO*****************
                ExecutorService executor = Executors.newFixedThreadPool(matriculas.size());
                List<TareaWrapper> callables = new ArrayList();
                for (int indexB = 0; indexB < matriculas.size(); indexB++) {
                    callables.add(new TareaWrapper((Nodo) matriculas.get(indexB), indexB + 1));
                }
                //**********COMPROBACION DE FINALIZACION****************************
                try {
                    List<Future<Void>> futures = executor.invokeAll(callables);
                } catch (InterruptedException ex) {
                    StringWriter errors = new StringWriter();
                    ex.printStackTrace(new PrintWriter(errors));
                    logRegistro = new LogRecord(Level.SEVERE, errors.toString());
                    logRegistro.setSourceMethodName("realizarActualizacionTotal");
                    logRegistro.setSourceClassName(OpcionesSyncMoodle.class.getName());
                } finally {
                    executor.shutdown();
                    if (logRegistro != null) {
                        LogSincronizacion.log(logRegistro);
                    }
                }
            }{
                System.out.println("No hay cursos");
            }
        } else {
            iuControl.borrarUsuario();
        }
    }

    /**
     * El param curso identifica al curso en el orden que lo encuentras en
     * moodle. Por ahora solo para las pruebas, pero dejamos el method() por si
     * acaso para un futuro.
     *
     * @param curso selector de curso. 0- DCU-DM 1- Sist. ayuda toma decisiones
     * 2- Seg. Informatica 3- Sist. y tegnologias web 4- Ing. Sw 5- Sist.
     * legados 6- Diseño y administracion de redes 7- Comercio Electornico
     * @param usuario identificador del usuario con el que logeamos en moodle
     * @param contrasenia passwd del usuario con el que logeamos en moodle
     * @param anio anio sobre el que se realiza la actuializacion
     * @param pathLocal path del local sobre el que realizaremos la
     * sincronizacion
     * @param iuControl clase que maneja el control de la IU
     *
     * @throws ClientProtocolException - in case of an http protocol error
     * @throws IOException - in case of a problem or the connection was aborted
     */
    public static void realizarActualizacionIndividual(int curso, String usuario, String contrasenia, String anio, String pathLocal, MainController iuControl)
            throws ClientProtocolException, IOException {
        OpcionesSyncMoodle.pathLocal = pathLocal;
        OpcionesSyncMoodle.iuControl = iuControl;
        LogRecord logRegistro = null;

//        Connection.Response res = Jsoup.connect("https://moodle2.unizar.es/add/login/index.php").data(new String[]{"username", usuario, "password", contrasenia}).timeout(180000).method(Connection.Method.POST).execute();
//        Map<String, String> cookies = res.cookies();
        Connection.Response res = Jsoup.connect("https://moodle2.unizar.es/add/")
                .method(Connection.Method.GET)
                .execute();
        Map<String, String> cookies = res.cookies();
        String autLogin = res.parse().selectFirst("input[name=logintoken]").attr("value");
        
        res = Jsoup.connect("https://moodle2.unizar.es/add/login/index.php")
                .cookies(cookies)
                .data("username", usuario)
                .data("password", contrasenia)
                .data("logintoken", autLogin)
                .timeout(180000)
                .method(Connection.Method.POST)
                .execute();
        
        cookies = res.cookies();
        
        
        if (cookies.size() != 1) {
            Document doc = res.parse();
            Elements titles = doc.select("li>a:contains" + anio);
            List<Nodo> matricula = new ArrayList();
            if (!titles.isEmpty()) {
                String name;
                for (int i = 0; i < titles.size(); i++) {
                    Element d = (Element) titles.get(i);
                    name = d.select("a").text();
                    String url = d.select("a").attr("href");
                    matricula.add(new Nodo(url, name, TipoNodo.CURSO, cookies));
                }
                //**********CONSTRUCCION DE UN TASK PARA CADA CURSO*****************
                ExecutorService executor = Executors.newFixedThreadPool(1);
                List<TareaWrapper> callables = new ArrayList();
                callables.add(new TareaWrapper((Nodo) matricula.get(curso), 1));
                
                //**********COMPROBACION DE FINALIZACION****************************
                try {
                    List<Future<Void>> futures = executor.invokeAll(callables);
                } catch (InterruptedException ex) {
                    StringWriter errors = new StringWriter();
                    ex.printStackTrace(new PrintWriter(errors));
                    logRegistro = new LogRecord(Level.SEVERE, errors.toString());
                    logRegistro.setSourceMethodName("realizarActualizacionIndividual");
                    logRegistro.setSourceClassName(OpcionesSyncMoodle.class.getName());
                } finally {
                    executor.shutdown();
                    if (logRegistro != null) {
                        LogSincronizacion.log(logRegistro);
                    }
                }
            }
        } else {
            iuControl.borrarUsuario();
        }
    }

    /**
     *
     * @return path guardado
     */
    public static String getPathDescarga() {
        return pathLocal;
    }

    /**
     *
     * @return iuController
     */
    public static MainController getIU() {
        return iuControl;
    }
}
