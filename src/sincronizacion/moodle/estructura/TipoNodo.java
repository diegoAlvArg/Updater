package sincronizacion.moodle.estructura;

/**
 * @author Diego Alvarez
 * 
 * Tipificacion de la clase Node. Dicha clase se valdra de esta para el control 
 *  en parte de su logica
 */
public enum TipoNodo {
    CURSO("Curso"),
    SECTIONCOLAP("SectionCOL"),
    SECTIONEXPAND("SectionEXP"),
    FOLDER("Carpeta"), 
    PAGE("P�gina"),
    ARCHIVO("Archivo"),
    URL("URL"),
    TAREA("Tarea"),
    OTHER("Other"),
    FORO("Foro");

    private String value;

    private TipoNodo(String value) {
        this.value = value;
    }

    private String getValor() {
        return this.value;
    }

    public String toString() {
        if ((equals(SECTIONCOLAP)) || (equals(SECTIONEXPAND))) {
            return "Section";
        }
        return getValor();
    }

    public static TipoNodo getEnum(String value) {
        for (TipoNodo b : TipoNodo.values()) {
            if (b.value.equalsIgnoreCase(value)) {
                return b;
            }
        }
        return OTHER;
    }
}
