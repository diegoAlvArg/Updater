package Sincronizacion.Moodle.estructura;

/**
 *
 * @author Diego
 * @version 1.0 Clase para normalizar nombres, para el S.F
 * 
 * @see http://www.rgagnon.com/javadetails/java-check-if-a-filename-is-valid.html
 * @see https://en.wikipedia.org/wiki/Filename
 * @see https://stackoverflow.com/questions/893977/java-how-to-find-out-whether-a-file-name-is-valid
 */
public class Normalizador {
    private static final String[] banned = {"\\\\", "\\?", "¿", "\\*", "\"", "\\|", "\n", "\r", "\t", "\0", "\f", "%", "'", "#"};
    
    public static String normalizarNombre(String name){
        String respuesta = name;
        respuesta = respuesta.replaceAll("<", "");
        respuesta = respuesta.replaceAll(">", "");
        respuesta = respuesta.replaceAll(" ", "_");
        respuesta = respuesta.replaceAll("/", "-");
        respuesta = respuesta.replaceAll(":", ",");
        
        for (String au : banned) {
            respuesta = respuesta.replaceAll(au, "");

        }
        
        return respuesta;
    }
}
