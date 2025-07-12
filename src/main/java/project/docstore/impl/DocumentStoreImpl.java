package project.docstore.impl;

import project.docstore.Document;
import project.docstore.DocumentStore;
import project.impl.BTreeImpl;
import project.impl.MinHeapImpl;
import project.impl.StackImpl;
import project.impl.TrieImpl;
import project.undo.*;
import project.undo.CommandSet;
import project.undo.GenericCommand;
import project.undo.Undoable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.io.File;

public class DocumentStoreImpl implements DocumentStore {
    private BTreeImpl<URI,Document> documentMap;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<URI> wordTree;
    private MinHeapImpl<URI> docHeap;
    private int maxDocumentCount;
    private int currDocumentCount;
    private int maxDocumentBytes;
    private int currDocumentBytes;
    private Set<URI> docsInMemory;
    private Set<URI> docInMemoryAndDisk;

    public DocumentStoreImpl(){
        this.documentMap = new BTreeImpl<>();
        this.documentMap.setPersistenceManager(new DocumentPersistenceManager(null));
        this.commandStack = new StackImpl<>();
        this.wordTree = new TrieImpl<>();
        this.docHeap = new MinHeapImpl<>(){
            @Override
            protected boolean isGreater(int i, int j) {
                Comparable[] urisInHeap = super.elements;
                Document doc1 = documentMap.get((URI) urisInHeap[i]);
                Document doc2 = documentMap.get((URI) urisInHeap[j]);
                if(doc1 == null || doc2 == null){return true;}
                return doc1.compareTo(doc2) == 1;

            }
        };
        this.currDocumentBytes = 0;
        this.currDocumentCount = 0;
        this.maxDocumentBytes = Integer.MAX_VALUE;
        this.maxDocumentCount = Integer.MAX_VALUE;
        this.docsInMemory = new HashSet<>();
        this.docInMemoryAndDisk = new HashSet<>();
    }
    public DocumentStoreImpl(File baseDir){
        this.documentMap = new BTreeImpl<>();
        this.documentMap.setPersistenceManager(new DocumentPersistenceManager(baseDir));
        this.commandStack = new StackImpl<>();
        this.wordTree = new TrieImpl<>();
        this.docHeap = new MinHeapImpl<>(){
            @Override
            protected boolean isGreater(int i, int j) {
                Comparable[] urisInHeap = super.elements;
                Document doc1 = documentMap.get((URI) urisInHeap[i]);
                Document doc2 = documentMap.get((URI) urisInHeap[j]);
                return doc1.compareTo(doc2) == 1;

            }
        };
        this.currDocumentBytes = 0;
        this.currDocumentCount = 0;
        this.maxDocumentBytes = Integer.MAX_VALUE;
        this.maxDocumentCount = Integer.MAX_VALUE;
        this.docsInMemory = new HashSet<>();
        this.docInMemoryAndDisk = new HashSet<>();
    }
    @Override
    public String setMetadata(URI uri, String key, String value) throws IOException {
        if(uri == null){
            throw new IllegalArgumentException("URI is null");
        } else if(key == null || key.isBlank()){
            throw new IllegalArgumentException("invalid key");
        }
        Document myDoc = this.get(uri);
        if(myDoc == null){
            throw new IllegalArgumentException("No document stored at" + uri);
        }
        String oldVal = myDoc.setMetadataValue(key, value);
        this.commandStack.push(new GenericCommand(uri, undo -> myDoc.setMetadataValue(key, oldVal)));
        return oldVal;
    }

    @Override
    public String getMetadata(URI uri, String key) throws IOException {
        if(uri == null){
            throw new IllegalArgumentException("URI is null");
        } else if(key == null || key.isBlank()){
            throw new IllegalArgumentException("invalid key");
        }
        Document myDoc = this.get(uri);
        if(myDoc == null){
            throw new IllegalArgumentException("No document stored at" + uri);
        }
        return myDoc.getMetadataValue(key);

    }

