import java.util.HashSet;
import java.util.Set;

public class Community {

    private final int id;
    private final Set<Integer> keys;

    public Community(int id) {
        this.id = id;
        this.keys = new HashSet<>();
    }

    public Set<Integer> getKeys() {
        return keys;
    }

    public int size() {
        return keys.size();
    }

    @Override
    public String toString() {
        return "Community{" +
                "id=" + id +
                ", keys=" + keys +
                '}';
    }
}
