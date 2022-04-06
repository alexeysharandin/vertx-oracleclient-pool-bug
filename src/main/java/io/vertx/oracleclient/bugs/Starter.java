package io.vertx.oracleclient.bugs;

import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@ApplicationScoped
public class Starter {

    @Inject
    PgService pgService;

    @Inject
    AgoralService agoral;

    @Inject
    OracleService ora;

    void onStart(@Observes StartupEvent event) {
        System.out.println("Start test");
        agoral();
        pg();
        ora();
    }

    private void pg() {
        System.out.println("Start PGSql test");
        System.out.println("Press enable connectivity to Pgsql and press any key");
        readLine();
        pgService.init();
        pgService.testPgPool();
        System.out.println("Press disable connectivity to Pgsql and press any key");
        readLine();
        pgService.testPgPool();
        System.out.println("Press enable connectivity again to Pgsql and press any key");
        readLine();
        pgService.testPgPool();
    }

    private void agoral() {
        System.out.println("Start Agoral test");
        System.out.println("Press enable connectivity to Oracle and press any key");
        readLine();
        agoral.testAgoral();
        System.out.println("Press disable connectivity to Oracle and press any key");
        readLine();
        agoral.testAgoral();
        System.out.println("Press enable connectivity again to Oracle and press any key");
        readLine();
        agoral.testAgoral();
    }

    private void ora() {
        System.out.println("Start Verx Oracle test");
        System.out.println("Press enable connectivity to Oracle and press any key");
        readLine();
        ora.init();
        ora.testVertxPool();
        System.out.println("Press disable connectivity to Oracle and press any key");
        readLine();
        ora.testVertxPool();
        System.out.println("Press enable connectivity again to Oracle and press any key");
        readLine();
        ora.testVertxPool();
    }

    private void vertxOracle() {
        System.out.println("Start Vertx Oracle test");
        System.out.println("Press enable connectivity to Oracle and press any key");
        readLine();
        agoral.testAgoral();
        System.out.println("Press disable connectivity to Oracle and press any key");
        readLine();
        agoral.testAgoral();
        System.out.println("Press enable connectivity again to Oracle and press any key");
        readLine();
        agoral.testAgoral();
    }

    public static void readLine() {
        try {
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(System.in));
            br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

}
