package tools.almacen;

//#1 Static import
import aplicacion.MainClass;
import tools.logger.LogGeneral;
//#4 Java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

/**
 * 490 (-3)
 *
 * @author Diego Alvarez
 */
public class InformacionUsuario {
    /**
     * Semaforo para gestionar el acceso sobre el archivo que contiene la
     * informacion del usuario.
     */
    private static final Semaphore semaforoFichero = new Semaphore(1);
    private static final String defaultPath = "./Dates/userInfo.txt";

    public static void borrarFichero() {
        try {
            Path pathfile = Paths.get("./Dates/userInfo.txt", new String[0]);
            Files.deleteIfExists(pathfile);
        } catch (IOException localIOException) {
        }
    }

    
    /**
     * Crea un fichero (inicializa) dados unos parametos, y los almacena
     * encriptados. Los parametros pueden ser vacios, pero eso podria dar
     * problemas mas adelante.
     *
     * @param usuario identificador del usuario
     * @param pass1 passwd para Moodle
     * @param pass2 passwd para NAS-TER
     * @param path path donde la app descargara contenido
     * @param useNas indicara si el usuario quiere o no utilizar Nas-Ter
     */
    public static void crearFichero(String usuario, String pass1, String pass2, String path, String useNas) {
        try {
            semaforoFichero.acquire();
            Path pathfile = Paths.get("./Dates/userInfo.txt", new String[0]);
            Files.createDirectories(pathfile.getParent(), new FileAttribute[0]);
            Codificador codex = Codificador.getInstance();
            try (BufferedWriter writer = Files.newBufferedWriter(pathfile, 
                    Charset.forName("UTF-8")/*, new OpenOption[0]*/);){
                                  
                writer.write(codex.encriptar(new StringBuilder().append("USER==").append(usuario).toString()) + "\n");
                writer.write(codex.encriptar(new StringBuilder().append("PASS1==").append(pass1).toString()) + "\n");
                writer.write(codex.encriptar(new StringBuilder().append("PASS2==").append(pass2).toString()) + "\n");
                writer.write(codex.encriptar(new StringBuilder().append("PATH==").append(path).toString()) + "\n");
                writer.write(codex.encriptar(new StringBuilder().append("USENAS==").append(useNas).toString()) + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (InterruptedException ex) {
            
        } catch (IOException ex) {
            //Se supone que al ejecutar la aplicacion ya deberias tener permisos de E/L 
            //  capturamos la excepcion por si acaso
        } finally {
            semaforoFichero.release();
        }
    }
 
    /**
     * @return TRUE si el archivo existe y almenos uno de sus campos no es ""
     */
    public static boolean existenDatos() {
        boolean respuesta = false;
        List<String> aux = Arrays.asList(new String[]{"", "", "", "", ""});
        List<String> aux2;
        if (Files.exists(Paths.get("./Dates/userInfo.txt"/*, new String[0]), new LinkOption[0]*/))) {
            aux2 = getDatos();
            if (aux2 != null) {
                respuesta = !aux2.equals(aux);
            }
        }
        return respuesta;
    }

    /**
     * @return List de los "atributos" listo para usar.
     */
    private static List<String> getDatos() {
        List<String> respuesta = Arrays.asList(new String[5]);
        List<String> list;
        String aux;
        try {
            semaforoFichero.acquire();
            list = leerFichero();
            for (String data : list) {
                if (data == null) {
                    throw new Exception();
                }else if (data.contains("USER==")) {
                    aux = data.replaceAll("USER==", "");
                    respuesta.set(0, aux);
                } else if (data.contains("PASS1==")) {
                    aux = data.replaceAll("PASS1==", "");
                    respuesta.set(1, aux);
                } else if (data.contains("PASS2==")) {
                    aux = data.replaceAll("PASS2==", "");
                    respuesta.set(2, aux);
                } else if (data.contains("PATH==")) {
                    aux = data.replaceAll("PATH==", "");
                    respuesta.set(3, aux);
                } else if (data.contains("USENAS==")) {
                    aux = data.replaceAll("USENAS==", "");
                    respuesta.set(4, aux);
                } else {
                    throw new Exception();
                }
            }
        } catch (InterruptedException ex) {

        } catch (Exception e) {
            respuesta = null;
            LogGeneral.log(new LogRecord(Level.SEVERE,
                    MainClass.getResource().getString("trace_dates_error")));
        } finally {
            semaforoFichero.release();
            return respuesta;
        }

    }

    /**
     * @return Lista del contenido linea a linea del fichero almacenador
     * ,desencriptado. Null en caso de error
     */
    private static List<String> leerFichero() {
        LogRecord logRegistro = null;
        List<String> respuesta = new ArrayList();
        List<String> list = new ArrayList();
        Codificador codex = Codificador.getInstance();
        
        try (BufferedReader br = Files.newBufferedReader(Paths.get("./Dates/userInfo.txt"));) {
            list = (List) br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, errors.toString());
            logRegistro.setSourceMethodName("leerFichero");
            logRegistro.setSourceClassName(InformacionUsuario.class.getName());
        } finally {
            if (logRegistro != null) {
                LogGeneral.log(logRegistro);
            }
        }
        for (String element : list) {
            respuesta.add(codex.desencriptar(element));
        }
        return respuesta;
    }

    /**
     * @param data Lista de lineas que se escribiran en el fichero almacenado
     */
    private static void reescribirFichero(List<String> datos) {
        LogRecord logRegistro = null;
        Path pathfile = Paths.get("./Dates/userInfo.txt");
        Codificador codex = Codificador.getInstance();
        
        try (BufferedWriter writer = Files.newBufferedWriter(pathfile, Charset.forName("UTF-8"));){
            for (String line : datos) {
                writer.write(codex.encriptar(line) + "\n");
            }
        } catch (IOException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, errors.toString());
            logRegistro.setSourceMethodName("reescribirFichero");
            logRegistro.setSourceClassName(InformacionUsuario.class.getName());
        } finally {
            if (logRegistro != null) {
                LogGeneral.log(logRegistro);
            }
        }
    }

    /**
     * PRE: el usuario no ha modificado el archivo (contenido/orden)
     *
     * @return path de descarga almacenado en el fichero.
     */
    public static String getUsuario() throws NoSuchFieldException {
        String respuesta = null;
        try {
            semaforoFichero.acquire();
            List<String> aux = leerFichero();
            for (String line : aux) {
                if ((line != null) && (line.contains("USER=="))) {
                    respuesta = line.replaceAll("USER==", "");
                    break;
                }
            }
        } catch (InterruptedException localInterruptedException) {

        } finally {
            semaforoFichero.release();
            if (respuesta == null) {
                throw new NoSuchFieldException();
            }else{
                return respuesta;
            }
        }

    }
    /**
     *
     * @param nuevoDato path de descarga, que se quiere cambiar por el almacenado.
     * newData != null && newData != ""
     */
    public static void setUsuario(String nuevoDato) {
        boolean isnew = true;
        if ((nuevoDato != null) && (!nuevoDato.equals(""))) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (((String) aux.get(i)).contains("USER==")) {
                        aux.set(i, "USER==" + nuevoDato);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("USER==" + nuevoDato);
                }
                reescribirFichero(aux);
            } catch (InterruptedException ex) {
            } finally {
                semaforoFichero.release();
            }
        }
    }

    /**
     *
     * @return passwd para Moodle almacenado.
     *
     * @throws NoSuchFieldException Si en el fichero no esta almacenado el campo
     * deseado
     */
    public static String getPassM() throws NoSuchFieldException {
        String respuesta = null;
        try {
            semaforoFichero.acquire();
            List<String> aux = leerFichero();
            for (String line : aux) {
                if (line.contains("PASS1==")) {
                    respuesta = line.replaceAll("PASS1==", "");
                    break;
                }
            }
        } catch (InterruptedException localInterruptedException) {

        } finally {
            semaforoFichero.release();
            if (respuesta == null) {
                throw new NoSuchFieldException();
            }else{
                return respuesta;
            }
        }

    }
    /**
     *
     * @param nuevoDato passwd para Moodle, que se quiere cambiar por el
     * almacenado. newData != null && newData != ""
     */
    public static void setPassM(String nuevoDato) {
        boolean isnew = true;
        
        if ((nuevoDato != null) && (!nuevoDato.equals(""))) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (((String) aux.get(i)).contains("PASS1==")) {
                        aux.set(i, "PASS1==" + nuevoDato);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("PASS1==" + nuevoDato);
                }
                reescribirFichero(aux);
            } catch (InterruptedException ex) {
            } finally {
                semaforoFichero.release();
            }
        }
    }

    /**
     *
     * @return passwd para NAS-TER almacenado.
     *
     * @throws NoSuchFieldException Si en el fichero no esta almacenado el campo
     * deseado
     */
    public static String getPassN() throws NoSuchFieldException {
        String respuesta = null;
        try {
            semaforoFichero.acquire();
            List<String> aux = leerFichero();
            for (String line : aux) {
                if (line.contains("PASS2==")) {
                    respuesta = line.replaceAll("PASS2==", "");
                    break;
                }
            }
        } catch (InterruptedException localInterruptedException) {
            
        } finally {
            semaforoFichero.release();
            if (respuesta == null) {
                throw new NoSuchFieldException();
            }else{
                return respuesta;
            }
        }

    }
    /**
     *
     * @param nuevoDato passwd para NAS-TER, que se quiere cambiar por el
     * almacenado. newData != null && newData != ""
     */
    public static void setPassN(String nuevoDato) {
        boolean isnew = true;
        if ((nuevoDato != null) && (!nuevoDato.equals(""))) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (((String) aux.get(i)).contains("PASS2==")) {
                        aux.set(i, "PASS2==" + nuevoDato);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("PASS2==" + nuevoDato);
                }
                reescribirFichero(aux);
            } catch (InterruptedException ex) {
            } finally {
                semaforoFichero.release();
            }
        }
    }
    
    /**
     *
     * @return path de descarga almacenado en el fichero.
     *
     * @throws NoSuchFieldException Si en el fichero no esta almacenado el campo
     * deseado
     */
    public static String getPath() throws NoSuchFieldException {
        String respuesta = null;
        try {
            semaforoFichero.acquire();
            List<String> aux = leerFichero();
            for (String line : aux) {
                if (line.contains("PATH==")) {
                    respuesta = line.replaceAll("PATH==", "");
                    break;
                }
            }
        } catch (InterruptedException localInterruptedException) {
            
        } finally {
            semaforoFichero.release();
            if (respuesta == null) {
                throw new NoSuchFieldException();
            }else{
                return respuesta;
            }
        }
        
    }
    /**
     *
     * @param nuevoDato path de descarga, que se quiere cambiar por el almacenado.
     */
    public static void setPath(String nuevoDato) {
        boolean isnew = true;
        if ((nuevoDato != null) && (!nuevoDato.equals(""))) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (((String) aux.get(i)).contains("PATH==")) {
                        aux.set(i, "PATH==" + nuevoDato);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("PATH==" + nuevoDato);
                }
                reescribirFichero(aux);
            } catch (InterruptedException ex) {
            } finally {
                semaforoFichero.release();
            }
        }
    }

    /**
     *
     * @return True si el usuario quiere usar Nas-Ter. False en caso contrario 
     *  o error
     *
     * @throws NoSuchFieldException Si en el fichero no esta almacenado el campo
     * deseado
     */
    public static boolean getUseNas() {
        boolean respuesta = false;
        try {
            semaforoFichero.acquire();
            List<String> aux = leerFichero();
            for (String line : aux) {
                if (line.contains("USENAS==")) {
                    respuesta = Boolean.parseBoolean(line.replaceAll("USENAS==", ""));
                    break;
                }
            }
        } catch (Exception ex) {
            
        } finally {
            semaforoFichero.release();
            return respuesta;
        }
        
    }
    /**
     * 
     * @param nuevoDato string de boolean que expresara el querer o no 
     * usar Nas-ter
     */
    public static void setUseNas(String nuevoDato) {
        boolean isnew = true;
        if ((nuevoDato != null) && (!nuevoDato.equals(""))) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (((String) aux.get(i)).contains("USENAS==")) {
                        aux.set(i, "USENAS==" + nuevoDato);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("USENAS==" + nuevoDato);
                }
                reescribirFichero(aux);
            } catch (InterruptedException localInterruptedException) {
            } finally {
                semaforoFichero.release();
            }
        }
    }
}
