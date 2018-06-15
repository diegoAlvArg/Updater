package Sincronizacion.Moodle.inicio;

//#1 Static import
import Sincronizacion.Moodle.estructura.Nodo;
//#4 Java
import java.util.concurrent.Callable;

/**
 *
 * @author Diego Alvarez
 
 Task para lanzar un Nodo "raiz". Metido en un callable porque queremos 
  gestionarlo con un executor.
 */
public class TareaWrapper implements Callable<Void> {

    private final Nodo curso;
    private final int id;

    public TareaWrapper(Nodo _node, int _id) {
        this.curso = _node;
        this.id = _id;
    }

    @Override
    public Void call() throws Exception {
        curso.descender();

        return null;
    }

    /**
     * @deprecated 
     */
    public void listar() {
        curso.listar(0);
    }
}
