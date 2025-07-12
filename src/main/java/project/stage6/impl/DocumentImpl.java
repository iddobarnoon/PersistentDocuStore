package project.stage6.impl;

import project.stage6.Document;

import java.net.URI;
import java.util.HashMap;
import java.util.Set;
import java.util.*;

public class DocumentImpl implements Document {
    private URI uri;
    private String text;
    private byte[] binaryData;
    private Map<String, String> metadata;
    private Map<String, Integer> wordCounts;
    private long lastTimeUsed;

    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap){
        if(uri == null){
            throw new IllegalArgumentException("URI is null");
        } else if(text == null){
            throw new IllegalArgumentException("txt or BinaryData is null");
        }
        this.uri = uri;
        this.text = text;
        this.binaryData = null;
        this.metadata = new HashMap<String, String>();
        String noSymbolString = this.text.replaceAll("[^a-zA-Z0-9 ]", "");
        String[] splitText = noSymbolString.split("\\s+");
        this.wordCounts = wordCountMap == null ? constructWordCounts(splitText) : wordCountMap;
        this.lastTimeUsed = Long.MAX_VALUE;

    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if(uri == null){
            throw new IllegalArgumentException("URI is null");
        } else if(binaryData == null){
            throw new IllegalArgumentException("txt or BinaryData is null");
        }
        this.uri = uri;
        this.binaryData = binaryData;
        this.text = null;
        this.metadata = new HashMap<String, String>();
    }

    private HashMap<String, Integer> constructWordCounts(String[] splitWords){
        HashMap<String, Integer> wordCounts = new HashMap<>();
        for(String word : splitWords){
            if(wordCounts.get(word) != null){wordCounts.put(word, wordCounts.get(word)+1);}
            else{wordCounts.put(word, 1);}
        } return wordCounts;
    }


    @Override
    public String setMetadataValue(String key, String value) {
        if(key == null){
            throw new IllegalArgumentException("key is null");
        } if(key.isBlank()){
            throw new IllegalArgumentException("key is an empty string");
        }
        String oldValue = this.getMetadataValue(key);
        this.metadata.put(key, value);
        return oldValue;

    }

    @Override
    public String getMetadataValue(String key) {
        if(key == null){
            throw new IllegalArgumentException("key is null");
        } if(key.isBlank()){
            throw new IllegalArgumentException("key is an empty string");
        }
        return (String) this.metadata.get(key);
    }

    @Override
    public HashMap<String, String> getMetadata() {
        HashMap<String, String> metadataCopy = new HashMap<>();
        for(String key : this.metadata.keySet()){
            metadataCopy.put(key,this.metadata.get(key));
        }
        return metadataCopy;
    }

    @Override
    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getDocumentTxt() {
        return this.text;
    }

    @Override
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    @Override
    public URI getKey() {
        return this.uri;
    }

    @Override
    public int wordCount(String word) {
        if(this.text==null){return 0;}
        Integer result = this.wordCounts.get(word);
        return result != null ? result : 0;
    }

    @Override
    public Set<String> getWords() {
        return this.wordCounts.keySet();
    }

    @Override
    public long getLastUseTime() {
        return lastTimeUsed;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastTimeUsed = timeInNanoseconds;
    }

    @Override
    public HashMap<String, Integer> getWordMap() {
        return new HashMap(this.wordCounts);
    }

    @Override
    public void setWordMap(HashMap<String, Integer> wordMap) {
        this.wordCounts = wordMap;
    }

    @Override
    public int compareTo(Document o) {
        return Long.compare(this.getLastUseTime(), o.getLastUseTime());
    }
    @Override
    public boolean equals(Object other){
        if(other == null){
            return false;
        } else if(other.getClass() != this.getClass()){
            return false;
        }
        DocumentImpl otherDoc = (DocumentImpl) other;
        return this.hashCode() == otherDoc.hashCode();
    }

    // if there is a problem with this, try to see if removing this would work
    @Override
    public int hashCode(){
        int result = this.uri.hashCode();
        result = 31 * result + (this.text != null ? this.text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(this.binaryData);
        return Math.abs(result);
    }

}
