import org.neo4j.driver.Session;

public interface Client {

    void run(int transactions);
    void shortReadTransaction(Session session);

    void shortReadWriteTransaction(Session session);

    void bulkUpdate(Session session);
}
