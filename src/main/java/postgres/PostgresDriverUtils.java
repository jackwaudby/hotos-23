package postgres;

import org.postgresql.ds.PGConnectionPoolDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class PostgresDriverUtils {
    static final String isolation_serializable = "set transaction isolation level serializable";
    static final String isolation_repeatable_read = "set transaction isolation level repeatable read";
    static final String isolation_read_committed = "set transaction isolation level read committed";

    public static Connection startTransaction(PGConnectionPoolDataSource ds) throws SQLException {
        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        conn.createStatement().executeUpdate(PostgresDriverUtils.isolation_repeatable_read);
        return conn;
    }

    public static void commitTransaction(Connection tt) throws SQLException {
        tt.commit();
    }

    public static String substituteParameters(String querySpecification, Map<String, Object> stringStringMap) {
        // substitute parameters
        if (stringStringMap != null) {
            for (Map.Entry<String, Object> param : stringStringMap.entrySet()) {
                querySpecification = querySpecification.replace("$" + param.getKey(), param.getValue().toString());
            }
        }

        return querySpecification;
    }
}
