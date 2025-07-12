package project.impl;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import project.docstore.Document;
import project.docstore.impl.DocumentImpl;
import project.docstore.impl.DocumentPersistenceManager;

class BTreeImplTest {

    @Test
    void get() {
        BTreeImpl<Integer, String> st = new BTreeImpl<>();
        assertEquals("one",st.put(1, "one"));
        st.put(2, "two");
        st.put(3, "three");
        st.put(4, "four");
        st.put(5, "five");
        st.put(6, "six");
        st.put(7, "seven");
        st.put(8, "eight");
        st.put(9, "nine");
        st.put(10, "ten");
        st.put(11, "eleven");
        st.put(12, "twelve");
        st.put(13, "thirteen");
        st.put(14, "fourteen");
        st.put(15, "fifteen");
        st.put(16, "sixteen");
        st.put(17, "seventeen");
        st.put(18, "eighteen");
        st.put(19, "nineteen");
        st.put(20, "twenty");
        st.put(21, "twenty one");
        st.put(22, "twenty two");
        st.put(23, "twenty three");
        st.put(24, "twenty four");
        st.put(25, "twenty five");
        st.put(26, "twenty six");

        assertEquals("twenty six", st.get(26));

    }

    @Test
    void put() {
        //Test to support nulls, that are in memory (implicitly deleted in the scope of the DocumentStore)
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/253");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/254");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/255");
        String doc1 = "doc1";
        String doc2 = "doc2 keyword";
        String doc3 = "doc3 and then some";
        BTreeImpl<URI, Document> st = new BTreeImpl<>();
        st.setPersistenceManager(new DocumentPersistenceManager(null));
        Document docFirst = new DocumentImpl(uri, doc1, null);
        Document docSecond = new DocumentImpl(uri2, doc2, null);
        Document docThird = new DocumentImpl(uri3, doc3, null);
        st.put(uri, docFirst);
        st.put(uri2, docSecond);
        st.put(uri3, docThird);
        assertEquals(docFirst.getDocumentTxt(), st.get(uri).getDocumentTxt());
        assertEquals(docSecond.getDocumentTxt(), st.get(uri2).getDocumentTxt());
        assertEquals(docThird.getDocumentTxt(), st.get(uri3).getDocumentTxt());
        st.put(uri3, null);
        assertNull(st.get(uri3));
    }

    @Test
    void moveToDisk() throws IOException {
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/253");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/254");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/255");
        String doc1 = "doc1";
        String doc2 = "doc2 keyword";
        String doc3 = "doc3 and then some";
        BTreeImpl<URI, Document> st = new BTreeImpl<>();
        st.setPersistenceManager(new DocumentPersistenceManager(null));
        Document docFirst = new DocumentImpl(uri, doc1, null);
        Document docSecond = new DocumentImpl(uri2, doc2, null);
        Document docThird = new DocumentImpl(uri3, doc3, null);
        st.put(uri, docFirst);
        st.put(uri2, docSecond);
        st.put(uri3, docThird);
        assertEquals(docFirst.getDocumentTxt(), st.get(uri).getDocumentTxt());
        assertEquals(docSecond.getDocumentTxt(), st.get(uri2).getDocumentTxt());
        assertEquals(docThird.getDocumentTxt(), st.get(uri3).getDocumentTxt());
        //Move doc2 into memory
        st.moveToDisk(uri2);
        //checks if all can still be accessed, makes sure that the PM knows its out of memory
        assertEquals(docFirst.getDocumentTxt(), st.get(uri).getDocumentTxt());
        assertEquals(docSecond.getDocumentTxt(), st.get(uri2).getDocumentTxt());
        assertEquals(docThird.getDocumentTxt(), st.get(uri3).getDocumentTxt());
    }

    @Test
    void setPersistenceManager() {
    }
}