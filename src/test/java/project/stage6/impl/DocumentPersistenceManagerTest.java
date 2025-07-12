package project.stage6.impl;

import project.stage6.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class DocumentPersistenceManagerTest {

    @Test
    void serialize() throws IOException {
    DocumentPersistenceManager pm = new DocumentPersistenceManager(null);
    URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/253");
    System.out.println(System.getProperty("user.dir"));
    String doc1 = "Hello world how are you";
    DocumentImpl doc = new DocumentImpl(uri, doc1, null);
    pm.serialize(uri, doc);
    Document doc2 = pm.deserialize(uri);
    assertTrue(doc.equals(doc2));
    System.out.println("deleting....");
    assertTrue(pm.delete(uri));
    }

    @Test
    void deserialize() {
    }

    @Test
    void delete() {
    }
}