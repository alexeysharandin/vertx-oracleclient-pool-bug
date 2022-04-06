package io.vertx.oracleclient.bugs;

import io.agroal.api.AgroalDataSource;
import io.agroal.pool.wrapper.ConnectionWrapper;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.oracleclient.OraclePool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.CommandHandler;
import io.vertx.oracleclient.impl.OracleConnectionImpl;
import io.vertx.sqlclient.PoolOptions;
import oracle.jdbc.OracleConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped
public class OracleService {
    private static final Logger oraclelog = LogManager.getLogger("oracle");

    private static final int MAX = 10;

    @ConfigProperty(name = "oracle.db.host")
    String HOST;

    @ConfigProperty(name = "oracle.db.name")
    String DB;

    @ConfigProperty(name = "oracle.db.user")
    String USER;

    @ConfigProperty(name = "oracle.db.password")
    String PASSWORD;

    @ConfigProperty(name = "oracle.db.port")
    int PORT;

    @Inject
    AgroalDataSource ds;

    @Inject
    Vertx vertx;

    OraclePool oraclePool;

    void testVertxPool() {
        oraclelog.info("Test Oracle over Vertx");

        for (int i = 0; i < MAX; i++) {
            SqlConnection conn = null;
            RowSet<Row> rows = null;
            long startTime = System.currentTimeMillis();
            try {
                long getstart = System.currentTimeMillis();
                conn = oraclePool.getConnectionAndAwait();
                unwrap(conn);
                oraclelog.info("Iteration {}. Connection hash: {}", i, conn);
                long getend = System.currentTimeMillis();
                oraclelog.info("Getting connection time {} in {} ms", i, (getend - getstart));

                long warmStart = System.currentTimeMillis();
                rows = conn.query("select 1 from dual").executeAndAwait();
                long warmEnd = System.currentTimeMillis();

                oraclelog.info("Exec query '{}' in {}ms", i, (warmEnd - warmStart));
                long endTime = System.currentTimeMillis();
                oraclelog.info("Total for Vertx test '{}' is {} ms", i, (endTime - startTime));
            } catch (Exception e) {
                oraclelog.info(i + ":" + e.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.closeAndAwait();
                    } catch (Exception ignored) {

                    }
                }
            }
        }
        oraclelog.info("Vertx Pool Test finished\n");
    }

    private void unwrap(io.vertx.mutiny.sqlclient.SqlConnection conn) {
        try {
            OracleConnectionImpl delegate = (OracleConnectionImpl) conn.getDelegate();
            io.vertx.sqlclient.impl.Connection unwrap = delegate.unwrap();
            CommandHandler handler = (CommandHandler) unwrap.unwrap();

            oraclelog.info("delegate = " + delegate);
            oraclelog.info("unwrap = " + unwrap);
            oraclelog.info("handler = " + handler);

            Field field = handler.getClass().getDeclaredField("connection");
            field.setAccessible(true);
            Object o = field.get(handler);
            oraclelog.info("o = " + o);
            dump((Connection) o);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dump(Connection o) throws SQLException {
        if (o instanceof ConnectionWrapper) {
            o = ((ConnectionWrapper) o).unwrap(OracleConnection.class);
        }
        if (o instanceof oracle.jdbc.driver.OracleConnection) {
            System.out.println("Connection is Oracle");
            OracleConnection oracleConnection = (OracleConnection) o;
            int ping = oracleConnection.pingDatabase();
            String netConnectionId = oracleConnection.getNetConnectionId();
            int hashCode = oracleConnection.hashCode();
            boolean usable = oracleConnection.isUsable();
            boolean closed = oracleConnection.isClosed();
            oraclelog.info("hashCode = " + hashCode);
            oraclelog.info("[" + hashCode + "]ping = " + ping);
            oraclelog.info("[" + hashCode + "]netConnectionId = " + netConnectionId);
            oraclelog.info("[" + hashCode + "]usable = " + usable);
            oraclelog.info("[" + hashCode + "]closed = " + closed);
        } else {
            oraclelog.info("Non Oracle connection");
        }
    }

    private OraclePool createPool() {
        OracleConnectOptions connectOptions = new OracleConnectOptions()
                .setPort(PORT)
                .setHost(HOST)
                .setDatabase(DB)
                .setUser(USER)
                .setPassword(PASSWORD)
                .setCachePreparedStatements(true)
                .setPreparedStatementCacheMaxSize(10)
                .setTracingPolicy(TracingPolicy.ALWAYS);

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(10);

        return OraclePool.pool(vertx, connectOptions, poolOptions);
    }

    public void init() {
        oraclePool = createPool();
    }
}
