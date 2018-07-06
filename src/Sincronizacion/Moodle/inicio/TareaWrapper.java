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

    public TareaWrapper(Nodo node, int id) {
        this.curso = node;
        this.id = id;
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