    @Override
    public int put(InputStream input, URI url, DocumentFormat format) throws IOException {
        if(input == null){
            Document currentValue=(Document) this.documentMap.get(url);return delete(url) ? currentValue.hashCode() : 0;
        } else{
            try{byte[] contents = input.readAllBytes();
                if(url == null){throw new IllegalArgumentException("URI invalid");}
                if(format == null){throw new IllegalArgumentException("format is null");}
                Document currentValue = (Document) get(url);
                if(currentValue == null){
                    DocumentImpl newDoc = format == DocumentFormat.BINARY ? new DocumentImpl(url, contents) : new DocumentImpl(url, new String(contents), null);
                    this.documentMap.put(url, newDoc);
                    if(format == DocumentFormat.TXT){
                        this.loadTrie(url);
                        this.commandStack.push(new GenericCommand(url, a -> {this.deleteDocFromTree(url); this.documentMap.put(url, null); deleteDocFromHeapAndUpdateCount(newDoc);}));
                    } else{this.commandStack.push(new GenericCommand(url, a -> {this.documentMap.put(url, null); deleteDocFromHeapAndUpdateCount(newDoc);}));}
                    insertDocToHeapUpdateCount(newDoc);
                    return 0;
                } else{
                    DocumentImpl newDoc = format == DocumentFormat.BINARY ? new DocumentImpl(url, contents) : new DocumentImpl(url, new String(contents), null);
                    if(format == DocumentFormat.TXT){
                        if(currentValue.getDocumentTxt() != null){this.deleteDocFromTree(url);}
                        this.documentMap.put(url, newDoc);
                        this.loadTrie(url);
                        //System.out.println("URI before " + currentValue.getKey().toString());
                        //System.out.println("URI after " + url);
                        deleteDocFromHeapAndUpdateCount(currentValue, newDoc);
                        this.commandStack.push(new GenericCommand(url, a -> {this.deleteDocFromTree(url); this.documentMap.put(url,currentValue); if(currentValue.getDocumentTxt() != null){this.loadTrie(currentValue.getKey());}
                            deleteDocFromHeapAndUpdateCount(newDoc, currentValue);}));
                    } else{this.documentMap.put(url, newDoc); this.commandStack.push(new GenericCommand(url, a -> {this.documentMap.put(url,currentValue); deleteDocFromHeapAndUpdateCount(newDoc, currentValue);}));}
                    return currentValue.hashCode();
                }
            } catch (IOException e) {throw new IOException(e);}
        }

    }

    private void loadTrie(URI url){
        Document doc = (Document) this.documentMap.get(url);
        if(doc == null || doc.getDocumentTxt() == null){return;}
        for(String word : doc.getWords()){
            this.wordTree.put(word, doc.getKey());
        }
    }

    private void deleteDocFromTree(URI url){
        Document doc = (Document) this.documentMap.get(url);
        if(doc == null || doc.getDocumentTxt() == null){return;}
        for(String word: doc.getWords()){
            this.wordTree.delete(word, doc.getKey());
        }
    }

    private void updateDocUsageTime(Document doc){
        doc.setLastUseTime(System.nanoTime());
        this.docHeap.reHeapify(doc.getKey());
    }

    private void prepareDocTimeToDelete(Document doc){
        doc.setLastUseTime(Long.MIN_VALUE);
        this.docHeap.reHeapify(doc.getKey());
    }

    private void deleteDocFromHeapAndUpdateCount(Document doc){
        if(this.docsInMemory.contains(doc.getKey())){
            prepareDocTimeToDelete(doc);
            this.docHeap.reHeapify(doc.getKey());
            this.docHeap.remove();
            this.currDocumentCount--;
            this.currDocumentBytes -= getDocumentBytes(doc);
            this.docsInMemory.remove(doc.getKey());
        }
        this.docInMemoryAndDisk.remove(doc.getKey());
        reinforceLimits();
    }

