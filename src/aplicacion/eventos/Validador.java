package aplicacion.eventos;

//#3 Third party
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
//#4 Java
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 110
 *
 * @author Usuario
 */
public class Validador {

    /**
     * In order to update this application must have READ,WRITE AND CREATE
     * permissions on the current folder
     * 
     * @param path path del local
     * @return 
     */
    public static boolean checkPermissions(String path) {
        boolean respuesta = false;
        //Check for permission to Create
        File sample;
        try {
            sample = new File(Paths.get(path + File.separator + "empty123123124122354345436.txt").toString());
            /*
            * Create and delete a dummy file in order to check file
            * permissions. Maybe there is a safer way for this check.
            */
            sample.createNewFile();
            respuesta = sample.canRead() && sample.canWrite();
            sample.delete();
        } catch (IOException e) {
            //Error message shown to user. Operation is aborted
            respuesta = false;
        } finally {
            return respuesta;
        }
    }

    /**
     *
     * Comprueba que es posible el acceso dado param usuario contrasenia
     *
     * @param usuario
     * @param contrasenia
     * @return 1-moodle caido, 2- credenciales erroneas, 3- credenciales Ok
     */
    public static int validarCredencialesMoodle(String usuario, String contrasenia) {
        int respuesta = 1;
        String title = "";
        String autLogin;
        Map<String, String> cookies;
        Connection.Response res;
        try {
            res = Jsoup.connect("https://moodle2.unizar.es/add/")
                .method(Connection.Method.GET)
                .execute();
            cookies = res.cookies();
            autLogin = res.parse().selectFirst("input[name=logintoken]").attr("value");
            
            res = Jsoup.connect("https://moodle2.unizar.es/add/login/index.php")
                    .cookies(cookies)
                    .data("username", usuario)
                    .data("password", contrasenia)
                    .data("logintoken", autLogin)
                    .timeout(18 * 1000)
                    .method(Connection.Method.POST)
                    .execute();
            
            Document doc = res.parse();
            title = doc.select("head>title").text();
            if (title.contains("ADD Unizar - Moodle 2")) {
                respuesta = 2;
            } else {
                respuesta = 3;
            }
        } catch (IOException ex) {
            // Se rechazo por un TimeOut lo que significa que moodle esta caido
        } finally {
//            System.err.println("\tReturnning " + respuesta);
//            System.err.println("\tTitle " + title);
            return respuesta;
        }
    }

    /**
     *
     * @param usuario
     * @param contrasenia
     * @return 1-NasTer caido, 2- credenciales erroneas, 3- credenciales Ok
     */
    public static int validarCredencialesNaster(String usuario, String contrasenia) {
        int respuesta = 3;
//        System.err.println("User " + usuario + " ,Pass " + contrasenia);
        try {
            Sardine sardineCon = SardineFactory.begin(usuario, contrasenia);
            URI url = URI.create("https://nas-ter.unizar.es/alumnos/" + usuario);
            sardineCon.exists(url.toString());

        } catch (SardineException e) {
            // puede deberse a credenciales erroneas, o que el usuario no este 
            //  dado de alta (no podemos saber)
            respuesta = 2;
        } catch (IOException e) {
            // Salta el timeOut, parece que no se extablece la conexion
            respuesta = 1;
        } finally {
            System.err.println("Nas " + respuesta);
            return respuesta;
        }
    }
}
