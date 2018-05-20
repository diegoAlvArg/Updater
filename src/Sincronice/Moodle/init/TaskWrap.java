package Sincronice.Moodle.init;

import Sincronice.moodle.tree.Node;
import java.util.concurrent.Callable;

/**
 *
 * @author Diego Alvarez
 * 
 * Task para lanzar un Node "raiz". Metido en un callable porque queremos 
 *  gestionarlo con un executor.
 */
public class TaskWrap implements Callable<Void> {

    private final Node curso;
    private final int id;

    public TaskWrap(Node _node, int _id) {
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
