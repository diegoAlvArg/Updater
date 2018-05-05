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
     */
    public static void createFile(String user, String pass1, String pass2, String path) {
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
            if (aux != null && aux.size() == 4) {
                respuesta = aux.get(0).replaceAll("USER==", "");
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
        if (newData != null && !newData.equals("")) {
            try {
                semaphore.acquire();
                List<String> aux = readFile();
                if (aux != null) {
                    aux.set(0, "USER==" + newData);
                    rewriteFile(aux);
                }
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
            if (aux != null && aux.size() == 4) {
                respuesta = aux.get(1).replaceAll("PASS1==", "");
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
        if (newData != null && !newData.equals("")) {
            try {
                semaphore.acquire();
                List<String> aux = readFile();
                if (aux != null) {
                    aux.set(1, "PASS1==" + newData);
                    rewriteFile(aux);
                }
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
            if (aux != null && aux.size() == 4) {
                respuesta = aux.get(2).replaceAll("PASS2==", "");
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
        if (newData != null && !newData.equals("")) {
            try {
                semaphore.acquire();
                List<String> aux = readFile();
                if (aux != null) {
                    aux.set(2, "PASS2==" + newData);
                    rewriteFile(aux);
                }
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
            if (aux != null && aux.size() == 4) {
                respuesta = aux.get(3).replaceAll("PATH==", "");
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
        if (newData != null && !newData.equals("")) {
            try {
                semaphore.acquire();
                List<String> aux = readFile();
                if (aux != null) {
                    aux.set(3, "PATH==" + newData);
                    rewriteFile(aux);
                }
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
    public static boolean dataExits(){
        boolean respuesta = false;
        List<String> aux = Arrays.asList("", "", "", "");
        if(Files.exists(Paths.get(defaultPath))){
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
