# PersistentDocuStore

PersistentDocuStore is a lightweight document storage library designed for reliability and ease of use. It provides a `DocumentStore` object for storing, retrieving, and managing documents with persistent storage.

This project reflects some of the data structures learnt in my Data Structures Course.

The following data structures were implemented in the project:
- B-Tree
- Trie
- Stack
- MinHeap


## Features

- Persistent document storage
- Easy document retrieval
- Simple API for integration
- Undo features for document manipulation
- Search and Search-by-prefix feature


## Getting Started

### Dependancies

- Java 17 or above
- GSON v2.9.0
- JUnit
- Jakarta

### Installation

Clone the repository:

```bash
git clone https://github.com/yourusername/PersistentDocuStore.git
cd PersistentDocuStore
```

### Usage

Import and use the `DocumentStore` object in your code:

```java
import project.docstore.*;
import java.io.*;
import java.net.URI;

// Initialize with base directory for persistence
DocumentStore store = new DocumentStoreImpl(new File("path/to/storage"));

// Add a document
String content = "Hello, world!";
URI uri = new URI("doc1");
InputStream stream = new ByteArrayInputStream(content.getBytes());
store.put(stream, uri, DocumentStore.DocumentFormat.TXT);

// Retrieve a document
Document doc = store.get(uri);

// Search for documents
List<Document> results = store.search("hello");

// Delete a document
store.delete(uri);

// Undo document delete
store.undo()
```

## API Reference

| Method                                    | Description                                                |
|-----------------------------------------|----------------------------------------------------------|
| `put(InputStream, URI, DocumentFormat)`  | Add or update a document from an input stream             |
| `get(URI)`                              | Get a document by URI                                     |
| `delete(URI)`                           | Delete a document by URI                                  |
| `search(String)`                        | Search for documents containing the keyword               |
| `searchByPrefix(String)`                | Search for documents with words starting with prefix      |
| `searchByMetadata(Map<String,String>)`  | Search for documents matching metadata key-value pairs    |
| `setMetadata(URI, String, String)`      | Set metadata value for a document                        |
| `getMetadata(URI, String)`              | Get metadata value from a document                        |
| `deleteAll(String)`                     | Delete all documents containing the keyword               |
| `deleteAllWithPrefix(String)`           | Delete all documents with words starting with prefix      |
| `deleteAllWithMetadata(Map)`            | Delete all documents matching metadata criteria           |
| `undo()`                                | Undo the last operation                                   |
| `undo(URI)`                             | Undo the last operation on the specified document         |
| `setMaxDocumentCount(int)`              | Set maximum number of documents to keep in memory         |
| `setMaxDocumentBytes(int)`              | Set maximum bytes of documents to keep in memory          |