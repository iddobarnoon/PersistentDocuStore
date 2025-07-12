package project.impl;

import project.Trie;

import java.util.Comparator;
import java.util.List;
import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 128;
    private Node<Value> root;

    private static class Node<Value>{
        private HashSet<Value> val;
        private Node<Value>[] links;
        public Node(){
            this.val = new HashSet<>();
            this.links = new Node[TrieImpl.alphabetSize];
        }
    }

    public TrieImpl(){
        this.root = null;
    }


    @Override
    public void put(String key, Value val) {
        if(val == null){this.deleteAll(key);}
        else {this.root = put(this.root, key, val, 0);}
    }

    private Node<Value> put(Node<Value> nodeCurr, String key, Value val, int x){
        if(nodeCurr == null){
            nodeCurr = new Node<>();
        } if(x == key.length()){
            nodeCurr.val.add(val);
            return nodeCurr;
        } else{
            char c = key.charAt(x);
            nodeCurr.links[c] = this.put(nodeCurr.links[c], key, val, x+1);
            return nodeCurr;
        }
    }


    @Override
    public List<Value> getSorted(String key, Comparator<Value> comparator) {
        Set<Value> targetSet = this.get(key);
        if(targetSet.isEmpty()){return List.of();}
        ArrayList<Value> valList = new ArrayList<>(targetSet);
        Collections.sort(valList, comparator);
        return valList;

    }

    @Override
    public Set<Value> get(String key) {
        Node<Value> myNode = this.get(this.root, key, 0);
        if(myNode == null){return new HashSet<>();}
        return myNode.val;
    }

    private Node<Value> get(Node<Value> nodeCurr, String key, int x){
        if(nodeCurr == null){return null;}
        if(x == key.length()){return nodeCurr;}
        else{
            char c = key.charAt(x);
            return this.get(nodeCurr.links[c], key, x+1);
        }
    }


    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        //traverse down the tree till the end of the prefix
        //implement DFS to go down each path, checking for val, and appending to a set
        Set<Value> results = getAllWithPrefix(prefix);
        if(results.isEmpty()){return List.of();}
        ArrayList<Value> resultList = new ArrayList<>(results);
        Collections.sort(resultList, comparator);
        return resultList;
    }

    private Set<Value> getAllWithPrefix(String prefix){
        HashSet<Value> results = new HashSet<>();
        Node<Value> myNode = this.getAllWithPrefixSorted(this.root, prefix, 0, results);
        if(myNode == null){return Set.of();}
        if(results.isEmpty()){return Set.of();}
        return results;
    }

    private Node<Value> getAllWithPrefixSorted(Node<Value> nodeCurr, String prefix, int x, HashSet<Value> resultSet) {
        if(nodeCurr == null){return null;}
        if(x == prefix.length()){
            resultSet.addAll(nodeCurr.val);
            // logic to add all the children's values to set
            resultSet.addAll(this.getAllChildren(nodeCurr, resultSet));
            return nodeCurr;
        } else{
            char c = prefix.charAt(x);
            return this.getAllWithPrefixSorted(nodeCurr.links[c], prefix, x+1, resultSet);
        }
    }

    private Set<Value> getAllChildren(Node<Value> nodeCurr, HashSet<Value> resultSet){
        for(Node<Value> child : nodeCurr.links){
            if(child != null){
                resultSet.addAll(getAllChildren(child, resultSet));
            }
        } resultSet.addAll(nodeCurr.val);
        return resultSet;
    }


    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        /*Outline, Top method: run getAllwithPrefix, this method will return getAllWithPrefix
         * Make private method, that recurses until it finds the end of prefix.
         * Make new function to delete all the children under it (will always delete)
         * recurse back up and delete the corresponding prefix nodes
         * */
        Set<Value> willDelete = getAllWithPrefix(prefix);
        this.root = deleteAllWithPrefix(this.root, prefix, 0);
        return willDelete;
    }

    private Node<Value> deleteAllWithPrefix(Node<Value> nodeCurr, String prefix, int x){
        if(nodeCurr == null){return null;}
        if(x == prefix.length()){
            nodeCurr.links = new Node[alphabetSize];
            nodeCurr.val = new HashSet<>();
        } else{
            char c = prefix.charAt(x);
            nodeCurr.links[c] = this.deleteAllWithPrefix(nodeCurr.links[c], prefix, x+1);
        } if(!nodeCurr.val.isEmpty()){return nodeCurr;}
        for(Node<Value> element : nodeCurr.links){
            if(element != null){
                return nodeCurr;
            }
        } return null;
    }

    @Override
    public Set<Value> deleteAll(String key) {
        if(this.get(key).isEmpty()){return Set.of();}
        Set<Value> valToDelete = this.get(key);
        this.root = deleteAll(this.root, key, 0);
        return valToDelete;
    }

    private Node<Value> deleteAll(Node<Value> nodeCurr, String key, int x){
        if(nodeCurr == null){return null;}
        if(x == key.length()){
            nodeCurr.val = new HashSet<>();
        } else{
            char c = key.charAt(x);
            nodeCurr.links[c] = this.deleteAll(nodeCurr.links[c], key, x+1);
        } if(!nodeCurr.val.isEmpty()){
            return nodeCurr;
        } for(Node<Value> element : nodeCurr.links) {
            if (element != null) {
                return nodeCurr;
            }
        } return null;
    }


    @Override
    public Value delete(String key, Value val) {
        if(!this.get(key).contains(val)){return null;}
        this.root = this.delete(this.root, val, key, 0);
        return val;

    }

    private Node<Value> delete(Node<Value> nodeCurr, Value val, String key, int d){
        if(nodeCurr == null){return null;}
        if(d == key.length()){nodeCurr.val.remove(val);}
        else{
            char c = key.charAt(d);
            nodeCurr.links[c] = this.delete(nodeCurr.links[c], val, key, d+1);
        }
        if(!nodeCurr.val.isEmpty()){return nodeCurr;}
        for(Node<Value> element : nodeCurr.links){
            if(element != null){
                return nodeCurr;
            }
        } return null;
    }

}
