package wrapper.tree;

/**
 * @author Diego Alvarez
 */
public enum TypeNode {
    CURSO("Curso"),
    SECTIONCOLAP("SectionCOL"),
    SECTIONEXPAND("SectionEXP"),
    FOLDER("Carpeta"),
    ARCHIVO("Archivo"),
    URL("URL"),
    TAREA("Tarea"),
    OTHER("Other"),
    FORO("Foro");

    private String value;

    TypeNode(String _value) {
        this.value = _value;
    }

    private String getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (this.equals(SECTIONCOLAP) || this.equals(SECTIONEXPAND)) {
            return "Section";
        } else {
            return this.getValue();
        }

    }

    public static TypeNode getEnum(String _value) {
        for (TypeNode b : TypeNode.values()) {
            if (b.value.equalsIgnoreCase(_value)) {
                return b;
            }
        }
        return TypeNode.OTHER;
    }
}
