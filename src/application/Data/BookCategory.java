package application.Data;

/**
 *
 * @author o7planning
 * @version 1.0
 * @see https://o7planning.org/en/11147/javafx-treeview-tutorial
 *
 */
public class BookCategory {

    private String code;
    private String name;

    public BookCategory() {

    }

    public BookCategory(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String print() {
        return this.name + " :: " + this.code;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof BookCategory)) {
            return false;
        }
        BookCategory otherBook = (BookCategory) other;

        return print().equals(otherBook.print());
    }
}
