import java.io.Serializable;

public class Id implements Serializable {
    private final int id;

    public Id(int id) {
        this.id = id;
    }

    public String toString() {
        return "" + id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Id && this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.id;
        return hash;
    }
}
