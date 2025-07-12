package project.stage6.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import project.stage6.Document;
import project.stage6.DocumentStore;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStoreImplTest {

    @Test
    void testCorrectlyStoringToDisk() throws IOException {
        DocumentStore st = new DocumentStoreImpl();
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc3");
        String doc1 = "doc1";
        String doc2 = "doc2";
        String doc3 = "doc3";

        st.setMaxDocumentCount(2);
        System.out.println("doc1 insert");
        st.put(new ByteArrayInputStream(doc1.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        System.out.println("doc1 insert done");
        assertEquals(doc1, st.get(uri).getDocumentTxt());
        System.out.println("doc1 get done");
        System.out.println("doc2 insert");
        st.put(new ByteArrayInputStream(doc2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        System.out.println("doc2 insert done");
        assertEquals(doc2, st.get(uri2).getDocumentTxt());
        System.out.println("doc2 get done");
        //Doc1 should be pushed into memory
        System.out.println("doc1 push into mem, doc3 insert");
        st.put(new ByteArrayInputStream(doc3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        System.out.println("doc1 push into mem, doc3 insert done");
        assertEquals(doc3, st.get(uri3).getDocumentTxt());
        System.out.println("doc3 get done");
        //Doc2 should be pushed into memory
        System.out.println("doc1 get doc 2 into disk");
        assertEquals(doc1, st.get(uri).getDocumentTxt());
        System.out.println("doc1 get doc 2 into disk done");
    }

    @Test
    void testSearchMethods() throws IOException{
        DocumentStore st = new DocumentStoreImpl();
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc3");
        String doc1 = "doc1";
        String doc2 = "doc2";
        String doc3 = "doc3";
        st.put(new ByteArrayInputStream(doc1.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        st.put(new ByteArrayInputStream(doc2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        st.put(new ByteArrayInputStream(doc3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        st.setMaxDocumentCount(2);
        st.deleteAllWithPrefix("doc");
        assertNull(st.get(uri));
        assertNull(st.get(uri2));
        assertNull(st.get(uri3));


    }

    @Test
    void testBinaryFunctions() throws IOException{
        DocumentStore st = new DocumentStoreImpl();
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc3");
        byte[] doc1 = "docA".getBytes();
        byte[] doc2 = "docB".getBytes();
        byte[] doc3 = "docC".getBytes();
        st.put(new ByteArrayInputStream(doc1), uri, DocumentStore.DocumentFormat.BINARY);
        st.put(new ByteArrayInputStream(doc2), uri2, DocumentStore.DocumentFormat.BINARY);
        st.setMaxDocumentCount(2);
        st.put(new ByteArrayInputStream(doc3), uri3, DocumentStore.DocumentFormat.BINARY);
        assertArrayEquals(doc1, st.get(uri).getDocumentBinaryData());
        assertArrayEquals(doc2, st.get(uri2).getDocumentBinaryData());
        assertArrayEquals(doc3, st.get(uri3).getDocumentBinaryData());


    }

    @Test
    public void testUndoBringBackIntoMemory() throws IOException{
        DocumentStore st = new DocumentStoreImpl();
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc3");
        String doc1 = "doc1";
        String doc2 = "doc2";
        String doc3 = "doc3";
        st.put(new ByteArrayInputStream(doc1.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        st.put(new ByteArrayInputStream(doc2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        st.setMaxDocumentBytes(8);
        st.put(new ByteArrayInputStream(doc3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        st.undo();
        assertEquals(doc1, st.get(uri).getDocumentTxt());
        assertEquals(doc2, st.get(uri2).getDocumentTxt());
        assertNull(st.get(uri3));


    }

    @Test
    public void testSearchMethodsTwo() throws IOException{
        ArrayList<Document> testList = new ArrayList<>();
        HashSet<String> keywords = new HashSet<>();
        DocumentStore st = new DocumentStoreImpl();
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc3");
        String doc1 = "doc1";
        String doc2 = "doc2";
        String doc3 = "doc3";
        String doc3Replace = "doc325";
        keywords.add(doc1);
        keywords.add(doc2);
        keywords.add(doc3);
        st.put(new ByteArrayInputStream(doc1.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        testList.add(st.get(uri));
        st.put(new ByteArrayInputStream(doc2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        testList.add(st.get(uri2));
        st.setMaxDocumentCount(2);
        st.put(new ByteArrayInputStream(doc3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        testList.add(st.get(uri3));
        for(Document doc : st.searchByPrefix("doc")){
            assertTrue(keywords.contains(doc.getDocumentTxt()));
        }
        //At the end of this, Doc2 should be in the disk, maintaining structure.
        st.put(new ByteArrayInputStream(doc3Replace.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        assertEquals(doc3Replace, st.get(uri2).getDocumentTxt());
        st.get(uri);
        st.get(uri3);

    }

    @AfterEach
    void resetDirectories() throws IOException{
        System.out.println("-------------------- CLEANUP BEGIN ---------------");
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc3");

        deleteFileForURI(uri);
        deleteFileForURI(uri2);
        deleteFileForURI(uri3);

        deleteDirectories();
        
    }

    private void deleteDirectories() throws IOException {
        Path dirPath = Paths.get(System.getProperty("user.dir"),"piazza.com");
        if (Files.exists(dirPath)) {
            System.out.println("hello");
            Files.walk(dirPath)
                 .sorted((p1, p2) -> -p1.compareTo(p2))
                 .forEach(path -> {
                     try {
                         if (Files.isDirectory(path) && Files.list(path).findAny().isEmpty()) {
                             Files.delete(path);
                         }
                     } catch (IOException e) {
                         throw new RuntimeException();
                     }
                 });
        }
    }

    @Test
    void pushToDiskViaMaxDocCountBringBackInViaMetadataSearch() throws IOException{
        DocumentStore st = new DocumentStoreImpl();
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc3");
        Map<String, String> hm = new HashMap<>();
        hm.put("Author", "myself");
        st.put(new ByteArrayInputStream("doc1".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        assertNull(st.setMetadata(uri, "Author", "myself"));
        st.put(new ByteArrayInputStream("doc2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        assertNull(st.setMetadata(uri2, "Author", "herself"));
        st.put(new ByteArrayInputStream("doc3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        assertNull(st.setMetadata(uri3, "Author", "himself"));
        System.out.println("Insert done, doc1 last used");
        st.setMaxDocumentCount(2);
        assertEquals(1, st.searchByMetadata(hm).size());
    }

    @Test
    void undoMetadataOneDocumentThenSearch() throws IOException{
        DocumentStore st = new DocumentStoreImpl();
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        Set targets = Set.of(uri, uri2);
        Map<String, String> hm = new HashMap<>();
        hm.put("Author", "myself");
        st.put(new ByteArrayInputStream("doc1".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        assertNull(st.setMetadata(uri, "Author", "myself"));
        st.put(new ByteArrayInputStream("doc2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        assertNull(st.setMetadata(uri2, "Author", "myself"));
        assertEquals(targets, st.deleteAllWithMetadata(hm));
        st.undo();
        assertEquals(2, st.searchByMetadata(hm).size());

    }

    @Test
    void pushToDiskViaMaxDocCountViaUndoDelete() throws IOException{
        DocumentStore st = new DocumentStoreImpl();
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc3");
        st.put(new ByteArrayInputStream("doc1".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        st.put(new ByteArrayInputStream("doc2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        st.put(new ByteArrayInputStream("doc3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        st.setMaxDocumentCount(2);
        System.out.println("Doc1 should be in");
        assertTrue(st.delete(uri3));
        System.out.println("doc1 should be still in");
        st.undo();
        System.out.println("doc1 should be still in, doc3 in mem");


    }

    @Test
    void pushToDiskViaMaxDocCountBringBackInViaDeleteAndSearch() throws IOException{
        DocumentStore st = new DocumentStoreImpl();
        URI uri = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc1");
        URI uri2 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc2");
        URI uri3 = URI.create("https://piazza.com/class/m5x1dbt22h175i/post/doc3");
        st.setMaxDocumentCount(2);
        assertEquals(0, st.put(new ByteArrayInputStream("doc1".getBytes()), uri, DocumentStore.DocumentFormat.TXT));
        assertEquals(0, st.put(new ByteArrayInputStream("doc2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT));
        assertEquals(0, st.put(new ByteArrayInputStream("doc3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT));
        System.out.println("confirm doc1 in");
        assertTrue(st.delete(uri2));
        assertEquals(1, st.search("doc1").size());
        System.out.println("confirm doc1 out");
    }

    private void deleteFileForURI(URI uri) throws IOException {
        String authority = uri.getAuthority();
        String path = uri.getPath();
        Path filePath = Paths.get(System.getProperty("user.dir"), authority, path + ".json");
        Files.deleteIfExists(filePath);
    }
}