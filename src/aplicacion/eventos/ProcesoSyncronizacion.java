package aplicacion.eventos;

//#1 Static import
//import aplicacion.controlador.InterfaceController;
import sincronizacion.moodle.inicio.OpcionesSyncMoodle;
import sincronizacion.naster.OpcionesSyncNaster;
import aplicacion.controlador.MainController;
//#4 Java
import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;
import javafx.application.Platform;

/**
 * 116
 * @author Diego
 *
 * Clase que gestiona el proceso de sincronizacion, crea y lanza un hilo que
 * ejecuta la logica de sincronizacion y al terminar contactara con el
 * controlador de la Iu y comunicara su finalizacion. No se realiza como un
 * Service porque es un porceso tan pesado que cuelga el hilo principal de la
 * Interfaz propio de JavaFx
 *
 * @version 1.0 El hilo creado lanzara una sincronizacion total contra Moodle.
 * @version 1.1 Se ha aniado la posibilidad de sincronizar con Nas-ter.
 *
 * 
 */
public class ProcesoSyncronizacion {

    /**
     * Contructor
     *
     * @param usuario identificador del usuario con el que logeamos
     * @param contraseniaM passwd del usuario con el que logeamos en moodle
     * @param contraseniaN passwd del usuario con el que logeamos en NASTER
     * @param pathDescarga path del local sobre el que realizaremos la
     * sincronizacion
     * @param rb resourceBundle con los mensajes susceptibles a cambio de idioma
     * @param iuControl clase que maneja el control de la IU
     * @param usarNas boolean, indicara si haremos uso de Nas-ter
     *
     *
     */
    public ProcesoSyncronizacion(String usuario, String contraseniaM, String contraseniaN, String pathDescarga, ResourceBundle rb, MainController iuControl, boolean usarNas) {
        lanzarProceso(usuario, contraseniaM, contraseniaN, pathDescarga, rb, iuControl, usarNas);
    }

    /**
     * Creacion del hilo, los parametros estan establecidos como final debido a
     * que asi estaban en los ejemplos y creo que es asi por el tema de lanzar
     * un hilo huerfano.
     *
     * @param user identificador del usuario con el que logeamos
     * @param contraseniaM passwd del usuario con el que logeamos en moodle
     * @param contraseniaN passwd del usuario con el que logeamos en NASTER
     * @param pathDescarga path del local sobre el que realizaremos la
     * sincronizacion
     * @param rb resourceBundle con los mensajes susceptibles a cambio de idioma
     * @param iuControl clase que maneja el control de la IU
     * @param usarNas boolean, indicara si haremos uso de Nas-ter
     */
    private void lanzarProceso(String user, String contraseniaM, String contraseniaN, String pathDescarga, ResourceBundle rb, MainController iuControl, boolean usarNas) {
        final String _user = user;
        final String _pass1 = contraseniaM;
        final String _pass2 = contraseniaN;
        final String _path = pathDescarga;
        final ResourceBundle _rb = rb;
        final MainController _iu = iuControl;
        final boolean _useNas = usarNas;
        System.err.println("flag 00");
        new Thread(() -> {
            System.err.println("flag 01");
            if (_user != null && _pass1 != null && _path != null && (contraseniaN != null || _useNas)) {
                System.err.println("flag 02A");
                try {
                    if (_useNas) {
                        OpcionesSyncNaster.sincronizar(user, contraseniaN, pathDescarga, getAnioActual());
                        // Creo que segun RQ hay que tratar de difernete forma
                    }
                    OpcionesSyncMoodle.realizarActualizacionTotal(_user, _pass1, getAnioActual(), _path, _iu);
//                OpcionesSyncMoodle.realizarActualizacionIndividual(6, _user, _pass1, "(2016-2017)", _path, _iu);
                } catch (IOException ex) {
//                    Moodle esta caido
                    Logger.getLogger(ProcesoSyncronizacion.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                        Platform.runLater(
                                () -> {
                                    System.err.println("acabando");
                                    _iu.finalizarSincronizacion();
                                }
                        );
                }
            } else {
                System.err.println("flag 02B");
                _iu.borrarUsuario();
            }
        }).start();
    }

    /**
     * Calculara el curso en el que esta actualmenteen un formato (yyyy-yyyy)
     *
     * @return
     */
    private String getAnioActual() {
        String respuesta = "";
        LocalDate today = LocalDate.now();
        LocalDate mark = LocalDate.of(today.getYear(), 9, 5); //Inicio de las matriculaciones 05/09/yyyy
        if (today.isAfter(mark)) {
            respuesta = "(" + today.getYear() + "-" + today.getYear() + 1 + ")";
        } else {
            respuesta = "(" + (today.getYear() - 1) + "-" + today.getYear() + ")";
        }
        return respuesta;
//                return "(2016-2017)";     //Test mode
    }
}
