package cz.hiro.vertxdb;

import cz.hiro.ContactsData.Person;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class HSqlDb extends VertxDb{

    private static final String SQL_CREATE_PERSONS_TABLE_TABLE = "create table if not exists Persons (Id integer identity primary key, FirstName varchar(255), LastName varchar(255))";
    private static final String SQL_GET_PERSON = "select Id, FirstName, LastName FROM Persons where Id = ?";
    private static final String SQL_CREATE_PERSON = "insert into Persons values (NULL, ?, ?)";
    private static final String SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?";
    private static final String SQL_ALL_PERSONS = "select * from Persons";
    private static final String SQL_DELETE_PAGE = "delete from Pages where Id = ?";

    public JDBCClient dbClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxDb.class);

    public Future<Void> create(Vertx vertx){
        return prepareDatabase(vertx);
    }

    public Future<SQLConnection> tryConnect() {
        Future<SQLConnection> future = Future.future();
        dbClient.getConnection(it -> {
            if (it.succeeded()) {
                future.complete(it.result());
            } else {
                future.fail(it.cause());
            }
        });
        return future;
    }


    private Future<Void> prepareDatabase(Vertx vertx) {
        Promise<Void> promise = Promise.promise();

        dbClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:file:db/wiki")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));

        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                promise.fail(ar.cause());
            } else {
                SQLConnection connection = ar.result();
                connection.execute(SQL_CREATE_PERSONS_TABLE_TABLE, create -> {
                    connection.close();
                    if (create.failed()) {
                        LOGGER.error("Database preparation error", create.cause());
                        promise.fail(create.cause());
                    } else {
                        promise.complete();
                    }
                });
            }
        });

        return promise.future();
    }

//    public CompletableFuture<List<JsonArray> > getContacts() {
//        CompletableFuture <List<JsonArray> > rt = new CompletableFuture <List<JsonArray> >();
//        rt.runAsync(new Runnable() {
//            @Override
//            public void run() {
//                dbClient.getConnection(car -> {
//                    if (car.succeeded()) {
//                        SQLConnection connection = car.result();
//
//                        connection.query(SQL_ALL_PERSONS, res -> {
//                            connection.close();
//
//                            if (res.succeeded()) {
//                                List<JsonArray> pages = res.result().getResults();
//                                rt.complete(pages);
//                            } else {
//                                rt.completeExceptionally(new Exception("QUERY FAILED"));
//                            }
//                        });
//                    } else {
//                        rt.completeExceptionally(new Exception("CONNECTION FAILED"));
//                    }
//                });
//            }});
//        return rt;
//    }

    public Future<List<JsonObject> > getContacts() {
        Promise<List<JsonObject>> promise = Promise.promise();

        tryConnect().onSuccess(conn -> {
            conn.query(SQL_ALL_PERSONS, res -> {
                conn.close();

                if (res.succeeded()) {
                    promise.complete(res.result().getRows(true));
                } else {
                    promise.fail(res.cause());
                }
            });

        });

        return promise.future();
    }

    public Future<JsonObject> getContact(int id) {
        Promise<JsonObject> promise = Promise.promise();

        tryConnect().onSuccess(conn -> {
            JsonArray params = new JsonArray();
            params.add(id);
            conn.queryWithParams(SQL_GET_PERSON, params, res -> {
                conn.close();
                if (res.succeeded()) {
                    promise.complete(res.result().getRows(true).get(0));
                } else {
                    promise.fail(res.cause());
                }
            });

        });

        return promise.future();
    }

    public Future  addPerson(int id, Person person) {
        Promise promise = Promise.promise();

        tryConnect().onSuccess(conn -> {
                JsonArray params = person.toJsonArray();
                conn.updateWithParams(SQL_CREATE_PERSON, params, res -> {
                    conn.close();


                    if (res.succeeded()) {
                        promise.complete();
                    } else {
                        promise.fail(res.cause());
                    }
                });
        });

        return promise.future();
    }



//    public List<JsonArray>  getContacts2() {
//        List<JsonArray> pages;
//        Promise<void> promise = Promise.promise();
//
//                dbClient.getConnection(car -> {
//                    if (car.succeeded()) {
//                        SQLConnection connection = car.result();
//
//                        connection.query(SQL_ALL_PERSONS, res -> {
//                            connection.close();
//
//                            if (res.succeeded()) {
//                                pages = res.result().getResults();
//                                promise.complete();
//                            } else {
//                                promise.fail("QUERY FAILED");
//                            }
//                        });
//                    } else {
//                        promise.fail("CONNECTION FAILED");
//                    }
//                });
//        Future<void> f = promise.future();
//        f.result();
//    }
}
