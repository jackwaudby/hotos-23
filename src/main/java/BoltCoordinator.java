import com.google.common.collect.ImmutableMap;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class BoltCoordinator implements Coordinator, AutoCloseable {

    private final Driver driver;

    private final int persons;

    public BoltCoordinator(String uri, String user, String password, int persons) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        this.persons = persons;
    }

    @Override
    public void initialiseDatabase() {
        var txn = driver.session().beginTransaction();

        for (int i = 1; i <= persons; i++) {
            txn.run("CREATE (:Person {id: $personId})", ImmutableMap.of("personId", i));
        }

        for (int j = 1; j <= persons; j++) {
            for (int k = j + 1; k <= persons; k++) {
                txn.run("""
                        MATCH (a:Person {id: $person1Id}), (b:Person {id: $person2Id})
                        CREATE (a)-[r:KNOWS]->(b)""", ImmutableMap.of("person1Id", j, "person2Id", k));
            }
        }

        txn.commit();
        txn.close();
    }

    @Override
    public void tearDownDatabase() {
        var txn = driver.session().beginTransaction();
        txn.run("MATCH (n) DETACH DELETE n");
        txn.commit();
        txn.close();
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }
}
