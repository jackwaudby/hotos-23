import java.util.Objects;

public class Key {
    private final int id;

    private boolean rangeLocked;

    private boolean communityLocked;
    public Key(int id) {
        this.id = id;
        this.rangeLocked = false;
        this.communityLocked = false;

    }

    public int getId() {
        return id;
    }

    public boolean isCommunityLocked() {
        return communityLocked;
    }

    public void setCommunityLocked() {
        this.communityLocked = true;
    }

    public void setRangeLocked() {
        this.rangeLocked = true;
    }

    public boolean isRangeLocked() {
        return rangeLocked;
    }

    @Override
    public String toString() {
        return "Key{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return id == key.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
