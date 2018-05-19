package Tools.userDates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
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
            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
        }
    }

    /**
     * @return Lista del contenido linea a linea del fichero almacenador
     * ,desencriptado
     */
    private static List<String> readFile() {
        List<String> respuesta = new ArrayList<>();
        List<String> list = new ArrayList<>();
        codificador codex = codificador.getInstande();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(defaultPath))) {
            list = br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String element : list) {
            respuesta.add(codex.decrypt(element.toString()));
        }
        return respuesta;
    }

    /**
     *
     * @param data Lista de lineas que se escribiran en el fichero almacenado
     */
    private static void rewriteFile(List<String> data) {
        Path pathfile = Paths.get(defaultPath);
        codificador codex = codificador.getInstande();

        try (BufferedWriter writer = Files.newBufferedWriter(pathfile, Charset.forName("UTF-8"))) {
            for (String line : data) {
                writer.write(codex.encrypt(line) + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * PRE: el usuario no ha modificado el archivo (contenido/orden)
     *
     * @return identificador del usuario almacenado.
     */
    public static String getUser() {
        String respuesta = null;
        try {
            semaphore.acquire();
            List<String> aux = readFile();
            for (String line : aux) {
                if (line.contains("USER==")) {
                    respuesta = line.replaceAll("USER==", "");
                    break;
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
            return respuesta;
        }
    }

    /**
     * PRE: el usuario no ha modificado el archivo (contenido/orden)
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
                if(isnew){
                    aux.add("USER==" + newData);
                }
                rewriteFile(aux);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaphore.release();
            }
        }
    }

    /**
     * PRE: el usuario no ha modificado el archivo (contenido/orden)
     *
     * @return passwd para Moodle almacenado.
     */
    public static String getPass1() {
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
        } catch (InterruptedException ex) {
            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
            return respuesta;
        }
    }

    /**
     * PRE: el usuario no ha modificado el archivo (contenido/orden)
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
                if(isnew){
                    aux.add("PASS1==" + newData);
                }
                rewriteFile(aux);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                semaphore.release();
            }
        }
    }

    /**
     * PRE: el usuario no ha modificado el archivo (contenido/orden)
     *
     * @return passwd para NAS-TER almacenado.
     */
    public static String getPass2() {
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
        } catch (InterruptedException ex) {
            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
            return respuesta;
        }
    }

    /**
     * PRE: el usuario no ha modificado el archivo (contenido/orden)
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
                if(isnew){
                    aux.add("PASS2==" + newData);
                }
                rewriteFile(aux);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
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
    public static String getPath() {
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
        } catch (InterruptedException ex) {
            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
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
                if(isnew){
                    aux.add("PATH==" + newData);
                }
                rewriteFile(aux);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
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
                if(isnew){
                    aux.add("USENAS==" + newData);
                }
                rewriteFile(aux);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        if (Files.exists(Paths.get(defaultPath))) {
            respuesta = !getDatas().equals(aux);
        }
        return respuesta;
    }

    /**
     * @return List de los "atributos" listo para usar.
     */
    private static List<String> getDatas() {
        List<String> respuesta = new ArrayList<>();
        List<String> list;
        String aux;
        try {
            semaphore.acquire();
            list = readFile();
            for (String data : list) {
                aux = data.substring(data.indexOf("==") + 2);
                respuesta.add(aux);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            semaphore.release();
            return respuesta;

        }
    }
}
