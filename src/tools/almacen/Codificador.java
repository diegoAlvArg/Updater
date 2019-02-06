package tools.almacen;

//#4 Java
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
//#5 JavaFx
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 142 (++12)
 *
 * @author Diego Alvarez
 * @version 1.0
 *
 * @author Diego Alvarez
 * @version 1.1 Se ha cambiado la generacion de la Key en base a la respuesta de
 * Alexandre Fenyo
 * 
 * @see <a href="https://stackoverflow.com/questions/45270549/getting-mac-address-without-internet-connection-in-java">Link 01</a>
 */
public class Codificador {

    private static Codificador instance = null;
    private SecretKeySpec claveCodificacion;

    protected Codificador() {
        setKey(generarKey());
    }

    public static synchronized Codificador getInstance() {
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
     * @see
     * https://docs.oracle.com/javase/8/docs/api/java/net/NetworkInterface.html#getNetworkInterfaces--
     * @see
     * https://docs.oracle.com/javase/8/docs/api/java/net/NetworkInterface.html#getHardwareAddress--
     */
    private String generarKey() {
        String respuesta = "WendolynVon";
        NetworkInterface aux;
        Enumeration<NetworkInterface> e;
        StringBuilder sb = new StringBuilder();
        byte[] mac;
        try {
            e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                aux = (NetworkInterface) e.nextElement();
                mac = aux.getHardwareAddress();
                if ((mac != null) && (!aux.isVirtual()) && ((aux.getDisplayName().contains("Wi-Fi")) || (aux.getDisplayName().contains("Controller")))) {
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    sb.append(":");//En el caso de mas de 1
                }
            }
            respuesta = sb.toString();

            return respuesta;
        } catch (SocketException localSocketException) {
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
            this.claveCodificacion = new SecretKeySpec(keyByte, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
//            e.printStackTrace();
            //No deberia ocurrir nunca, meter log?

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
        String respuesta = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, this.claveCodificacion);
            respuesta = Base64.getEncoder().encodeToString(cipher.doFinal(cadena.getBytes("UTF-8")));
        } catch (Exception e) {
            respuesta = null;
        }finally{
            return respuesta;
        }
    }

    /**
     * Desencripta la cadena de entrada en base a una key
     *
     * @param cadena
     * @return String desencriptado, Null en caso de error
     */
    public synchronized String desencriptar(String cadena) {
        String respuesta = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(2, this.claveCodificacion);
            respuesta = new String(cipher.doFinal(Base64.getDecoder().decode(cadena)));
//            return new String(cipher.doFinal(Base64.getDecoder().decode(cadena)));
        } catch (Exception e) {
            respuesta = null;
        }finally{
            return respuesta;
        }
    }
}
