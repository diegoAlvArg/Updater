package wrapper.init;

import application.InterfaceController;
import wrapper.tree.Node;
import wrapper.tree.TypeNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Diego
 * 
 * @version 1.0
 * Opciones a las que se le da soporte, una actualizacion total o una parcial.
 * 
 * @version 1.0.1
 * Al incorporarlo a la aplicacion han cambiado varias cosas, como los parametros
 *  y la privacidad. Como lo lanzamos a traves de un Thread no contruimos el
 *  objeto y debemos establecer el path para que luego los Task puedan pedir
 *  el dato.
 * 
 * @version 1.0.2
 * El tema de la IU(controlador) se podria hacer de otra mas 
 *  elegante, sin embargo creo que de esta forma no hacemos tantos cuellos 
 *  de botella, por lo que se imita el mismo modelo que el path.
 */
public class Opciones {

//    private static List<Node> matricula = new ArrayList<Node>();
    private static String path;
    private static InterfaceController iuControl;
    /**
     * 
     * @param user identificador del usuario con el que logeamos en moodle
     * @param pass passwd del usuario con el que logeamos en moodle
     * @param year anio sobre el que se realiza la actuializacion
     * @param path path del local sobre el que realizaremos la sincronizacion
     * @param iuControl clase que maneja el control de la IU
     * 
     * 
     * @throws ClientProtocolException - in case of an http protocol error
     * @throws IOException - in case of a problem or the connection was aborted
     */
    public static void realizarActualizacionTotal(String user, String pass, String year, String path, InterfaceController iuControl) throws ClientProtocolException, IOException {
        List<Node> matricula = new ArrayList<Node>();
        Opciones.path = path;
        Opciones.iuControl = iuControl;
        long time_start, time_end;
        time_start = System.currentTimeMillis();

        Connection.Response res = Jsoup.connect("https://moodle2.unizar.es/add/login/index.php")
                .data("username", user, "password", pass)
                .method(Connection.Method.POST)
                .execute();
        Map<String, String> cookies = res.cookies();

        if (cookies.size() != 1) {
            System.out.println("Credenciales OK");
            Document doc = res.parse();
            Elements titles = doc.select("li>a:contains" + year);
            if (!titles.isEmpty()) {
                for (int i = 0; i < titles.size(); i++) {
                    Element d = titles.get(i);
                    String name = d.select("a").text();
                    String url = d.select("a").attr("href");
                    matricula.add(new Node(url, name, TypeNode.CURSO, cookies));
                }
                //**********CONSTRUCCION DE UN TASK PARA CADA CURSO*****************
                ExecutorService executor = Executors.newFixedThreadPool(matricula.size());
                List<TaskWrap> callables = new ArrayList<>();

                for (int i = 0; i < matricula.size(); i++) {
                    callables.add(new TaskWrap(matricula.get(i), i + 1));
                }
//                System.err.println("Lanzando Task");
                //**********COMPROBACION DE FINALIZACION****************************
                try {
                    List<Future<Void>> futures = executor.invokeAll(callables);
//                    for(TaskWrap element :callables){
//                        element.listar();
//                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Opciones.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    executor.shutdown();
                }
            }
        } else {
            System.out.println("Credenciales erroneas");
        }

        time_end = System.currentTimeMillis();
        System.out.println("\n\n--the task has taken " + (time_end - time_start) + " milliseconds");
    }

    /**
     * El param curso identifica al curso en el orden que lo encuentras en 
     *  moodle. Por ahora solo para las pruebas, pero dejamos el method() por si
     *  acaso para un futuro.
     * 
     * @param curso selector de curso. 0- DCU-DM 1- Sist. ayuda toma decisiones
     * 2- Seg. Informatica 3- Sist. y tegnologias web 4- Ing. Sw 5- Sist.
     * legados 6- Diseño y administracion de redes 7- Comercio Electornico
     * @param user identificador del usuario con el que logeamos en moodle
     * @param pass passwd del usuario con el que logeamos en moodle
     * @param year anio sobre el que se realiza la actuializacion
     * @param path path del local sobre el que realizaremos la sincronizacion
     * @param iuControl clase que maneja el control de la IU
     *
     * @throws ClientProtocolException - in case of an http protocol error
     * @throws IOException - in case of a problem or the connection was aborted
     */
    public static void realizarActualizacionIndividual(int curso, String user, String pass, String year, String path, InterfaceController iuControl) throws ClientProtocolException, IOException {
        List<Node> matricula = new ArrayList<Node>();
        Opciones.path = path;
        Opciones.iuControl = iuControl;
        long time_start, time_end;
        time_start = System.currentTimeMillis();
        Connection.Response res = Jsoup.connect("https://moodle2.unizar.es/add/login/index.php")
                .data("username", user, "password", pass)
                .method(Connection.Method.POST)
                .execute();
        Map<String, String> cookies = res.cookies();

        if (cookies.size() != 1) {
//            System.out.println("Credenciales OK");
            Document doc = res.parse();
            Elements titles = doc.select("li>a:contains" + year);
            if (!titles.isEmpty()) {
                for (int i = 0; i < titles.size(); i++) {
                    Element d = titles.get(i);
                    String name = d.select("a").text();
                    String url = d.select("a").attr("href");
                    matricula.add(new Node(url, name, TypeNode.CURSO, cookies));
                }
                //**********CONSTRUCCION DE UN TASK PARA CADA CURSO*****************
                ExecutorService executor = Executors.newFixedThreadPool(1);
                List<TaskWrap> callables = new ArrayList<>();
                callables.add(new TaskWrap(matricula.get(curso), 1));

                //**********COMPROBACION DE FINALIZACION****************************
                try {
                    List<Future<Void>> futures = executor.invokeAll(callables);
//                    for(TaskWrap element :callables){
//                        element.listar();
//                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Opciones.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    executor.shutdown();
                }
            }
        } else {
            System.out.println("Credenciales erroneas");
        }

        time_end = System.currentTimeMillis();
        System.out.println("\n\n--the task has taken " + (time_end - time_start) + " milliseconds");

    }

    /**
     *
     * @return path guardado
     */
    public static String getDownloadPath() {
        return path;
    }
    
    /**
    * 
    * @return iuController 
    */
    public static InterfaceController getIU(){
        return iuControl;
    }
}
