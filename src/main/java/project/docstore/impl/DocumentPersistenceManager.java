package project.docstore.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import project.docstore.Document;
import project.docstore.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File baseDir;
    private GsonBuilder gsonBuilder;
    private Gson gson;
    public DocumentPersistenceManager(File dir){
        this.baseDir = dir == null ? Paths.get(System.getProperty("user.dir")).toFile() : dir;
        this.gsonBuilder = new GsonBuilder();
        this.gsonBuilder.registerTypeAdapter(Document.class, new DocumentSerializer());
        this.gsonBuilder.registerTypeAdapter(Document.class, new DocumentDeserializer());
        this.gson = gsonBuilder.create();
    }

    @Override
    public void serialize(URI key, Document val) throws IOException {
        // Java has a method in File to create a file, You need a string for the filepath
        // URI could have https://, need to remove before. then rest is kosher, after adding .json to end
        // getHost to get string without the slashes, but also without https://
        // stringbuilder?
        Path newPath = this.buildPath(key);
        if(Files.exists(newPath)){
            Files.writeString(newPath, this.gson.toJson(val, Document.class));
        } else{
            Files.createDirectories(newPath.getParent());
            Files.createFile(newPath);
            Files.writeString(newPath, this.gson.toJson(val, Document.class));
        }

    }


    @Override
    public Document deserialize(URI key) throws IOException {
        //Construct the path
        Path fullPath = this.buildPath(key);
        if(!Files.exists(fullPath)){throw new IOException("json file for respective URI does not exist, is it in memory?");}
        String contents = Files.readString(fullPath);
        return gson.fromJson(contents, Document.class);
    }

    private Path buildPath(URI key){
        StringBuilder concat = new StringBuilder();
        concat.append(key.getAuthority());
        concat.append(key.getPath());
        concat.append(".json");
        String pathString = concat.toString();
        // make the full path, from the base directory up until the one where teh file will be
        Path newPath = Paths.get(baseDir.getAbsolutePath(), pathString);
        return newPath;
    }

    @Override
    public boolean delete(URI key) throws IOException {
        //If path exists, delete, if not then just return false
        Path fullPath = this.buildPath(key);
        if(Files.exists(fullPath)){
            Files.delete(fullPath);
            //System.out.println(key.toString() + " deleted");
            return true;
        }
        return false;
    }

    private class DocumentSerializer implements JsonSerializer<Document> {

        @Override
        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            // need to start with a json object, then as I go, add each of the four things
            //metadata, uri, if there is text, then text and wordmap, if not, add bin data.. encoded?
            JsonObject concat = new JsonObject();
            concat.add("URI", context.serialize(src.getKey()));
            concat.add("Metadata", context.serialize(src.getMetadata()));
            // if there is text, add both text and the wordMap
            // if there isnt, just add encoded byteArray
            if(src.getDocumentTxt() != null){
                concat.add("text", context.serialize(src.getDocumentTxt()));
                concat.add("WordMap", context.serialize(src.getWordMap()));
            } else{
                concat.addProperty("BinaryData", DatatypeConverter.printBase64Binary(src.getDocumentBinaryData()));
            }
            return concat;
        }
    }

    private class DocumentDeserializer implements JsonDeserializer<Document> {
        @Override
        public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            // access each part of the json, then create object, add metaData using mutator, then return.. Make sure to check links in Doc for byte[] decode
            JsonObject entryJson = json.getAsJsonObject();
            URI key = URI.create(entryJson.get("URI").getAsString());
            HashMap<String, String> metadata = context.deserialize(entryJson.get("Metadata"), HashMap.class);
            if(entryJson.has("text")){
                String text = entryJson.get("text").getAsString();
                Map<String, Integer> wordMap = context.deserialize(entryJson.get("WordMap"), HashMap.class);
                DocumentImpl myDoc = new DocumentImpl(key, text, wordMap);
                myDoc.setMetadata(metadata);
                return myDoc;
            } else{
                byte[] binData = DatatypeConverter.parseBase64Binary(entryJson.get("BinaryData").getAsString());
                DocumentImpl myDoc = new DocumentImpl(key, binData);
                myDoc.setMetadata(metadata);
                myDoc.setLastUseTime(System.nanoTime());
                return myDoc;
            }
        }
    }
}
