package Tools.almacen;

//#1 Static import
import application.Data.Delivery;
//#4 Java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
//#5 JavaFx
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Diego
 */
public class almacenTareas {

    /**
     * Metodo que almacenara una Colection Map<> de Delivery en un fichero que
     * encriptaremos en base de una clave.
     *
     * @param store Map<> de Delivery que queremos almacenar/persistir
     * @param key clave con la que se almacenara los datos
     */
    public static void guardarDatos(Map<String, Delivery> store, String key) {
//        Map<String, Delivery> tareasTrack = new HashMap<>();
        Path ficheroTemporal = Paths.get("./Dates/tempDeliverys.txt");
        Path ficheroFinal = Paths.get("./Dates/allDeliverys.txt");

        try (FileOutputStream fos = new FileOutputStream(ficheroTemporal.toString());
                ObjectOutputStream oos = new ObjectOutputStream(fos);) {

            oos.writeObject(store);

            System.out.printf("Serialized HashMap data is saved");
            encrypt("Wendy is: " + key, ficheroTemporal.toFile(), ficheroFinal.toFile());
        } catch (IOException ex) {
            ex.printStackTrace();
//            Logger.getLogger(StringSearch2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (ficheroTemporal.toFile().exists()) {
                ficheroTemporal.toFile().delete();
            }
        }
    }

    /**
     * @deprecated 
     * @param store
     * @param key
     * @param fileSource
     * @param fileTemp
     */
    private static void saveData(Map<String, Delivery> store, String key, Path fileSource, Path fileTemp) {
//        Map<String, Delivery> tareasTrack = new HashMap<>();
//        Delivery del;
//        Path fileTemp = Paths.get("./Log/temallDeliverys.txt");
//        Path fileF = Paths.get("./Log/allDeliverys.txt");

        try (FileOutputStream fos = new FileOutputStream(fileTemp.toString());
                ObjectOutputStream oos = new ObjectOutputStream(fos);) {

            oos.writeObject(store);

            System.out.printf("Serialized HashMap data is saved");
            encrypt("Wendy is: " + key, fileTemp.toFile(), fileSource.toFile());
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

    
    public static HashMap<String, Delivery> loadData(String key) {
        Path pathfileTemp = Paths.get("./Dates/tempDeliverys.txt");
        Path pathfile = Paths.get("./Dates/allDeliverys.txt");

        if (!pathfile.toFile().exists()) {
            return null;
        } else {
            return loadData(key, pathfile, pathfileTemp);
        }
    }

    private static HashMap<String, Delivery> loadData(String key, Path fileSource, Path fileTemp) {
        HashMap<String, Delivery> respuesta = new HashMap<>();
        decrypt("Wendy is: " + key, fileSource.toFile(), fileTemp.toFile());

        try (FileInputStream fis = new FileInputStream(fileTemp.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis);) {

            respuesta = (HashMap) ois.readObject();
//            System.out.println("Deserialized HashMap..");

//            // Display content using Iterator
//            Set set = tareasTrack.entrySet();
//            Iterator iterator = set.iterator();
//            for (Map.Entry<String, Delivery> entry : tareasTrack.entrySet()) {
//                System.out.println(entry.getKey() + "/" + entry.getValue());
//            }
        } catch (IOException ioe) {
            System.err.println("Alguien toco los datos");
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
        } finally {
            if (fileTemp.toFile().exists()) {
                fileTemp.toFile().delete();
            }
            return respuesta;
        }
    }

    private static void encrypt(String key, File inputFile, File outputFile) {
        doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
    }

    private static void decrypt(String key, File inputFile, File outputFile) {
        doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
    }

    private static void doCrypto(int cipherMode, String key, File inputFile, File outputFile) {
        try (FileInputStream inputStream = new FileInputStream(inputFile);
                FileOutputStream outputStream = new FileOutputStream(outputFile);) {
            // Init Cipher
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            outputStream.write(outputBytes);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IOException ex) {
            ex.printStackTrace();
        }
//        catch (BadPaddingException ex) {
//            Logger.getLogger(StringSearch2.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

}
