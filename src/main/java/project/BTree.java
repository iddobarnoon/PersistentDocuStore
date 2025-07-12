package project;

import java.io.IOException;

import project.docstore.PersistenceManager;

public interface BTree<Key extends Comparable<Key>, Value> {
    Value get(Key k);
    Value put(Key k, Value v);
    void moveToDisk(Key k) throws IOException;
    void setPersistenceManager(PersistenceManager<Key,Value> pm);
}