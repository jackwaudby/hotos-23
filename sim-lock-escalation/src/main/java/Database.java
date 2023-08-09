import java.util.ArrayList;
import java.util.List;

public class Database {

    private final List<Key> database;

    public Database(int keys) {
        this.database = new ArrayList<>();
        for (int i = 0; i < keys; i++) {
            database.add(new Key(i));
        }
    }

    public List<Key> getKeys() {
        return database;
    }
}
