import org.neo4j.driver.Session;

public interface OltpDriver extends Driver {
    void readTransaction(Session session);

    void readWriteTransaction(Session session);
}
