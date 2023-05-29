package postgres;

import org.postgresql.ds.PGConnectionPoolDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;

import utils.Driver;
import utils.Metrics;

public class PostgresMammothDriver implements Driver {

    public static final String balancedMammoth = """
            UPDATE person
              SET hit = hit + sub.hops
              FROM (
                -- 2-hop
                SELECT k1.person2id AS id, 2 AS hops
                FROM person a
                JOIN person_knows_person k1 ON k1.person1id = a.id
                UNION
                -- 3-hop
                SELECT k2.person2id AS id, 3 AS hops
                FROM person a
                JOIN person_knows_person k1 ON k1.person1id = a.id
                JOIN person_knows_person k2 ON k2.person1id = k1.person2id AND k2.person2id != a.id
              ) AS sub
              WHERE person.id = sub.id""";

    //    \set date '\'2010-07-26\''::timestamp
    public static final String unbalancedMammoth = """
                WITH Top100_Popular_Forums AS (
              SELECT DISTINCT ForumId AS id, max(numberOfMembers) AS maxNumberOfMembers
              FROM (
               SELECT Forum.id AS ForumId, count(Person.id) AS numberOfMembers, Country.id AS CountryId
                  FROM Forum_hasMember_Person
                  JOIN Person
                    ON Person.id = Forum_hasMember_Person.PersonId
                  JOIN City
                    ON City.id = Person.LocationCityId
                  JOIN Country
                    ON Country.id = City.PartOfCountryId
                  JOIN Forum
                    ON Forum_hasMember_Person.ForumId = Forum.id
                   AND Forum.creationDate > '2010-07-26'
                  GROUP BY Country.Id, Forum.Id
              ) ForumMembershipPerCountry
              GROUP BY ForumId
              ORDER BY maxNumberOfMembers DESC, ForumId
              LIMIT 100
            )
            SELECT au.id AS "person.id"
                 , au.firstName AS "person.firstName"
                 , au.lastName AS "person.lastName"
                 , au.creationDate
                 -- a single person might be member of more than 1 of the top100 forums, so their messages should be DISTINCT counted
                 , count(Message.id) AS messageCount
              FROM
                   Person au
                   LEFT JOIN Message
                          ON au.id = Message.CreatorPersonId
                         AND Message.ContainerForumId IN (SELECT id FROM Top100_Popular_Forums)
                         AND Message.creationDate > '2010-07-26'
              WHERE EXISTS (SELECT * FROM Top100_Popular_Forums INNER JOIN
                            Forum_hasMember_Person ON Top100_Popular_Forums.id =
                            Forum_hasMember_Person.ForumId WHERE Forum_hasMember_Person.PersonId = au.id
            )
            GROUP BY au.id, au.firstName, au.lastName, au.creationDate
            ORDER BY messageCount DESC, au.id
            LIMIT 100
            ;
            """;

    private final PGConnectionPoolDataSource ds;
    private final Metrics metrics;
    private final boolean balanced;
    private final int delayInMillis;
    private final BlockingQueue<Integer> out;


    public PostgresMammothDriver(Metrics metrics, BlockingQueue<Integer> out, int delayInMillis, boolean balanced) {
        this.out = out;
        this.delayInMillis = delayInMillis;
        this.balanced = balanced;
        this.metrics = metrics;
        this.ds = new PGConnectionPoolDataSource();
        ds.setDefaultAutoCommit(false);
        ds.setDatabaseName("ldbcsnb");
        ds.setServerName("localhost");
        ds.setPortNumber(5432);
        ds.setUser("postgres");
        ds.setPassword("mysecretpassword");
    }

    @Override
    public void run() throws InterruptedException {
        Thread.sleep(delayInMillis);

        System.out.println("Start mammoth transaction");
        var startTime = System.nanoTime();
        metrics.markMammoth();

        try (Connection conn = PostgresDriverUtils.startTransaction(ds); Statement st = conn.createStatement()) {
            if (balanced) {
                st.execute(balancedMammoth);
            } else {
                st.execute(unbalancedMammoth);
            }
            PostgresDriverUtils.commitTransaction(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        metrics.markMammoth();
        var endTime = System.nanoTime();
        var duration = (double) (endTime - startTime) / 1000000.0 / 1000.0;
        System.out.printf("Mammoth transaction finished in %.2f seconds\n", duration);
        out.put(0);
    }
}