    private void deleteDocFromHeapAndUpdateCount(Document docOld, Document docNew){
        prepareDocTimeToDelete(docOld);
        this.docHeap.reHeapify(docOld.getKey());
        this.docHeap.remove();
        this.docInMemoryAndDisk.remove(docOld.getKey());
        this.docsInMemory.remove(docOld.getKey());
        updateDocUsageTime(docNew);
        this.docHeap.insert(docNew.getKey());
        this.currDocumentBytes -= getDocumentBytes(docOld);
        this.currDocumentBytes += getDocumentBytes(docNew);
        this.docInMemoryAndDisk.add(docNew.getKey());
        this.docsInMemory.add(docNew.getKey());
        reinforceLimits();
    }

    private void insertDocToHeapUpdateCount(Document newDoc){
        this.currDocumentCount++;
        this.currDocumentBytes += getDocumentBytes(newDoc);
        updateDocUsageTime(newDoc);
        //System.out.println(newDoc.getDocumentTxt() + " heapmagic");
        this.docHeap.insert(newDoc.getKey());
        this.docInMemoryAndDisk.add(newDoc.getKey());
        this.docsInMemory.add(newDoc.getKey());
        reinforceLimits();
    }

    private void reinforceLimits(){
        while(currDocumentCount > maxDocumentCount || currDocumentBytes > maxDocumentBytes){
            URI uriOfDocToRemove = this.docHeap.remove();
            Document docToRemove = this.documentMap.get(uriOfDocToRemove);
            this.removeAllDocTrace(docToRemove);
        }
    }

    private void removeAllDocTrace(Document doc){
        URI url = doc.getKey();
        //this.removeDocFromUndo(url);
        //this.deleteDocFromTree(url);
        try{this.documentMap.moveToDisk(url);
            //System.out.println(doc.getDocumentTxt() + " moved to disk");
            this.currDocumentCount--;
            this.currDocumentBytes -= getDocumentBytes(doc);
            this.docsInMemory.remove(url);
        }
        catch(Exception IOException){throw new RuntimeException("IO error");}

    }

    private int getDocumentBytes(Document doc){
        if(doc.getDocumentTxt() != null){
            return doc.getDocumentTxt().getBytes().length;
        } else{
            return doc.getDocumentBinaryData().length;
        }
    }

    private void removeDocFromUndo(URI url){
        StackImpl<Undoable> tempStack = new StackImpl<>();
        while(this.commandStack.size() > 0){
            Undoable currCommand = this.commandStack.pop();
            if(currCommand instanceof GenericCommand<?>){
                GenericCommand<URI> command = (GenericCommand<URI>) currCommand;
                if(!command.getTarget().equals(url)){
                    tempStack.push(command);
                }
            } else if(currCommand instanceof CommandSet<?>){
                CommandSet<URI> commandSet = (CommandSet<URI>) currCommand;
                Iterator<GenericCommand<URI>> iterator = commandSet.iterator();
                while(iterator.hasNext()){
                    GenericCommand<URI> currCmd = iterator.next();
                    if(currCmd.getTarget().equals(url)){iterator.remove();}
                }
                if(!commandSet.isEmpty()){tempStack.push(commandSet);}
            }
        }
        //this.commandStack is now empty, fill it back in with temp
        while(tempStack.size() != 0){
            this.commandStack.push(tempStack.pop());
        }
    }

    @Override
    public Document get(URI url) throws IOException {
        Document doc = (Document) this.documentMap.get(url);
        if(doc != null){
            updateDocUsageTime(doc);
            if(!this.docsInMemory.contains(url)){
                docsInMemory.add(url);
                this.docHeap.insert(url);
                this.docHeap.reHeapify(url);
                this.currDocumentCount++;
                this.currDocumentBytes += getDocumentBytes(doc);
                reinforceLimits();

            }
        }
        return doc;
    }

    private void updateForIncomingDoc(Document doc){
        URI url = doc.getKey();
        this.docsInMemory.add(url);
        this.docHeap.insert(url);
        this.docHeap.reHeapify(url);
        this.currDocumentCount++;
        this.currDocumentBytes += getDocumentBytes(doc);
    }

