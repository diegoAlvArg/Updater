package sincronizacion.moodle.inicio;

//#1 Static import
import sincronizacion.moodle.wrapper.Nodo;
//#4 Java
import java.util.concurrent.Callable;

/**
 * 32
 *
 * @author Diego Alvarez
 *
 * Task para lanzar un Nodo "raiz". Metido en un callable porque queremos
 * gestionarlo con un executor.
 */
public class TareaWrapper implements Callable<Void> {

    private Nodo curso;
    private int id;

    public TareaWrapper(Nodo node, int id) {
        this.curso = node;
        this.id = id;
    }

    public Void call() throws Exception {
        this.curso.descender();

        return null;
    }
}
