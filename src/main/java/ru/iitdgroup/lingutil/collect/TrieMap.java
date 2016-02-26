package ru.iitdgroup.lingutil.collect;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;


/**
 * Trie-based map
 * 
 * @author Salauyou
 */
public class TrieMap<V> implements Iterable<Map.Entry<CharSequence, V>> {

    /*
     * This trie implementation utilizes the fact that in real-life dictionaries 
     * a lot of trie nodes have only one child, so speed of access and
     * traversal can be improved by using special implementation for such nodes.
     *
     * Below is performance comparison against popular implementations, 
     * including Apache Commons `PatricialTrie`. Tests were performed on list of 
     * random 5...10-char string keys, equal amount of existing and non-existing keys. 
     * When testing on `HashMap`, all string keys were recreated to clear cached 
     * hash values.
     *                    
     * TrieMap<V> vs | HashMap<String, V> | TreeMap<String, V> | PatricialTrie<V>     
     * --------------+--------------------+--------------------+-----------------                              
     *         get() | ~2 times slower    | ~2 times faster    | ~25% faster
     * containsKey() |
     *      remove() |
     *         put() |
     * --------------+--------------------+--------------------+-----------------
     *    traversal: |
     *        first  |
     *   subsequent  |
     */ 
    
    
    
    // TODO in Node:
    //      1) value access: private setValue(), public V getValue()
    //      2) public iterator over first-level children only
    //      3) use in trie traversal for TrieMap#iterator()
    
    
    Node<V> root = new Node<>();
    int size = 0;
    
    
    /**
     * Puts an entry, replacing existing value. 
     * Null values are disallowed
     */
    public TrieMap<V> put(CharSequence s, V v) {
        Objects.requireNonNull(v);
        if (!containsKey(s))
            size++;
        root.put(s, 0, v);
        return this;
    }
        
    
    /**
     * Puts an entry, resolving conflict if value for the key is already set.
     * Null values are disallowed
     * 
     * @param resolver function of (existing value, offered value) whose result 
     *           will be associated with the key
     */
    public TrieMap<V> put(CharSequence s, V v, 
                          BiFunction<? super V, ? super V, ? extends V> resolver) {
        Objects.requireNonNull(v);
        return put(s, containsKey(s) ? resolver.apply(get(s), v) : v);
    }
        
    
    /**
     * Checks whether there is a value associated with the key
     */
    public boolean containsKey(CharSequence s) {
        return root.contains(s, 0);
    }
        
    
    /**
     * Returns the value associated with the key
     */
    public V get(CharSequence s) {
        return root.get(s, 0);
    }
    
    
    public int size() {
        return size;
    }
    
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    
    public int nodeCount() {
        return root.nodeCount;
    }
    
    
    public TrieMap<V> putAll(TrieMap<? extends V> another) {
        return putAll(another, (v1, v2) -> v2);
    }
    
    
    public TrieMap<V> putAll(TrieMap<? extends V> another, 
                             BiFunction<? super V, ? super V, ? extends V> resolver) {
        throw new UnsupportedOperationException();
    }
    
    
    public TrieMap<V> putAll(Map<? extends CharSequence, ? extends V> another) {
        return putAll(another, (v1, v2) -> v2);
    }
    
    
    public TrieMap<V> putAll(Map<? extends CharSequence, ? extends V> another, 
                             BiFunction<? super V, ? super V, ? extends V> resolver) {
        for (Map.Entry<? extends CharSequence, ? extends V> e : another.entrySet())
            put(e.getKey(), e.getValue(), resolver);
        return this;
    }
    
    
    public TrieMap<V> remove(CharSequence s) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
    
    
    // --------------------- iteration support ------------------------ //
    
    @Override
    public Iterator<Entry<CharSequence, V>> iterator() {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public Iterable<CharSequence> keys() {
        return () -> {
            return new Iterator<CharSequence>() {
                Iterator<Entry<CharSequence, V>> i = TrieMap.this.iterator();                
                @Override public boolean   hasNext() { return i.hasNext(); }                
                @Override public CharSequence next() { return i.next().getKey(); }
            };
        };
    }
    
    
    public Iterable<V> values() {
        return () -> {
            return new Iterator<V>() {
                Iterator<Entry<CharSequence, V>> i = TrieMap.this.iterator();                
                @Override public boolean hasNext() { return i.hasNext(); }                
                @Override public V       next()    { return i.next().getValue(); }
            };
        };
    }
    
    
    
    // ---------------- Node class ------------------ //
    
    static private final class Node<V> implements Iterable<Node<V>> {
        
        int nodeCount;
        CharMap<Node<V>> next;
        V value = null;
        
        
        // adds a char sequence into node, creating subnodes if needed,
        // returns how node count of this node is changed
        int put(CharSequence s, int from, V v) {
            if (from == s.length()) {
                value = v;
                return 0;
            }
            int  r = 0;
            char c = s.charAt(from);
            Node<V> n;
            if (next == null) {
                r = 1;
                n = new Node<>();
                next = new CharMapImpl.SingleCharMap<>(c, n);
            } else {
                n = next.get(c);
                if (n == null) {
                    n = new Node<>();
                    r = 1;
                    next = next.put(c, n);
                }
            }
            r += n.put(s, from + 1, v);
            nodeCount += r;
            return r;
        }
                
        
        boolean contains(CharSequence s, int from) {
            return get(s, from) != null;
        }  
            
        
        V get(CharSequence s, int from) {
            if (from == s.length())
                return value;
            if (next == null)
                return null;
            Node<V> n = next.get(s.charAt(from));
            return n == null ? null : n.get(s, from + 1); 
        }

        
        @Override
        public Iterator<Node<V>> iterator() {
            // TODO: implement
            return null;
        }
    }

    
}
