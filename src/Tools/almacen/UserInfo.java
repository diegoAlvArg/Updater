package Tools.almacen;

import Tools.lenguaje.ResourceLeng;
import application.HelloWorld;
import Tools.logger.LogGeneral;
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
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Usuario
 */
public class UserInfo {

    /**
     * Semaforo para gestionar el acceso sobre el archivo que contiene la
     * informacion del usuario.
     */
    private static final Semaphore semaphore = new Semaphore(1);
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
    public static void createFile(String user, String pass1, String pass2, String path, String useNas) {
        try {
            semaphore.acquire();
            Path pathfile = Paths.get(defaultPath);
            Files.createDirectories(pathfile.getParent());
            codificador codex = codificador.getInstande();
            try (BufferedWriter writer = Files.newBufferedWriter(pathfile, Charset.forName("UTF-8"))) {
                writer.write(codex.encrypt("USER==" + user) + "\n");
                writer.write(codex.encrypt("PASS1==" + pass1) + "\n");
                writer.write(codex.encrypt("PASS2==" + pass2) + "\n");
                writer.write(codex.encrypt("PATH==" + path) + "\n");
                writer.write(codex.encrypt("USENAS==" + useNas) + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            //Se supone que al ejecutar la aplicacion ya deberias tener permisos de E/L 
            //  capturamos la excepcion por si acaso
//            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
        }
    }

    /**
     * @return Lista del contenido linea a linea del fichero almacenador
     * ,desencriptado. Null en caso de error
     */
    private static List<String> readFile() {
        LogRecord logRegistro = null;
        List<String> respuesta = new ArrayList<>();
        List<String> list = new ArrayList<>();
        codificador codex = codificador.getInstande();
        
        try (BufferedReader br = Files.newBufferedReader(Paths.get(defaultPath))) {
            list = br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, errors.toString());
            logRegistro.setSourceMethodName("readFile");
            logRegistro.setSourceClassName(UserInfo.class.getName());
        }finally{
            if (logRegistro != null) {
                LogGeneral.log(logRegistro);
            }
        }

        for (String element : list) {
            respuesta.add(codex.decrypt(element.toString()));
        }
        return respuesta;
    }

    /**
     * @param data Lista de lineas que se escribiran en el fichero almacenado
     */
    private static void rewriteFile(List<String> data) {
        LogRecord logRegistro = null;
        Path pathfile = Paths.get(defaultPath);
        codificador codex = codificador.getInstande();

        try (BufferedWriter writer = Files.newBufferedWriter(pathfile, Charset.forName("UTF-8"))) {
            for (String line : data) {
                writer.write(codex.encrypt(line) + "\n");
            }
        } catch (IOException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, errors.toString());
            logRegistro.setSourceMethodName("rewriteFile");
            logRegistro.setSourceClassName(UserInfo.class.getName());
        }finally{
            if (logRegistro != null) {
                LogGeneral.log(logRegistro);
            }
        }
    }

    /**
     *
     * @return identificador del usuario almacenado.
     * 
     * @throws NoSuchFieldException Si en el fichero no esta almacenado el campo deseado
     */
    public static String getUser() throws NoSuchFieldException {
        String respuesta = null;
        try {
            semaphore.acquire();
            List<String> aux = readFile();
            for (String line : aux) {
                if (line != null && line.contains("USER==")) {
                    respuesta = line.replaceAll("USER==", "");
                    break;
                }
            }
            if(respuesta == null){
                throw new NoSuchFieldException();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
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
                semaphore.acquire();
                List<String> aux = readFile();
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
                rewriteFile(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaphore.release();
            }
        }
    }

    /**
     *
     * @return passwd para Moodle almacenado.
     * 
     * @throws NoSuchFieldException Si en el fichero no esta almacenado el campo deseado
     */
    public static String getPass1() throws NoSuchFieldException {
        String respuesta = null;
        try {
            semaphore.acquire();
            List<String> aux = readFile();
            for (String line : aux) {
                if (line.contains("PASS1==")) {
                    respuesta = line.replaceAll("PASS1==", "");
                    break;
                }
            }
            if(respuesta == null){
                throw new NoSuchFieldException();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
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
                semaphore.acquire();
                List<String> aux = readFile();
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
                rewriteFile(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaphore.release();
            }
        }
    }

    /**
     *
     * @return passwd para NAS-TER almacenado.
     * 
     * @throws NoSuchFieldException Si en el fichero no esta almacenado el campo deseado
     */
    public static String getPass2() throws NoSuchFieldException {
        String respuesta = null;
        try {
            semaphore.acquire();
            List<String> aux = readFile();
            for (String line : aux) {
                if (line.contains("PASS2==")) {
                    respuesta = line.replaceAll("PASS2==", "");
                    break;
                }
            }
            if(respuesta == null){
                throw new NoSuchFieldException();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
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
                semaphore.acquire();
                List<String> aux = readFile();
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
                rewriteFile(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaphore.release();
            }
        }
    }

    /**
     *
     * @return path de descarga almacenado en el fichero.
     * 
     * @throws NoSuchFieldException Si en el fichero no esta almacenado el campo deseado
     */
    public static String getPath() throws NoSuchFieldException{
        String respuesta = null;
        try {
            semaphore.acquire();
            List<String> aux = readFile();
            for (String line : aux) {
                if (line.contains("PATH==")) {
                    respuesta = line.replaceAll("PATH==", "");
                    break;
                }
            }
            if(respuesta == null){
                throw new NoSuchFieldException();
            }
        } catch (InterruptedException ex) {
//            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
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
                semaphore.acquire();
                List<String> aux = readFile();
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
                rewriteFile(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaphore.release();
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
            semaphore.acquire();
            List<String> aux = readFile();
            for (String line : aux) {
                if (line.contains("USENAS==")) {
                    respuesta = Boolean.parseBoolean(line.replaceAll("USENAS==", ""));
                    break;
                }
            }
        } catch (Exception ex) {
//            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
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
                semaphore.acquire();
                List<String> aux = readFile();
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
                rewriteFile(aux);
            } catch (InterruptedException ex) {
//                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaphore.release();
            }
        }
    }

    /**
     * @deprecated Print del contenido del fichero, aun codificado
     */
    public static void printFile() {
        List aux = readFile();

        aux.forEach(System.out::println);
    }

    /**
     * @return TRUE si el archivo existe y almenos uno de sus campos no es ""
     */
    public static boolean dataExits() {
        boolean respuesta = false;
        List<String> aux = Arrays.asList("", "", "", "", "");
        List<String> aux2;
        if (Files.exists(Paths.get(defaultPath))) {
            aux2 = getDatas();
            if (aux2 != null) {
                respuesta = !aux2.equals(aux);
            }
        }
        return respuesta;
    }

    /**
     * @return List de los "atributos" listo para usar.
     */
    private static List<String> getDatas() {
        List<String> respuesta = Arrays.asList(new String[5]);//new ArrayList<>();
        List<String> list;
        String aux;
        try {
            semaphore.acquire();
            list = readFile();
            for (String data : list) {
                if(data == null){
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
//            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            respuesta = null;
            LogGeneral.log(new LogRecord(Level.SEVERE, HelloWorld
                    .getResource().getString(ResourceLeng.TRACE_DATES_ERROR)));
        } finally {
            semaphore.release();
            return respuesta;
        }
    }

    public static void deleteFile(){
        try {
            Path pathfile = Paths.get(defaultPath);
            Files.deleteIfExists(pathfile);
        } catch (IOException ex) {
//            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
