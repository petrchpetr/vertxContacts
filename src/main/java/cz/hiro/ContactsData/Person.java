package cz.hiro.ContactsData;

        import io.vertx.core.json.JsonArray;

        import java.util.concurrent.atomic.AtomicInteger;

public class Person {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private int id;

    private String FirstName;

    private String LastName;

    public Person(String FirstName, String LastName) {
        this.id = COUNTER.getAndIncrement();
        this.FirstName = FirstName;
        this.LastName = LastName;
    }

    public Person() {
        this.id = COUNTER.getAndIncrement();
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public int getId() {
        return id;
    }

    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }

    public void setLastName(String LastName) {
        this.LastName = LastName;
    }

    public JsonArray toJsonArray() {
        JsonArray params = new JsonArray();
        params.add(FirstName).add(LastName);
        return params;
    }

}