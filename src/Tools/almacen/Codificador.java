package Tools.almacen;

//#4 Java
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 130
 *
 * @author Diego
 *
 * NOTA: Revisar catch de generarKey()
 */
public class Codificador {

    private static Codificador instance = null;
    private SecretKeySpec secretKey;

    protected Codificador() {
        setKey(generarKey());
    }

    public synchronized static Codificador getInstande() {
        if (instance == null) {
            instance = new Codificador();
        }
        return instance;
    }

    /**
     * Generador de la key para encriptar, en base a la MAC del computador
     *
     * @return
     */
    private String generarKey() {
        String respuesta = "WendolynVon";
        InetAddress ip;
        try {

            ip = InetAddress.getLocalHost();
//            System.out.println("Current IP address : " + ip.getHostAddress());

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();

//            System.out.print("Current MAC address : ");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            respuesta = sb.toString();

        } catch (UnknownHostException e) {
            // Trabajando en local saltara esto??--------------------------------------------------------------------------------
            e.printStackTrace();
        } catch (SocketException e) {
            // Trabajando en local saltara esto??--------------------------------------------------------------------------------
            e.printStackTrace();
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
