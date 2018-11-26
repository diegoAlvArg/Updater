package sincronizacion.moodle.estructura;

/**
 *
 * @author Diego Alvarez
 * @version 1.0 Clase para normalizar nombres, para el S.F
 * 
 * @see <a href="http://www.rgagnon.com/javadetails/java-check-if-a-filename-is-valid.html">Link 01</a>
 * @see <a href="https://en.wikipedia.org/wiki/Filename">Link 02</a>
 * @see <a href="https://stackoverflow.com/questions/893977/java-how-to-find-out-whether-a-file-name-is-valid">Link 03</a>
 */
public class Normalizador {

    private static final String[] restricciones = {"\\\\", "\\?", "¿", "\\*", "\"", "\\|", "\n", "\r", "\t", "\0", "\f", "%", "'", "#"};

    public static String normalizarNombre(String nombre) {
        String respuesta = nombre;
        respuesta = respuesta.replaceAll("<", "");
        respuesta = respuesta.replaceAll(">", "");
        respuesta = respuesta.replaceAll(" ", "_");
        respuesta = respuesta.replaceAll("/", "-");
        respuesta = respuesta.replaceAll(":", ",");
        for (String au : restricciones) {
            respuesta = respuesta.replaceAll(au, "");
        }
        return respuesta;
    }
}
