package application.events;

import application.InterfaceController;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.client.ClientProtocolException;
import wrapper.init.Opciones;
import wrapper.init.Opciones;

/**
 *
 * @author Usuario
 * 
 * Clase que gestiona el proceso de sincronizacion, crea y lanza un hilo que 
 *  ejecuta la logica de sincronizacion y al terminar contactara con el 
 *  controlador de la Iu y comunicara su finalizacion. No se realiza como un 
 *  Service porque es un porceso tan pesado que cuelga el hilo principal de 
 *  la Interfaz propio de JavaFx
 * 
 * @version 1.0
 * El hilo creado lanzara una sincronizacion total contra Moodle.
 * 
 * 
 * @see Doc Services Javafx
 */
public class procesoSyncronizacion {

//    private String user;
//    private String pass1;
//    private String pass2;
//    private String pathDowload;
//    private ResourceBundle rb;
//    private InterfaceController iu;
    
    /**
     * Contructor
     * 
     * @param user identificador del usuario con el que logeamos
     * @param pass1 passwd del usuario con el que logeamos en moodle
     * @param pass2 passwd del usuario con el que logeamos en NASTER
     * @param pathDowload  path del local sobre el que realizaremos la sincronizacion
     * @param rb resourceBundle con los mensajes susceptibles a cambio de idioma
     * @param iuControl clase que maneja el control de la IU
     * 
     * 
     */
    public procesoSyncronizacion(String user, String pass1, String pass2, String pathDowload, ResourceBundle rb, InterfaceController iuControl) {
//        this.user = user;
//        this.pass1 = pass1;
//        this.pass2 = pass2;
//        this.pathDowload = pathDowload;
//        this.rb = rb;
//        this.iu = iu;
        launchTread(user, pass1, pass2, pathDowload, rb, iuControl);
    }
    
    /**
     * Creacion del hilo, los parametros estan establecidos como final debido a 
     *  que asi estaban en los ejemplos y creo que es asi por el tema de lanzar 
     *  un hilo huerfano.
     * 
     * @param user identificador del usuario con el que logeamos
     * @param pass1 passwd del usuario con el que logeamos en moodle
     * @param pass2 passwd del usuario con el que logeamos en NASTER
     * @param pathDowload  path del local sobre el que realizaremos la sincronizacion
     * @param rb resourceBundle con los mensajes susceptibles a cambio de idioma
     * @param iuControl clase que maneja el control de la IU
     */
    private void launchTread(String user, String pass1, String pass2, String pathDowload, ResourceBundle rb, InterfaceController iuControl) {
        final String _user = user;
        final String _pass1 = pass1;
        final String _pass2 = pass2;
        final String _path = pathDowload;
        final ResourceBundle _rb = rb;
        final InterfaceController _iu = iuControl;
        new Thread(() -> {
            try {
                Opciones.realizarActualizacionTotal(_user, _pass1, currentYear(), _path, _iu);
//                Opciones.realizarActualizacionIndividual(0, _user, _pass1, "(2016-2017)", _path, _iu);
            } catch (IOException ex) {
                //Moodle esta caido
                Logger.getLogger(procesoSyncronizacion.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Platform.runLater(
                        () -> {
                            System.err.println("acabando");
                            _iu.syncroEnd();
                        }
                );
//                _iu.syncroEnd();
            }
        }).start();
    }
     
    /**
     * Calculara el curso en el que esta actualmenteen un formato (yyyy-yyyy)
     * 
     * @return
     */
    private String currentYear() {
        return "(2016-2017)";
    }
}