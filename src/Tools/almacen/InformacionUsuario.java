package Tools.almacen;

//#1 Static import
import aplicacion.HelloWorld;
import Tools.lenguaje.ResourceLeng;
import Tools.logger.LogGeneral;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

/**
 * 493
 *
 * @author Usuario
 */
public class InformacionUsuario {

    /**
     * Semaforo para gestionar el acceso sobre el archivo que contiene la
     * informacion del usuario.
     */
    private static final Semaphore semaforoFichero = new Semaphore(1);
    private static final String defaultPath = "./Dates/userInfo.txt";

    /**
     * Crea un fichero (inicializa) dados unos parametos, y los almacena
     * encriptados. Los parametros pueden ser vacios, pero eso podria dar
     * problemas mas adelante.
     *
     * @param user identificador del usuario
     * @param pass1 passwd para Moodle
     * @param pass2 passwd para NAS-TER
     * @param path path donde la app descargara contenido
     * @param useNas indicara si el usuario quiere o no utilizar Nas-Ter
     */
    public static void crearFichero(String user, String pass1, String pass2, String path, String useNas) {
        try {
            semaforoFichero.acquire();
            Path pathfile = Paths.get(defaultPath);
            Files.createDirectories(pathfile.getParent());
            Codificador codex = Codificador.getInstance();
            try (BufferedWriter writer = Files.newBufferedWriter(pathfile, Charset.forName("UTF-8"))) {
                writer.write(codex.encriptar("USER==" + user) + "\n");
                writer.write(codex.encriptar("PASS1==" + pass1) + "\n");
                writer.write(codex.encriptar("PASS2==" + pass2) + "\n");
                writer.write(codex.encriptar("PATH==" + path) + "\n");
                writer.write(codex.encriptar("USENAS==" + useNas) + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            //Se supone que al ejecutar la aplicacion ya deberias tener permisos de E/L 
            //  capturamos la excepcion por si acaso
//            Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaforoFichero.release();
        }
    }

    /**
     * @return Lista del contenido linea a linea del fichero almacenador
     * ,desencriptado. Null en caso de error
     */
    private static List<String> leerFichero() {
        LogRecord logRegistro = null;
        List<String> respuesta = new ArrayList<>();
        List<String> list = new ArrayList<>();
        Codificador codex = Codificador.getInstance();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(defaultPath))) {
            list = br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, errors.toString());
            logRegistro.setSourceMethodName("readFile");
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
    private static void reescribirFichero(List<String> data) {
        LogRecord logRegistro = null;
        Path pathfile = Paths.get(defaultPath);
        Codificador codex = Codificador.getInstance();

        try (BufferedWriter writer = Files.newBufferedWriter(pathfile, Charset.forName("UTF-8"))) {
            for (String line : data) {
                writer.write(codex.encriptar(line) + "\n");
            }
        } catch (IOException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, errors.toString());
            logRegistro.setSourceMethodName("rewriteFile");
            logRegistro.setSourceClassName(InformacionUsuario.class.getName());
        } finally {
            if (logRegistro != null) {
                LogGeneral.log(logRegistro);
            }
        }
    }

