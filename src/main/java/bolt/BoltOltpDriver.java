package bolt;

import utils.Metrics;

import com.google.common.collect.ImmutableMap;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class BoltOltpDriver implements utils.Driver, AutoCloseable {
    private final Driver driver;
    private final Random rand;
    private final List<Long> personIds;
    private final Metrics metrics;
    private final BlockingQueue<Integer> in;
    private final BlockingQueue<Integer> out;

    private final boolean rw;


    public BoltOltpDriver(String uri, Metrics metrics, BlockingQueue<Integer> in, BlockingQueue<Integer> out, List<Long> personIds, boolean rw) {
        this.driver = GraphDatabase.driver(uri);
        this.rand = new Random();
        this.personIds = personIds;
        this.metrics = metrics;
        this.out = out;
        this.in = in;
        this.rw = rw;
    }

    @Override
    public void run() throws InterruptedException {
        try (var session = driver.session()) {
            while (in.size() == 0) {
                if (rw) {
                    readWriteTransaction(session);
                } else {
                    readTransaction(session);
                }
                incCommitted();
            }
        }
        out.put(0);
    }

    public void readTransaction(Session session) {

        var personId = getPersonId();

        var txn = session.beginTransaction();
        txn.run("""
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

    private void incCommitted() {
        this.metrics.increment();
    }

    private long getPersonId() {
        return personIds.get(rand.nextInt(personIds.size()));
    }
}
