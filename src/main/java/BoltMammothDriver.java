import com.google.common.collect.ImmutableMap;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.util.concurrent.BlockingQueue;

public class BoltMammothDriver implements MammothDriver, AutoCloseable {
    private final Driver driver;
    private final BlockingQueue<Integer> out;
    private final int delayInMillis;
    private final boolean balanced;
    private final Metrics metrics;

    public BoltMammothDriver(String uri, Metrics metrics, BlockingQueue<Integer> out, int delayInMillis, boolean balanced) {
        this.driver = GraphDatabase.driver(uri);
        this.out = out;
        this.delayInMillis = delayInMillis;
        this.balanced = balanced;
        this.metrics = metrics;
    }

    @Override
    public void run() throws InterruptedException {
        try (var session = driver.session()) {
            Thread.sleep(delayInMillis);
            mammothTransaction(session);
        }
        out.put(0);
    }


    @Override
    public void mammothTransaction(Session session) {
        System.out.println("Start mammoth transaction");
        var startTime = System.nanoTime();

        metrics.markMammoth();
        var txn = session.beginTransaction();
        if (balanced) {
            txn.run("""
                    MATCH p=(a:Person)-[:KNOWS*6]->(:Person)
                    WHERE NONE(node IN nodes(p) WHERE NOT (node)-[:IS_LOCATED_IN]-(:City)-[:IS_PART_OF]-(:Country {name: "Vietnam"}) )
                    FOREACH (n IN nodes(p) | SET n.hit = (CASE WHEN n.hit IS NULL THEN 0 ELSE n.hit + 1 END))""");
        } else {
            txn.run("""
                            MATCH (country:Country)<-[:IS_PART_OF]-(:City)<-[:IS_LOCATED_IN]-(person:Person)<-[:HAS_MEMBER]-(forum:Forum)
                            WHERE forum.creationDate > $date
                            WITH country, forum, count(person) AS numberOfMembers
                            ORDER BY numberOfMembers DESC, forum.id ASC, country.id
                            WITH DISTINCT forum AS topForum
                            LIMIT 10

                            WITH collect(topForum) AS topForums

                            CALL {
                              WITH topForums
                              UNWIND topForums AS topForum1
                              MATCH (topForum1)-[:CONTAINER_OF]->(post:Post)<-[:REPLY_OF*0..]-(message:Message)-[:HAS_CREATOR]->(person:Person)<-[:HAS_MEMBER]-(topForum2:Forum)
                              WITH person, message, topForum2
                              WHERE topForum2 IN topForums
                              RETURN person, count(DISTINCT message) AS messageCount
                            UNION ALL
                              // Ensure that people who are members of top forums but have 0 messages are also returned.
                              // To this end, we return each person with a 0 messageCount
                              WITH topForums
                              UNWIND topForums AS topForum1
                              MATCH (person:Person)<-[:HAS_MEMBER]-(topForum1:Forum)
                              RETURN person, 0 AS messageCount
                            }
                            WITH person, sum(messageCount) AS messageCount
                            ORDER BY
                              messageCount DESC,
                              person.id ASC
                            LIMIT 1000
                            SET person.posterType = "topPoster"
                            RETURN
                              person.id AS personId,
                              person.firstName AS personFirstName,
                              person.lastName AS personLastName,
                              person.creationDate AS personCreationDate,
                              sum(messageCount) AS messageCount,
                              person.posterType AS type""",
                    ImmutableMap.of("date", 1280102400000L));
        }
        txn.commit();
        txn.close();
        metrics.markMammoth();
        var endTime = System.nanoTime();
        var duration = (double) (endTime - startTime) / 1000000.0 / 1000.0;
        System.out.printf("Mammoth transaction finished in %.2f seconds\n", duration);
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }
}