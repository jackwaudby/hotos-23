import com.google.common.collect.ImmutableMap;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class BoltOltpDriver implements OltpDriver, AutoCloseable {
    private final Driver driver;
    private final Random rand;
    private final List<Long> personIds;
    private final Metrics metrics;
    private final BlockingQueue<Integer> shutdown;

    public BoltOltpDriver(String uri, List<Long> personIds, Metrics metrics, BlockingQueue<Integer> shutdown) {
        this.driver = GraphDatabase.driver(uri);
        this.rand = new Random();
        this.personIds = personIds;
        this.metrics = metrics;
        this.shutdown = shutdown;
    }

    @Override
    public void run(int transactions) throws InterruptedException {
        System.out.println("Start oltp driver");
        try (var session = driver.session()) {
            System.out.println("Start oltp transactions");
            for (int i = 0; i < transactions; i++) {
                var n = rand.nextDouble();
                if (n < 0.5) {
                    readTransaction(session);
                } else {
                    readWriteTransaction(session);
                }
                incCommitted();
            }
            System.out.println("Stop oltp transactions");
        }

        System.out.println("Shutdown oltp driver");
        shutdown.put(0);
    }


    private void incCommitted() {
        this.metrics.increment();
    }

    @Override
    public void readTransaction(Session session) {
        var personId = getPersonId();

        var txn = session.beginTransaction();
        var res = txn.run("""
                MATCH (n:Person {id: $personId })-[:IS_LOCATED_IN]->(p:City)
                RETURN
                    n.firstName AS firstName,
                    n.lastName AS lastName,
                    n.birthday AS birthday,
                    n.locationIP AS locationIP,
                    n.browserUsed AS browserUsed,
                    p.id AS cityId,
                    n.gender AS gender,
                    n.creationDate AS creationDate""", ImmutableMap.of("personId", personId));
        txn.commit();
        txn.close();
    }

    @Override
    public void readWriteTransaction(Session session) {
        var personId = getPersonId();
        var txn = session.beginTransaction();
        txn.run("""
                        MATCH (p:Person {id: $personId})
                        SET p.lastSeen = $lastSeen""",
                ImmutableMap.of("personId", personId, "lastSeen", 19832474023L));
        txn.commit();
        txn.close();
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }

    private long getPersonId() {
        return personIds.get(rand.nextInt(personIds.size()));
    }
}
