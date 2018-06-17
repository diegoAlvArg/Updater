package Tools.almacen;

//#1 Static import
import aplicacion.datos.Tareas;
import Tools.lenguaje.ResourceLeng;
import Tools.logger.LogGeneral;
import aplicacion.HelloWorld;
//#4 Java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.Map;
//#5 Javax
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Diego
 */
public class AlmacenTareas2 {

    /**
     * Metodo que almacenara una Colection Map<> de Tareas en un fichero que
 encriptaremos en base de una clave.
     *
     * @param store Map<> de Tareas que queremos almacenar/persistir
     * @param key clave con la que se almacenara los datos
     */
    public static void guardarDatos(Map<String, Tareas> store, String key) {
//        Map<String, Tareas> tareasTrack = new HashMap<>();
        Path ficheroTemporal = Paths.get("./Dates/tempDeliverys.txt");
        Path ficheroFinal = Paths.get("./Dates/allDeliverys.txt");
        LogRecord logRegistro = null;

        try (FileOutputStream fos = new FileOutputStream(ficheroTemporal.toString());
                ObjectOutputStream oos = new ObjectOutputStream(fos);) {

            oos.writeObject(store);

//            System.out.printf("Serialized HashMap data is saved");
            encriptar("Wendy is: " + key, ficheroTemporal.toFile(), ficheroFinal.toFile());
        } catch (IOException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, HelloWorld.getResource()
                    .getString(ResourceLeng.TRACE_STORE_SAVE) + "\n" + errors.toString());
        } finally {
            if (logRegistro != null) {
                logRegistro.setSourceClassName("guardarDatos");
                LogGeneral.log(logRegistro);
            }
            if (ficheroTemporal.toFile().exists()) {
                ficheroTemporal.toFile().delete();
            }
        }
    }

    /**
     * @deprecated @param store
     * @param key
     * @param fileSource
     * @param fileTemp
     */
    private static void saveData(Map<String, Tareas> store, String key, Path fileSource, Path fileTemp) {
//        Map<String, Tareas> tareasTrack = new HashMap<>();
//        Tareas del;
//        Path fileTemp = Paths.get("./Log/temallDeliverys.txt");
//        Path fileF = Paths.get("./Log/allDeliverys.txt");

        try (FileOutputStream fos = new FileOutputStream(fileTemp.toString());
                ObjectOutputStream oos = new ObjectOutputStream(fos);) {

            oos.writeObject(store);

            System.out.printf("Serialized HashMap data is saved");
            encriptar("Wendy is: " + key, fileTemp.toFile(), fileSource.toFile());
//            pathfile.toFile().delete();
        } catch (IOException ex) {
            ex.printStackTrace();
//            Logger.getLogger(StringSearch2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fileTemp.toFile().exists()) {
//                fileTemp.toFile().delete();
            }
        }
    }

    /**
     * Metodo para recuperar datos almacenados
     *
     * @param key clave con la que se recuperaran los datos
     *
     * @return devuelve Map con los datos guardados. Null en caso de que el
     * fichero que deberia tener los datos no exista o la clave sea incorrecta o
     * no sea longitud 6
     */
    public static HashMap<String, Tareas> cargarDatos(String key) {
        Path ficheroTemporal = Paths.get("./Dates/tempDeliverys.txt");
        Path ficheroFinal = Paths.get("./Dates/allDeliverys.txt");

        if (!ficheroFinal.toFile().exists() && key.length() == 6) {
            // Meter loggers
            return null;
        } else {
            return cargarDatos(key, ficheroFinal, ficheroTemporal);
        }
    }

    /**
     * Metodo para recuperar datos almacenados
     *
     * @param key clave con la que se recuperaran los datos
     * @param ficheroFinal fichero que contiene los datos encriptados
     * @param ficheroTemporal fichero que contiene los datos desencriptados
     *
     * @return devuelve Map con los datos guardados. Null en caso de que el que
     * almacena los datos haya sido modificado o ocurra error al recuperar los
     * datos.
     */
    private static HashMap<String, Tareas> cargarDatos(String key, Path ficheroFinal, Path ficheroTemporal) {
        HashMap<String, Tareas> respuesta = new HashMap<>();
        LogRecord logRegistro = null;
        StringWriter errors = null;
        desencriptar("Wendy is: " + key, ficheroFinal.toFile(), ficheroTemporal.toFile());

        try (FileInputStream fis = new FileInputStream(ficheroTemporal.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis);) {

            respuesta = (HashMap) ois.readObject();

//            System.out.println("Deserialized HashMap..");
        } catch (FileNotFoundException ex1) {
            // EL fichero Temp del que se recuperan datos no esta
            errors = new StringWriter();
            ex1.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, HelloWorld.getResource()
                    .getString(ResourceLeng.TRACE_STORE_LOAD_FILE) + "\n" + errors.toString());
        } catch (IOException | ClassNotFoundException ex2) {
            // Error al leer los datos
            errors = new StringWriter();
            ex2.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, HelloWorld.getResource()
                    .getString(ResourceLeng.TRACE_STORE_LOAD_DATA) + "\n" + errors.toString());
        } finally {
            if (logRegistro != null) {
                logRegistro.setSourceClassName("cargarDatos");
                LogGeneral.log(logRegistro);
            }
            if (ficheroTemporal.toFile().exists()) {
                ficheroTemporal.toFile().delete();
            }
            return respuesta;
        }
    }

    /**
     * Metodo que encripta en base a una key el ficheroEntrada y da como
     * resultado el ficheroSalida
     *
     * @param key clave con la que se encripta los datos
     * @param ficheroEntrada fichero que contiene los datos originales
     * @param ficheroSalida fichero que contendra los datos encriptados
     */
    private static void encriptar(String key, File ficheroEntrada, File ficheroSalida) {
        criptografiar(Cipher.ENCRYPT_MODE, key, ficheroEntrada, ficheroSalida);
    }

    /**
     * Metodo que desencripta un ficheroEntrada en base a una key y da como
     * resultado el ficheroSalida
     *
     * @param key clave con la que se desencripta los datos
     * @param ficehroEntrada fichero que contiene los datos encriptados
     * @param ficheroSalida fichero que contendra los datos desencriptados
     */
    private static void desencriptar(String key, File ficehroEntrada, File ficheroSalida) {
        criptografiar(Cipher.DECRYPT_MODE, key, ficehroEntrada, ficheroSalida);
    }

    /**
     *
     * @param modo
     * @param key
     * @param ficheroEntrada
     * @param ficheroSalida
     *
     * @see https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html
     * @see
     * https://docs.oracle.com/javase/8/docs/api/java/io/FileInputStream.html
     * @see
     * https://docs.oracle.com/javase/8/docs/api/java/io/FileOutputStream.html
     *
     */
    private static void criptografiar(int modo, String key, File ficheroEntrada, File ficheroSalida) {
        String modoText = modo == 1 ? "ENCRYPT" : "DECRYPT";
        LogRecord logRegistro = null;
        StringWriter errors = null;

        try (FileInputStream inputStream = new FileInputStream(ficheroEntrada);
                FileOutputStream outputStream = new FileOutputStream(ficheroSalida);) {
            // Init Cipher
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(modo, secretKey); //1

            byte[] inputBytes = new byte[(int) ficheroEntrada.length()];
            inputStream.read(inputBytes); //4

            byte[] outputBytes = cipher.doFinal(inputBytes); //2/3

            outputStream.write(outputBytes);// 4
        } catch (NoSuchPaddingException | NoSuchAlgorithmException ex1) {
            // No deberia, no depende del usuario
            errors = new StringWriter();
            ex1.printStackTrace(new PrintWriter(errors));
        } catch (InvalidKeyException ex2) {
            // Error al inicializar, no deberia ya se trato
            errors = new StringWriter();
            ex2.printStackTrace(new PrintWriter(errors));
        } catch (IOException ex3) {
            // Ocurre un error al leer/escribir bytes en los ficheros
            errors = new StringWriter();
            ex3.printStackTrace(new PrintWriter(errors));
        } catch (BadPaddingException | IllegalBlockSizeException ex4) {
            // BadPaddingException al desencriptar es contraseña incorrecta
            // IllegalBlockSizeException al encriptar ocurre un problema
            //  relacionado con el tamaño de bloque       
            errors = new StringWriter();
            ex4.printStackTrace(new PrintWriter(errors));
        } finally {
            if (errors != null) {
                logRegistro = new LogRecord(Level.SEVERE, modoText + "\n" + errors.toString());
                logRegistro.setSourceClassName("criptografiar");
                LogGeneral.log(logRegistro);
            }
        }
    }
}