    private void updateForUndoneDoc(Document doc){
        URI url = doc.getKey();
        this.docsInMemory.add(url);
        this.docInMemoryAndDisk.add(url);
        this.docHeap.insert(url);
        this.docHeap.reHeapify(url);
        this.currDocumentCount++;
        this.currDocumentBytes += getDocumentBytes(doc);
    }

    @Override
    public boolean delete(URI url) {
        try{Document myDoc = this.get(url);
            if(myDoc == null){
                return false;
            }else{
                //Delete the things from the Trie
                this.deleteDocFromTree(myDoc.getKey());
                this.documentMap.put(url, null);
                deleteDocFromHeapAndUpdateCount(myDoc);
                this.commandStack.push(new GenericCommand(myDoc.getKey(), a -> {this.documentMap.put(myDoc.getKey(), myDoc);
                    if(myDoc.getDocumentTxt() != null){this.loadTrie(myDoc.getKey());} updateForUndoneDoc(myDoc); reinforceLimits();}));
                return true;
            }
        }
        catch(Exception IOException){throw new RuntimeException();}

    }

    @Override
    public void undo() throws IllegalStateException {
        if(this.commandStack.size() == 0){throw new IllegalStateException();}
        if(this.commandStack.peek() instanceof GenericCommand){this.commandStack.pop().undo();}
        else if(this.commandStack.peek() instanceof CommandSet){
            CommandSet<URI> commands = (CommandSet) this.commandStack.pop();
            commands.undoAll();
        }

    }

    @Override
    public void undo(URI url) throws IllegalStateException {
        if(this.commandStack.size() == 0){throw new IllegalStateException();}
        StackImpl<Undoable> auxStack = new StackImpl<>();
        boolean found = false;
        if(this.commandStack.peek() instanceof GenericCommand){
            while(this.commandStack.size() > 0 && !found){
                if(((GenericCommand) this.commandStack.peek()).getTarget().equals(url)){
                    this.commandStack.pop().undo();
                    found = true;
                }else{auxStack.push(this.commandStack.pop());}
            } while(auxStack.size() > 0){
                this.commandStack.push(auxStack.pop());
            } if(found == false){throw new IllegalStateException();}
        } else if(this.commandStack.peek() instanceof CommandSet){
            while(this.commandStack.size() > 0 && !found){
                CommandSet<URI> potential = (CommandSet<URI>) this.commandStack.peek();
                if(potential.containsTarget(url)){
                    this.commandStack.pop();
                    potential.undo(url);
                    found = true;
                }else{auxStack.push(this.commandStack.pop());}
            } while(auxStack.size() > 0){
                this.commandStack.push(auxStack.pop());
            } if(found == false){throw new IllegalStateException();}
        }

    }

