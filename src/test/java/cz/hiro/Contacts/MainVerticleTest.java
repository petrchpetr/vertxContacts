package cz.hiro.Contacts;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext tc) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(MainVerticle.class.getName(), tc.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext tc) {
        vertx.close(tc.asyncAssertSuccess());
    }

    @Test
    public void testThatThePersonsIsCreated(TestContext tc) {
        Async async = tc.async();
        JsonObject jo = new JsonObject();
        jo
                .put("firstName", "TestName")
                .put("lastName", "TestLastName");

        WebClient client = WebClient.create(vertx);


        client.post(8080, "localhost", "/persons")
                .sendBuffer(jo.toBuffer(), ar -> {
                    if (ar.succeeded()) {
                        // Ok
                        tc.assertEquals(ar.result().statusCode(), 201);
                    }else{
                        tc.fail();
                    }
                    async.complete();
                });
    }



    @Test
    public void testThatThePersonsAreReturned(TestContext tc) {
        Async async = tc.async();
        vertx.createHttpClient().getNow(8080, "localhost", "/persons", response -> {
            tc.assertEquals(response.statusCode(), 200);
            response.bodyHandler(body -> {
                tc.assertTrue(body.length() > 0);
                async.complete();
            });
        });
    }

    @Test
    public void testThatGetReturnsAPerson(TestContext tc) {
        Async async = tc.async();
        vertx.createHttpClient().getNow(8080, "localhost", "/persons/1", response -> {
            tc.assertEquals(response.statusCode(), 200);
            response.bodyHandler(body -> {
                tc.assertTrue(body.length() > 0);
                async.complete();
            });
        });
    }
}