package tools.almacen;

//#1 Static import
import aplicacion.MainClass;
import aplicacion.datosListas.Tarea;
import tools.logger.LogGeneral;
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
import tools.lenguaje.ResourceLeng;

/**
 * 200
 * @author Diego Alvarez
 */
public class AlmacenTareas {

    /**
     * Metodo que almacenara una Colection Map de Tarea en un fichero que
 encriptaremos en base de una clave.
     *
     * @param datos Map de Tarea que queremos almacenar/persistir
     * @param clave clave con la que se almacenara los datos
     */
    public static void guardarDatos(Map<String, Tarea> datos, String clave) {
        Path ficheroTemporal = Paths.get("./Dates/tempDeliverys.txt", new String[0]);
        Path ficheroFinal = Paths.get("./Dates/allDeliverys.txt", new String[0]);
        LogRecord logRegistro = null;

        try (FileOutputStream fos = new FileOutputStream(ficheroTemporal.toString());
                ObjectOutputStream oos = new ObjectOutputStream(fos);) {

            oos.writeObject(datos);
            criptografiar(1, "Wendy is: " + clave, ficheroTemporal.toFile(), ficheroFinal.toFile());

        } catch (IOException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            logRegistro = new LogRecord(Level.WARNING, MainClass.getResource().getString(ResourceLeng.TRACE_STORE_SAVE) + "\n" + errors.toString());
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
     * Metodo para recuperar datos almacenados
     *
     * @param clave clave con la que se recuperaran los datos
     *
     * @return devuelve Map con los datos guardados. Null en caso de que el
     * fichero que deberia tener los datos no exista o la clave sea incorrecta o
     * no sea longitud 6
     */
    public static HashMap<String, Tarea> cargarDatos(String clave) {
        Path ficheroTemporal = Paths.get("./Dates/tempDeliverys.txt", new String[0]);
        Path ficheroFinal = Paths.get("./Dates/allDeliverys.txt", new String[0]);

        if ((!ficheroFinal.toFile().exists()) && (clave.length() == 6)) {
            // Meter loggers
            return null;
        } else {
            return cargarDatos(clave, ficheroFinal, ficheroTemporal);
        }
    }

    /**
     * Metodo para recuperar datos almacenados
     *
     * @param clave clave con la que se recuperaran los datos
     * @param ficheroFinal fichero que contiene los datos encriptados
     * @param ficheroTemporal fichero que contiene los datos desencriptados
     *
     * @return devuelve Map con los datos guardados. Null en caso de que el que
     * almacena los datos haya sido modificado o ocurra error al recuperar los
     * datos.
     */
    private static HashMap<String, Tarea> cargarDatos(String clave, Path ficheroFinal, Path ficheroTemporal) {
        HashMap<String, Tarea> respuesta = new HashMap();
        LogRecord logRegistro = null;
        StringWriter errors = null;
                
        criptografiar(2, "Wendy is: " + clave, ficheroFinal.toFile(), ficheroTemporal.toFile());
        try (FileInputStream fis = new FileInputStream(ficheroTemporal.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis);){
            
            respuesta = (HashMap) ois.readObject();   
        } catch (FileNotFoundException ex1) {
            // EL fichero Temp del que se recuperan datos no esta
            errors = new StringWriter();
            ex1.printStackTrace(new PrintWriter(errors));

            logRegistro = new LogRecord(Level.WARNING, MainClass.getResource()
                    .getString(ResourceLeng.TRACE_STORE_LOAD_FILE) + "\n" + errors.toString());
            
        } catch (IOException | ClassNotFoundException ex2) {
            // Error al leer los datos
            errors = new StringWriter();
            ex2.printStackTrace(new PrintWriter(errors));

            logRegistro = new LogRecord(Level.SEVERE, MainClass.getResource()
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
    private static void criptografiar(int modo, String clave, File ficheroEntrada, File ficheroSalida) {
        String modoText = modo == 1 ? "ENCRYPT" : "DECRYPT";
        LogRecord logRegistro = null;
        StringWriter errors = null;
        
        try (FileInputStream inputStream = new FileInputStream(ficheroEntrada);
                FileOutputStream outputStream = new FileOutputStream(ficheroSalida);){
            // Init Cipher
            Key secretKey = new SecretKeySpec(clave.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(modo, secretKey); //1

            byte[] inputBytes = new byte[(int) ficheroEntrada.length()];
            inputStream.read(inputBytes);// 4

            byte[] outputBytes = cipher.doFinal(inputBytes);//2/3
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
