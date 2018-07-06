package Tools.almacen;

//#4 Java
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 130
 *
 * @author Diego
 * @version 1.0
 * 
 * @author Diego
 * @version 1.1 Se ha cambiado la generacion de la Key en base a la respuesta de 
 * Alexandre Fenyo 
 * @see https://stackoverflow.com/questions/45270549/getting-mac-address-without-internet-connection-in-java
 * 
 * 
 * NOTA: Revisar catch de generarKey()
 */
public class Codificador {

    private static Codificador instance = null;
    private SecretKeySpec secretKey;

    protected Codificador() {
        setKey(generarKey());
    }

    public synchronized static Codificador getInstance() {
        if (instance == null) {
            instance = new Codificador();
        }
        return instance;
    }

    /**
     * Generador de la key para encriptar, en base a la MAC del computador
     *
     * @return
     * 
     * @see https://docs.oracle.com/javase/8/docs/api/java/net/NetworkInterface.html#getNetworkInterfaces--
     * @see https://docs.oracle.com/javase/8/docs/api/java/net/NetworkInterface.html#getHardwareAddress--
     */
    private String generarKey() {
        String respuesta = "WendolynVon";
        Enumeration<NetworkInterface> e;
        NetworkInterface aux;
        StringBuilder sb = new StringBuilder();
        try {
            e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                aux = e.nextElement();
                byte[] mac = aux.getHardwareAddress();
                if (mac != null && !aux.isVirtual()) {
                    if (aux.getDisplayName().contains("Wi-Fi") || aux.getDisplayName().contains("Controller")) {
                        for (int i = 0; i < mac.length; i++) {
                            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                        }
                        sb.append(":"); //En el caso de mas de 1
                    }
                }
            }
            respuesta = sb.toString();
        } catch (SocketException ex) {
            // La documentacion no es clara de cuando puede ocurrir este error
            
        } finally {
            return respuesta;
        }
    }

    /**
     * Establece la key a utilizar para encriptar/desencriptar
     *
     * @param myKey
     */
    private void setKey(String myKey) {
        MessageDigest sha = null;
        byte[] keyByte;
        try {
            keyByte = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            keyByte = sha.digest(keyByte);
            keyByte = Arrays.copyOf(keyByte, 16);
            secretKey = new SecretKeySpec(keyByte, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cifra la cadena de entrada en base a una key.
     *
     * @param cadena
     *
     * @return String cifrado, Null en caso de error
     */
    public synchronized String encriptar(String cadena) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(cadena.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    /**
     * Desencripta la cadena de entrada en base a una key
     *
     * @param cadena
     * @return String desencriptado, Null en caso de error
     */
    public synchronized String desencriptar(String cadena) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(cadena)));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}