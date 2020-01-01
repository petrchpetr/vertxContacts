package cz.hiro.Contacts;

import cz.hiro.ContactsData.Person;
import cz.hiro.vertxdb.HSqlDb;
import cz.hiro.vertxdb.VertxDb;
import io.vertx.core.*;

import io.vertx.core.http.HttpServer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxDb.class);
    private static final HSqlDb DB = new HSqlDb();

    @Override
    public void start(Promise<Void> promise)  throws Exception  {
        Future<Void> steps = DB.create(vertx).compose(v -> startHttpServer());
        steps.setHandler(ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
    }

    private Future<Void> startHttpServer() {
        Promise<Void> promise = Promise.promise();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/persons").handler(this::indexHandler);
        router.get("/persons/:id").handler(this::personGetHandler);
        router.post().handler(BodyHandler.create());
        router.post("/persons").handler(this::personPostHandler);
        router.delete("/persons/:id").handler(this::personDeleteHandler);
//        router.get("/wiki/:page").handler(this::pageRenderingHandler);
//        router.post().handler(BodyHandler.create());
//        router.post("/save").handler(this::pageUpdateHandler);
//        router.post("/create").handler(this::pageCreateHandler);
//        router.post("/delete").handler(this::pageDeletionHandler);

//        templateEngine = FreeMarkerTemplateEngine.create(vertx);

        server
                .requestHandler(router)
                .listen(8080, ar -> {
                    if (ar.succeeded()) {
                        LOGGER.info("HTTP server running on port 8080");
                        promise.complete();
                    } else {
                        LOGGER.error("Could not start a HTTP server", ar.cause());
                        promise.fail(ar.cause());
                    }
                });

        return promise.future();
    }

    private void personGetHandler(RoutingContext routingContext) {
        int id = parseInt(routingContext.request().getParam("id"));

        final Person person;
        try {
            DB.getContact(id).onSuccess(res -> {
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(res));
            });
        }catch(DecodeException e){
            LOGGER.error(e.getMessage());
        }
    }

    private void personPostHandler(RoutingContext routingContext) {
        String body = routingContext.getBodyAsString();
        final Person person;
        try {
            // final Object o = Json.decodeValue(body);
             person = Json.decodeValue(body,
                    Person.class);

            DB.addPerson(person.getId(), person).onSuccess(res -> {
                    routingContext.response()
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encodePrettily(person));
            });
        }catch(DecodeException e){
            LOGGER.error(e.getMessage());
        }
    }

//    private void personPostHandler(RoutingContext context) {
//        DB.getContacts().onSuccess(contacts -> {
//                    context.response()
//                            .putHeader("content-type", "application/json; charset=utf-8")
//                            .end(contacts.toString());
//                }
//        );
//        LOGGER.info("NOT IMPLEMENTED");
//        context.response().end("not implemented");
//    }

    private void personDeleteHandler(RoutingContext context) {
        LOGGER.info("NOT IMPLEMENTED");
        context.response().end("not implemented");
    }


//    private void indexHandler(RoutingContext context) {
//        try {
//            List<JsonArray> contacts = DB.getContacts().get();
//            context.response()
//                    .putHeader("content-type", "application/json; charset=utf-8")
//                    .end(contacts.toString());
//        } catch(Exception e){
//            LOGGER.error(e.getMessage());
//        }
//    }

    private void indexHandler(RoutingContext context) {
        DB.getContacts().onSuccess(contacts -> {
            context.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(contacts.toString());
                }
        );
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hello");

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
