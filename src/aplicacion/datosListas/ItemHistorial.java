package aplicacion.datosListas;

/**
 * 59
 * @author Diego Alvarez
 * @version 1.0 
 */
public class ItemHistorial {

    private String pathFichero;
    private String nombre;

    
    public ItemHistorial(String pathFichero, String nombre) {
        this.pathFichero = pathFichero;
        this.nombre = nombre;
    }

    public String getPathFichero() {
        return pathFichero;
    }
    public void setPathFichero(String code) {
        this.pathFichero = code;
    }

    
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String name) {
        this.nombre = name;
    }

    
    public String print() {
        return this.nombre + " :: " + this.pathFichero;
    }

    @Override
    public String toString() {
        return this.nombre;//--------------------------
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof ItemHistorial)) {
            return false;
        }
        ItemHistorial otherBook = (ItemHistorial) other;

        return print().equals(otherBook.print());
    }
}
