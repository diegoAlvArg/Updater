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
 *
 * @author Usuario 58,545 ms Listando 54,438 ms Sin Listar
 */
public class main {

//    private static List<Node> matricula = new ArrayList<Node>();
    private static String path;
    private static InterfaceController iu;
    /**
     * @param year (2016-2017)
     * @throws ClientProtocolException - in case of an http protocol error
     * @throws IOException - in case of a problem or the connection was aborted
     */
    public static void performUpdate(String user, String pass, String year, String newpath, InterfaceController newIu) throws ClientProtocolException, IOException {
        List<Node> matricula = new ArrayList<Node>();
        main.path = newpath;
        main.iu = newIu;
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
                System.err.println("Lanzando Task");
                //**********COMPROBACION DE FINALIZACION****************************
                try {
                    List<Future<Void>> futures = executor.invokeAll(callables);
//                    for(TaskWrap element :callables){
//                        element.listar();
//                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
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
     * @param _curso selector de curso. 0- DCU-DM 1- Sist. ayuda toma decisiones
     * 2- Seg. Informatica 3- Sist. y tegnologias web 4- Ing. Sw 5- Sist.
     * legados 6- Diseño y administracion de redes 7- Comercio Electornico
     *
     *
     * @throws ClientProtocolException - in case of an http protocol error
     * @throws IOException - in case of a problem or the connection was aborted
     */
    public static void performUpdateSingle(int _curso, String user, String pass, String year, String newpath, InterfaceController newIu) throws ClientProtocolException, IOException {
        List<Node> matricula = new ArrayList<Node>();
        main.path = newpath;
        main.iu = newIu;
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
                ExecutorService executor = Executors.newFixedThreadPool(1);
                List<TaskWrap> callables = new ArrayList<>();
                callables.add(new TaskWrap(matricula.get(_curso), 1));
//                Callable mio = new  (matricula.get(_curso), _curso + 1);

                //**********COMPROBACION DE FINALIZACION****************************
                try {
                    List<Future<Void>> futures = executor.invokeAll(callables);
//                    for(TaskWrap element :callables){
//                        element.listar();
//                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
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

    public static String getDownloadPath() {
        return path;
    }
   
    public static InterfaceController getIU(){
        return iu;
    }
}
