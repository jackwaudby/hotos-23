package postgres;

import com.google.common.collect.ImmutableMap;
import org.neo4j.driver.Session;
import org.postgresql.ds.PGConnectionPoolDataSource;
import utils.Driver;
import utils.Metrics;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class PostgresOltpDriver implements Driver {
    private final PGConnectionPoolDataSource ds;

    private final Random rand;
    private final List<Long> personIds;
    private final Metrics metrics;
    private final BlockingQueue<Integer> in;
    private final BlockingQueue<Integer> out;

    private final boolean rw;

    public PostgresOltpDriver( Metrics metrics, BlockingQueue<Integer> in, BlockingQueue<Integer> out, List<Long> personIds, boolean rw) {
        this.ds = new PGConnectionPoolDataSource();
        ds.setDefaultAutoCommit(false);
        ds.setDatabaseName("ldbcsnb");
        ds.setServerName("localhost");
        ds.setPortNumber(5435);
        ds.setUser("postgres");
        ds.setPassword("mysecretpassword");
        this.rand = new Random();
        this.personIds = personIds;
        this.metrics = metrics;
        this.out = out;
        this.in = in;
        this.rw = rw;
    }


    public void run() throws InterruptedException {
        try (Connection conn = PostgresDriverUtils.startTransaction(ds); Statement st = conn.createStatement()) {
            while (in.size() == 0) {
                if (rw) {
                    readWriteTransaction(st);
                } else {
                    readTransaction(st);
                }
                PostgresDriverUtils.commitTransaction(conn);
                incCommitted();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        out.put(0);
    }

    public void readWriteTransaction(Statement st) throws SQLException {
        ImmutableMap<String,Object> params = ImmutableMap.of("personId", getPersonId());
        var write1 = "select * from person where id = $personId for update";
        var input1 = PostgresDriverUtils.substituteParameters(write1, params);
        st.execute(input1);
        var write2 = "update person set lastSeen = 209483257 where id = $personId";
        var input2 = PostgresDriverUtils.substituteParameters(write2, params);
        st.executeUpdate(input2);
    }

    public void readTransaction(Statement st) throws SQLException {
        ImmutableMap<String,Object> params = ImmutableMap.of("personId", getPersonId());
        var read = "select * from person where id = $personId";
        var input = PostgresDriverUtils.substituteParameters(read, params);

        st.executeQuery(input);
    }


    private void incCommitted() {
        this.metrics.increment();
    }

    private long getPersonId() {
        return personIds.get(rand.nextInt(personIds.size()));
    }
}


