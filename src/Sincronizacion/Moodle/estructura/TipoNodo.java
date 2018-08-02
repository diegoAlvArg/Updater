package Sincronizacion.Moodle.estructura;

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
    PAGE("Página"),
    ARCHIVO("Archivo"),
    URL("URL"),
    TAREA("Tarea"),
    OTHER("Other"),
    FORO("Foro");

    private String value;

    TipoNodo(String _value) {
        this.value = _value;
    }

    private String getValor() {
        return value;
    }

    @Override
    public String toString() {
        if (this.equals(SECTIONCOLAP) || this.equals(SECTIONEXPAND)) {
            return "Section";
        } else {
            return this.getValor();
        }

    }

    public static TipoNodo getEnum(String _value) {
        for (TipoNodo b : TipoNodo.values()) {
            if (b.value.equalsIgnoreCase(_value)) {
                return b;
            }
        }
        return TipoNodo.OTHER;
    }
}