    @Override
    public List<Document> search(String keyword) throws IOException {
        List<URI> targetsURI = this.wordTree.getSorted(keyword, new Comparator<URI>() {
            @Override
            public int compare(URI o1, URI o2) {
                try {
                    Document o1Doc = documentMap.get(o1);
                    if(!docsInMemory.contains(o1Doc.getKey())){
                        updateForIncomingDoc(o1Doc);
                    }
                    Document o2Doc = documentMap.get(o2);
                    if(!docsInMemory.contains(o2Doc.getKey())){
                        updateForIncomingDoc(o2Doc);
                    }
                    return o2Doc.wordCount(keyword) - o1Doc.wordCount(keyword);
                } catch (Exception IOException) {
                    throw new RuntimeException();
                }
            }
        });
        List<Document> targets = new ArrayList<>();
        for(URI docURI : targetsURI){targets.add(get(docURI));}

        for(Document doc : targets){iterateAndUpdateDocTimes(doc);}
        reinforceLimits();
        return targets;

    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException {
        List<URI> targetsURI = this.wordTree.getAllWithPrefixSorted(keywordPrefix, new Comparator<URI>() {
            @Override
            public int compare(URI o1, URI o2) {
                try{
                    Document o1Doc = documentMap.get(o1);
                    if(!docsInMemory.contains(o1Doc.getKey())){
                        updateForIncomingDoc(o1Doc);
                    }
                    Document o2Doc = documentMap.get(o2);
                    if(!docsInMemory.contains(o2Doc.getKey())){
                        updateForIncomingDoc(o2Doc);
                    }
                    int o1Count = 0;
                    int o2Count = 0;
                    for(String word : o1Doc.getWords()){
                        if(word.startsWith(keywordPrefix)){o1Count+=o1Doc.wordCount(word);}
                    }
                    for(String word : o2Doc.getWords()){
                        if(word.startsWith(keywordPrefix)){o2Count+=o2Doc.wordCount(word);}
                    }
                    return o2Count-o1Count;
                }catch(Exception IOException){return -1;}

            }
        });
        List<Document> targets = new ArrayList<>();
        for(URI docURI : targetsURI){targets.add(get(docURI));}
        for(Document doc : targets){iterateAndUpdateDocTimes(doc);}
        reinforceLimits();
        return targets;
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<URI> docToDeleteURI = this.wordTree.deleteAll(keyword);
        Set<Document> targets = new HashSet<>();
        for(URI docURI : docToDeleteURI){targets.add(this.documentMap.get(docURI));}
        Set<URI> uriSet = new HashSet<>();
        CommandSet<URI> cmdSet = new CommandSet<>();
        for(Document doc : targets){
            cmdSet.addCommand(new GenericCommand<>(doc.getKey(), a -> {this.documentMap.put(doc.getKey(), doc);
                if(doc.getDocumentTxt() != null){this.loadTrie(doc.getKey());} updateForUndoneDoc(doc); reinforceLimits();}));
            uriSet.add(doc.getKey());
            this.documentMap.put(doc.getKey(), null);
            deleteDocFromHeapAndUpdateCount(doc);
        } this.commandStack.push(cmdSet);
        reinforceLimits();
        return uriSet;

    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI> docsToDeleteURI = this.wordTree.deleteAllWithPrefix(keywordPrefix);
        Set<Document> targets = new HashSet<>();
        for(URI docURI : docsToDeleteURI){targets.add(this.documentMap.get(docURI));}
        Set<URI> uriSet = new HashSet<>();
        CommandSet<URI> cmdSet = new CommandSet<>();
        for(Document doc : targets){
            cmdSet.addCommand(new GenericCommand<>(doc.getKey(), a -> {this.documentMap.put(doc.getKey(), doc);
                if(doc.getDocumentTxt() != null){this.loadTrie(doc.getKey());} updateForUndoneDoc(doc); reinforceLimits();}));
            uriSet.add(doc.getKey());
            this.documentMap.put(doc.getKey(), null);
            deleteDocFromHeapAndUpdateCount(doc);
        } this.commandStack.push(cmdSet);
        reinforceLimits();
        return uriSet;

    }
    //TODO: REPLACE ALL LOGIC FOR GETS IN THE SEARCHES TO BTREE GET, HANDLE MM using UPDATEFORINCOMING DOC, REPLACE UPDATE TIME LOOP
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) throws IOException {
        ArrayList<Document> myList = new ArrayList<>();
        for(URI doc: this.docInMemoryAndDisk){
            Document myDoc = this.documentMap.get(doc);
            if(!docsInMemory.contains(myDoc.getKey())){
                updateForIncomingDoc(myDoc);
            }
            if(myDoc.getMetadata().keySet().equals(keysValues.keySet())){
                boolean mismatch = false;
                for(String key : keysValues.keySet()){
                    if(!keysValues.get(key).equals(myDoc.getMetadataValue(key))){mismatch = true; break;}
                } if(!mismatch){myList.add((Document) myDoc);}
            }
        } // FIX THIS IF NOT WORKING, MAKE 1 LINE SOLUTION
        for(Document doc : myList){iterateAndUpdateDocTimes(doc);}
        reinforceLimits();
        return myList;
    }