    /**
     * @return TRUE si el archivo existe y almenos uno de sus campos no es ""
     */
    public static boolean existenDatos() {
        boolean respuesta = false;
        List<String> aux = Arrays.asList("", "", "", "", "");
        List<String> aux2;
        if (Files.exists(Paths.get(defaultPath))) {
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
        List<String> respuesta = Arrays.asList(new String[5]);//new ArrayList<>();
        List<String> list;
        String aux;
        try {
            semaforoFichero.acquire();
            list = leerFichero();
            for (String data : list) {
                if (data == null) {
                    throw new Exception();
                } else if (data.contains("USER==")) {
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
//            Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            respuesta = null;
            LogGeneral.log(new LogRecord(Level.SEVERE, HelloWorld
                    .getResource().getString(ResourceLeng.TRACE_DATES_ERROR)));
        } finally {
            semaforoFichero.release();
            return respuesta;
        }
    }

    /**
     * Metodo que elimina el fichero donde se almacenan los datos.
     */
    public static void borrarFichero() {
        try {
            Path pathfile = Paths.get(defaultPath);
            Files.deleteIfExists(pathfile);
        } catch (IOException ex) {
//            Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return identificador del usuario almacenado.
     *
     * @throws NoSuchFieldException Si en el fichero no esta almacenado el campo
     * deseado
     */
    public static String getUser() throws NoSuchFieldException {
        String respuesta = null;
        try {
            semaforoFichero.acquire();
            List<String> aux = leerFichero();
            for (String line : aux) {
                if (line != null && line.contains("USER==")) {
                    respuesta = line.replaceAll("USER==", "");
                    break;
                }
            }
            if (respuesta == null) {
                throw new NoSuchFieldException();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaforoFichero.release();
            return respuesta;
        }
    }
    /**
     *
     * @param newData identificador del usuario, que se quiere cambiar por el
     * almacenado. newData != null && newData != ""
     */
    public static void setUser(String newData) {
        boolean isnew = true;
        if (newData != null && !newData.equals("")) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (aux.get(i).contains("USER==")) {
                        aux.set(i, "USER==" + newData);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("USER==" + newData);
                }
                reescribirFichero(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
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
    public static String getPass1() throws NoSuchFieldException {
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
            if (respuesta == null) {
                throw new NoSuchFieldException();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaforoFichero.release();
            return respuesta;
        }
    }
    /**
     *
     * @param newData passwd para Moodle, que se quiere cambiar por el
     * almacenado. newData != null && newData != ""
     */
    public static void setPass1(String newData) {
        boolean isnew = true;
        if (newData != null && !newData.equals("")) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (aux.get(i).contains("PASS1==")) {
                        aux.set(i, "PASS1==" + newData);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("PASS1==" + newData);
                }
                reescribirFichero(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
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
    public static String getPass2() throws NoSuchFieldException {
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
            if (respuesta == null) {
                throw new NoSuchFieldException();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaforoFichero.release();
            System.err.println("Pass2 " + respuesta);
            return respuesta;
        }
    }
    /**
     *
     * @param newData passwd para NAS-TER, que se quiere cambiar por el
     * almacenado. newData != null && newData != ""
     */
    public static void setPass2(String newData) {
        boolean isnew = true;
        if (newData != null && !newData.equals("")) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (aux.get(i).contains("PASS2==")) {
                        aux.set(i, "PASS2==" + newData);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("PASS2==" + newData);
                }
                reescribirFichero(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
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
            if (respuesta == null) {
                throw new NoSuchFieldException();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaforoFichero.release();
            return respuesta;
        }
    }
    /**
     * PRE: el usuario no ha modificado el archivo (contenido/orden)
     *
     * @param newData path de descarga, que se quiere cambiar por el almacenado.
     * newData != null && newData != ""
     */
    public static void setPath(String newData) {
        boolean isnew = true;
        if (newData != null && !newData.equals("")) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (aux.get(i).contains("PATH==")) {
                        aux.set(i, "PATH==" + newData);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("PATH==" + newData);
                }
                reescribirFichero(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaforoFichero.release();
            }
        }
    }

    
    /**
     * PRE: el usuario no ha modificado el archivo (contenido/orden)
     *
     * @return path de descarga almacenado en el fichero.
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
//            Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaforoFichero.release();
            return respuesta;
        }
    }
    /**
     *
     * @param newData path de descarga, que se quiere cambiar por el almacenado.
     * newData != null && newData != ""
     */
    public static void setUseNas(String newData) {
        boolean isnew = true;
        if (newData != null && !newData.equals("")) {
            try {
                semaforoFichero.acquire();
                List<String> aux = leerFichero();
                for (int i = 0; i < aux.size(); i++) {
                    if (aux.get(i).contains("USENAS==")) {
                        aux.set(i, "USENAS==" + newData);
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    aux.add("USENAS==" + newData);
                }
                reescribirFichero(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(InformacionUsuario.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaforoFichero.release();
            }
        }
    }
}