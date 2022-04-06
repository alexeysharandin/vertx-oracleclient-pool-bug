package io.vertx.oracleclient.bugs;

import io.vertx.core.tracing.TracingPolicy;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PgService {
    private static final Logger pglog = LogManager.getLogger("pgsql");
    private static final int MAX = 10;

    @ConfigProperty(name = "pg.db.port")
    int pgPort;

    @ConfigProperty(name = "pg.db.host")
    String pgHost;

    @ConfigProperty(name = "pg.db.database")
    String pgDatabase;

    @ConfigProperty(name = "pg.db.user")
    String pgUser;

    @ConfigProperty(name = "pg.db.password")
    String pgPass;

    @Inject
    Vertx vertx;

    PgPool pgPool;

    void testPgPool() {
        pglog.info("Test PgPool over Vertx");

        for (int i = 0; i < MAX; i++) {
            SqlConnection conn = null;
            RowSet<Row> rows = null;
            long startTime = System.currentTimeMillis();
            try {
                long getstart = System.currentTimeMillis();
                conn = pgPool.getConnectionAndAwait();
                //unwrap(conn);
                pglog.info("Iteration {}. Connection hash: {}", i, conn);
                long getend = System.currentTimeMillis();
                pglog.info("Getting connection time {} in {} ms", i, (getend - getstart));

                long warmStart = System.currentTimeMillis();
                rows = conn.query("SELECT 1").executeAndAwait();
                long warmEnd = System.currentTimeMillis();

                pglog.info("Exec query '{}' in {}ms", i, (warmEnd - warmStart));
                long endTime = System.currentTimeMillis();
                pglog.info("Total for PgTest test '{}' is {} ms", i, (endTime - startTime));
            } catch (Exception e) {
                pglog.info(i + ":" + e.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.closeAndAwait();
                    } catch (Exception ignored) {

                    }
                }
            }
        }
        pglog.info("Vertx Pool Test finished\n");
    }


    private PgPool createPgPool() {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(pgPort)
                .setHost(pgHost)
                .setDatabase(pgDatabase)
                .setUser(pgUser)
                .setPassword(pgPass)
                .setCachePreparedStatements(true)
                .setPreparedStatementCacheMaxSize(5)
                .setTracingPolicy(TracingPolicy.ALWAYS);

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        return PgPool.pool(vertx, connectOptions, poolOptions);
    }

    public void init() {
        pgPool = createPgPool();
    }
}