    private void iterateAndUpdateDocTimes(Document doc){
        if(docsInMemory.contains(doc.getKey())) updateDocUsageTime(doc);
    }

    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        List<Document> resultKeyword = this.search(keyword);
        for(Document doc : resultKeyword){
            if(doc.getMetadata().keySet().equals(keysValues.keySet())){
                for(String key : doc.getMetadata().keySet()){
                    if(!doc.getMetadataValue(key).equals(keysValues.get(key))){resultKeyword.remove(doc); break;}
                }
            }
        }
        for(Document doc : resultKeyword){iterateAndUpdateDocTimes(doc);}
        reinforceLimits();
        return resultKeyword;

    }

    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
        List<Document> resultKeyword = this.searchByPrefix(keywordPrefix);
        List<Document> realResult = new ArrayList<>();
        for(Document doc : resultKeyword){
            boolean areGood = true;
            if(doc.getMetadata().keySet().equals(keysValues.keySet())){
                for(String key : doc.getMetadata().keySet()){
                    if(!doc.getMetadataValue(key).equals(keysValues.get(key))){areGood = false; break;}
                } if(areGood){realResult.add(doc);}
            }
        }
        for(Document doc : realResult){iterateAndUpdateDocTimes(doc);}
        reinforceLimits();
        return realResult;
    }

    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) throws IOException {
        List<Document> docsToDelete = this.searchByMetadata(keysValues);
        Set<URI> uriSet = new HashSet<>();
        CommandSet<URI> cmdSet = new CommandSet<>();
        for(Document doc : docsToDelete){
            uriSet.add(doc.getKey());
            for(String word : doc.getWords()){this.wordTree.delete(word, doc.getKey());}
            this.documentMap.put(doc.getKey(), null);
            deleteDocFromHeapAndUpdateCount(doc);
            cmdSet.addCommand(new GenericCommand<>(doc.getKey(), a -> {this.documentMap.put(doc.getKey(), doc);
                if(doc.getDocumentTxt() != null){this.loadTrie(doc.getKey());} updateForUndoneDoc(doc); reinforceLimits();}));
        } this.commandStack.push(cmdSet);
        reinforceLimits();
        return uriSet;
    }

    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        List<Document> docsToDelete = this.searchByKeywordAndMetadata(keyword, keysValues);
        Set<URI> uriSet = new HashSet<>();
        CommandSet<URI> cmdSet = new CommandSet<>();
        for(Document doc : docsToDelete){
            uriSet.add(doc.getKey());
            for(String word : doc.getWords()){this.wordTree.delete(word, doc.getKey());}
            this.documentMap.put(doc.getKey(), null);
            deleteDocFromHeapAndUpdateCount(doc);
            cmdSet.addCommand(new GenericCommand<>(doc.getKey(), a -> {this.documentMap.put(doc.getKey(), doc);
                if(doc.getDocumentTxt() != null){this.loadTrie(doc.getKey());} updateForUndoneDoc(doc); reinforceLimits();}));
        } this.commandStack.push(cmdSet);
        reinforceLimits();
        return uriSet;
    }

    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
        List<Document> docsToDelete = this.searchByPrefixAndMetadata(keywordPrefix, keysValues);
        Set<URI> uriSet = new HashSet<>();
        CommandSet<URI> cmdSet = new CommandSet<>();
        for(Document doc : docsToDelete){
            uriSet.add(doc.getKey());
            for(String word : doc.getWords()){this.wordTree.delete(word, doc.getKey());}
            this.documentMap.put(doc.getKey(), null);
            deleteDocFromHeapAndUpdateCount(doc);
            cmdSet.addCommand(new GenericCommand<>(doc.getKey(), a -> {this.documentMap.put(doc.getKey(), doc);
                if(doc.getDocumentTxt() != null){this.loadTrie(doc.getKey());} updateForUndoneDoc(doc); reinforceLimits();}));
        } this.commandStack.push(cmdSet);
        reinforceLimits();
        return uriSet;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        this.maxDocumentCount = limit;
        reinforceLimits();
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        this.maxDocumentBytes = limit;
        reinforceLimits();
    }
}
