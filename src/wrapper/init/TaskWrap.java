package wrapper.init;

import wrapper.tree.Node;
import java.util.concurrent.Callable;

/**
 *
 * @author Diego Alvarez
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
     *
     */
    public void listar() {
        curso.listar(0);
    }
}
