import org.neo4j.driver.Session;

public interface MammothDriver extends Driver {
    void mammothTransaction(Session session);
}
