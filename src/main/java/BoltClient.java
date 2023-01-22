import com.google.common.collect.ImmutableMap;
import org.neo4j.driver.*;

import java.util.*;

public class BoltClient implements Client, AutoCloseable {
    private final Driver driver;

    private final Random rand;

    private final int persons;

    private int currentCommitted;

    private final List<Integer> committed;

    public BoltClient(String uri, String user, String password, int persons) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        this.rand = new Random();
        this.persons = persons;
        this.currentCommitted = 0;
        this.committed = new ArrayList<>();
    }

    public void nodeCount() {
        try (var session = driver.session()) {
            var response = session.executeRead(tx -> {
                var query = new Query("MATCH (n) RETURN count(n) as nodeCount");
                var result = tx.run(query);
                tx.run(query);
                return result.single().get(0);
            });
            System.out.println("nodeCount: " + response);
        }

    }

    public void edgeCount() {
        try (var session = driver.session()) {
            var response = session.executeRead(tx -> {
                var query = new Query("MATCH ()-[r]->() RETURN count(r) as edgeCount");
                var result = tx.run(query);


                return result.single().get(0);
            });
            System.out.println("edgeCount: " + response);
        }
    }


    @Override
    public void run(int transactions) {
        try (var session = driver.session()) {
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    collect();
                }
            }, 0, 1000);

            for (int i = 0; i < transactions; i++) {
                var n = rand.nextDouble();
                if (n < 0.49) {
                    shortReadTransaction(session);
                } else if (n < 0.98) {
                    shortReadWriteTransaction(session);
                } else {
                    bulkUpdate(session);
                }
                incCommitted();
            }

            t.cancel();
        }
    }

    private void incCommitted() {
        this.currentCommitted++;
    }

    @Override
    public void shortReadTransaction(Session session) {
        var personId = getPersonId();

        var txn = session.beginTransaction();
        txn.run("MATCH (p:Person {id: $personId}) RETURN p", ImmutableMap.of("personId", personId));
        txn.commit();
        txn.close();
    }

    @Override
    public void shortReadWriteTransaction(Session session) {
        var currTime = 19832474023L;
        var personId = getPersonId();

        var txn = session.beginTransaction();
        txn.run("""
                        MATCH (p:Person {id: $personId})
                        SET p.lastSeen = $lastSeen""",
                ImmutableMap.of("personId", personId, "lastSeen", currTime));
        txn.commit();
        txn.close();
    }

    @Override
    public void bulkUpdate(Session session) {
        var txn = session.beginTransaction();
        for (int personId = 1; personId <= persons; personId++) {
            txn.run("""
                            MATCH (p:Person {id: $personId})-[r:KNOWS]-()
                            WITH p, COUNT(r) AS friends
                            SET p.friends = friends""",
                    ImmutableMap.of("personId", personId));
        }
        txn.commit();
        txn.close();
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }

    private int getPersonId() {
        return rand.nextInt(persons) + 1;
    }

    private void collect() {
        System.out.println("Committed: " + currentCommitted);
        committed.add(currentCommitted);
        currentCommitted = 0;
    }

    public List<Integer> getCommitted() {
        return committed;
    }
}

