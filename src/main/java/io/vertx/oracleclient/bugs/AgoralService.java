package io.vertx.oracleclient.bugs;

import io.agroal.api.AgroalDataSource;
import io.agroal.pool.wrapper.ConnectionWrapper;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.oracleclient.OraclePool;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.CommandHandler;
import io.vertx.oracleclient.impl.OracleConnectionImpl;
import io.vertx.pgclient.PgConnectOptions;
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
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ApplicationScoped
public class AgoralService {
    private static final Logger agorallog = LogManager.getLogger("agoral");

    private static final int MAX = 10;

    @Inject
    AgroalDataSource ds;

    @Inject
    Vertx vertx;

    void testAgoral() {
        agorallog.info("Agoral Test started");
        Connection conn = null;
        PreparedStatement ps = null;
        for (int i = 0; i < MAX; i++) {
            long startTime = System.currentTimeMillis();
            try {
                long connStartTime = System.currentTimeMillis();
                conn = ds.getConnection();
                long connEndTime = System.currentTimeMillis();

                agorallog.info("Iteration {}. Connection hash: {}", i, conn);
                agorallog.info("Getting connection time {} in {}ms", i, (connEndTime - connStartTime));
                dump(conn);
                long execStart = System.currentTimeMillis();

                ps = conn.prepareStatement("select 1 from dual");
                ps.execute();
                long execEnd = System.currentTimeMillis();

                agorallog.info("Exec query '{}' in {}ms", i, (execEnd - execStart));
                long endTime = System.currentTimeMillis();
                agorallog.info("Total for Agoral test '{}' is {}ms", i, (endTime - startTime));
            } catch (Exception ex) {
                agorallog.info(ex);
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException ignored) {
                    }
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException ignored) {
                    }
                }
            }
        }

        agorallog.info("Agoral Test finished\n");
    }

    private void unwrap(SqlConnection conn) {
        try {
            OracleConnectionImpl delegate = (OracleConnectionImpl) conn.getDelegate();
            io.vertx.sqlclient.impl.Connection unwrap = delegate.unwrap();
            CommandHandler handler = (CommandHandler) unwrap.unwrap();

            agorallog.info("delegate = " + delegate);
            agorallog.info("unwrap = " + unwrap);
            agorallog.info("handler = " + handler);

            Field field = handler.getClass().getDeclaredField("connection");
            field.setAccessible(true);
            Object o = field.get(handler);
            agorallog.info("o = " + o);
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
            agorallog.info("Connection is Oracle");
            OracleConnection oracleConnection = (OracleConnection) o;
            int ping = oracleConnection.pingDatabase();
            String netConnectionId = oracleConnection.getNetConnectionId();
            int hashCode = oracleConnection.hashCode();
            boolean usable = oracleConnection.isUsable();
            boolean closed = oracleConnection.isClosed();
            agorallog.info("hashCode = " + hashCode);
            agorallog.info("[" + hashCode + "]ping = " + ping);
            agorallog.info("[" + hashCode + "]netConnectionId = " + netConnectionId);
            agorallog.info("[" + hashCode + "]usable = " + usable);
            agorallog.info("[" + hashCode + "]closed = " + closed);
        } else {
            agorallog.info("Non Oracle connection");
        }
    }
}
