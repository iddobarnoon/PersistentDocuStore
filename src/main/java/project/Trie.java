package project;

import java.util.Comparator;
import java.util.List;
import java.util.Set;


public interface Trie<Value>
{
    /**
     * add the given value at the given key
     * @param key
     * @param val
     */
    void put(String key, Value val);

    /**
     * Get all exact matches for the given key, sorted in descending order, where "descending" is defined by the comparator.
     * Search is CASE SENSITIVE.
     * @param key
     * @param comparator used to sort values
     * @return a List of matching Values. Empty List if no matches.
     */
    List<Value> getSorted(String key, Comparator<Value> comparator);

    /**
     * get all exact matches for the given key.
     * Search is CASE SENSITIVE.
     * @param key
     * @return a Set of matching Values. Empty set if no matches.
     */
    Set<Value> get(String key);

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order, where "descending" is defined by the comparator.
     * Search is CASE SENSITIVE.
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order. Empty List if no matches.
     */
    List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator);

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    Set<Value> deleteAllWithPrefix(String prefix);

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    Set<Value> deleteAll(String key);

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    Value delete(String key, Value val);
}