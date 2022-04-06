# bug-with-pool-connections Project

###Configuration:

**quarkus.version:** 2.7.3.Final

**vertx-oracle-client:** 4.2.5.1

**smallrye-mutiny-vertx-oracle-client:** 2.18.1

**ojdbc11:** 21.4.0.0

The application can be packaged using:
```shell script
./mvnw package
```

The application can be executed using:
```shell script
java -jar ./target/bug-with-pool-connections-1.0.0-SNAPSHOT-runner.jar
```

### Information about repo
This repo contains classes:  
0. Starter - starter class  
1. OracleService - execute "select 1 from dual" over io.vertx.mutiny.oracleclient.OraclePool
2. AgoralService - execute "select 1 from dual" over AgoralDataSource without vertx - reason to create this class = to show what Oracle driver working properly with different Pool/DS 
3. PgService - execute "select 1" over io.vertx.mutiny.pgclient.PgPool - reason to create this class = to separate vertx-sql-* and vertx-oracle-*

### Logging
OracleService > logs/oracle.log  
AgoralService > logs/agoral.log  
PgService > logs/pgsql.log  
ALL > logs/default.log  

### Configuration
update application.properties and configure:

oracle.db.host=[configure]  
oracle.db.name=[configure]   
oracle.db.user=[configure]  
oracle.db.password=[configure]    
oracle.db.port=[configure]

pg.db.port=5432  
pg.db.host=localhost  
pg.db.database=postgres  
pg.db.user=postgres  
pg.db.password=postgres  


### How to reproduce
0. Enable connectivity to Oracle/PG
1. Run application
2. Read information from console and make actions based on this info. (1st iter - run with enabled connectivity. 2nd iter - with disabled. 3rd - with restored)
3. Review logs

## Expected Results vs Actual Results
#####ER: 
- When connectivity will be dropped all services start to log errors.  
- When connectivity will be restored all services will restore(recreate) connections and continue working


#####AR: 
- When connectivity will be dropped all services start to log errors. [Passed]
- When connectivity will be restored all services will restore(recreate) connections and continue working **[Failed]**.   
Agoral and Pg connection restored and continue working. Oracle Reactive: **no**.  
Oracle Pool keeping closed connection in the pool (please see "closed = true" in logs) and try to use them for queries.

Issue reproduced in Win10 and RHEL 8.3+

## Logs examples

[Agoral](Agoral_Log.md)  
[Vertx-PgSql](PgSql_Log.md)  
[Verx-Oracle](Ora_Log.md)  

