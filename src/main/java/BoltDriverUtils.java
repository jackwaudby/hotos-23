import com.google.common.collect.ImmutableMap;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;

import java.util.ArrayList;
import java.util.List;

public class BoltDriverUtils implements AutoCloseable {
    private final Driver driver;
    private final List<Long> personIds;

    public BoltDriverUtils(String uri) {
        this.driver = GraphDatabase.driver(uri);
        this.personIds = new ArrayList<>();
    }

    public List<Long> getAllPersonIds(boolean balanced) {
        var experiment = balanced ? "balanced" : "unbalanced";
        System.out.printf("Getting parameters for %s experiment\n", experiment);

        try (var session = driver.session()) {
            session.executeRead(tx -> {
                Query query;
                if (balanced) {
                    query = new Query("""
                            MATCH (p:Person) RETURN p.id""");
                } else {
                    query = new Query("""
                             MATCH (country:Country)<-[:IS_PART_OF]-(:City)<-[:IS_LOCATED_IN]-(person:Person)<-[:HAS_MEMBER]-(forum:Forum)
                                WHERE forum.creationDate > $date
                                WITH country, forum, count(person) AS numberOfMembers
                                ORDER BY numberOfMembers DESC, forum.id ASC, country.id
                                WITH DISTINCT forum AS topForum
                                LIMIT 100
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
                                RETURN
                                person.id AS personId\
                            """, ImmutableMap.of("date", 1280102400000L));
                }

                var result = tx.run(query);
                result.stream().forEach(record -> personIds.add(record.get(0).asLong()));

                return null;
            });
        }
        System.out.printf("Acquired %s parameters\n", personIds.size());
        return personIds;
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }
}
