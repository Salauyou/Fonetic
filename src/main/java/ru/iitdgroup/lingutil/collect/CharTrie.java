package ru.iitdgroup.lingutil.collect;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ru.iitdgroup.lingutil.collect.CharMap.CharEntry;


/**
 * Map based on uncompressed char trie.
 * <p>
 * The main goal of implementation is to disclose access to char-keyed 
 * nodes, which is useful in searching algorithms that perform 
 * char-by-char matching
 * 
 * @author Salauyou
 */
public class CharTrie<V> implements Iterable<Entry<CharSequence, V>> {

    /*
     * This map is designed specially for text searching algorithms 
     * performing char-by-char matching. It allows external access 
     * to char-keyed nodes and their traversal using `Node#children()` 
     * iterator.
     * 
     * The price for such functionality is large amount of intermediate
     * nodes (with no value) and nodes that have only one child. In 
     * natural language dictionaries proportion of such nodes gains 3/4, 
     * so this implementation requires much more memory than standard 
     * string-key maps (~twice more than `HashMap<String, V>`, even with 
     * use of special implementation for nodes having one child).
     *
     * Below is `CharTrieMap` performance comparison against popular 
     * map implementations, including Apache Commons `PatriciaTrie`. 
     * Tests were performed on 200K random 3...11-char `String` keys, 
     * equal amount of existing and non-existing keys. Before each test, 
     * all keys were recreated to clear cached hash values.
     *     
     * As compared to | HashMap       | TreeMap       | PatriciaTrie     
     * ---------------+------------ --+---------------+---------------
     *          get() | ~1.4x slower  | ~2x faster    | ~1.4x faster
     *  containsKey() | ~1.4x slower  | ~2x faster    | ~1.4x faster
     *          put() | ~1.7x slower  | ~2.1x faster  | ~1.3x faster
     *       remove() |
     *  putIfAbsent() | ~1.7x slower  | ~2.3x faster  | ~1.8x faster
     * ---------------+----------- ---+---------------+---------------
     *     traversal: |
     *         first  |
     *    subsequent  |
     *   
     */ 
    
    
    volatile Node<V> root = new Node<>();
    int size = 0;
    
    
    /**
     * Associate the given key with given value, replacing
     * existing value, if set. Null values are disallowed
     */
    public CharTrie<V> put(CharSequence s, V v) {
        return merge(s, v, null);
    }
        
    
    /**
     * Puts an entry, resolving conflict if the key is already associated 
     * with some value.
     * <p>For example, to sum amounts use <tt>map.put(name, amount, Integer::sum)</tt>
     * 
     * @param resolver function of (existing value, offered value) whose result 
     *                 will be associated with the key
     */
    public CharTrie<V> merge(CharSequence s, V v, 
                             BiFunction<? super V, ? super V, ? extends V> resolver) {
        Objects.requireNonNull(v);
        if (root.put(s, 0, v, resolver) > 0)
            size++;
        return this;
    }
     
    
    final BiFunction<V, V, V> FIRST_ARGUMENT = (v1, v2) -> v1; 
    
    /**
     * Puts and entry if the key is not already associated with some value
     */
    public CharTrie<V> putIfAbsent(CharSequence s, V v) {
        Objects.requireNonNull(v);
        if (root.put(s, 0, v, FIRST_ARGUMENT) > 0)
            size++;
        return this;
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
    
    
    public CharTrie<V> putAll(CharTrie<? extends V> another) {
        return putAll(another, null);
    }
    
    
    public CharTrie<V> putAll(CharTrie<? extends V> another, 
                             BiFunction<? super V, ? super V, ? extends V> resolver) {
        throw new UnsupportedOperationException();
    }
    
    
    public CharTrie<V> putAll(Map<? extends CharSequence, ? extends V> another) {
        return putAll(another, null);
    }
    
    
    public CharTrie<V> putAll(Map<? extends CharSequence, ? extends V> another, 
                             BiFunction<? super V, ? super V, ? extends V> resolver) {
        for (Map.Entry<? extends CharSequence, ? extends V> e : another.entrySet())
            merge(e.getKey(), e.getValue(), resolver);
        return this;
    }
    
    
    public CharTrie<V> remove(CharSequence s) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
    
    
    public CharTrie<V> clear() {
        root = new Node<V>();
        size = 0;
        return this;
    }
    
    
    // --------------------- iteration support ------------------------ //
    
    /**
     * Iterator over entries
     */
    @Override
    public Iterator<Entry<CharSequence, V>> iterator() {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
    
    
    /**
     * Iterator over keys
     */
    public Iterable<CharSequence> keys() {
        return () -> {
            return new Iterator<CharSequence>() {
                Iterator<Entry<CharSequence, V>> i = CharTrie.this.iterator();                
                @Override public boolean   hasNext() { return i.hasNext(); }                
                @Override public CharSequence next() { return i.next().getKey(); }
            };
        };
    }
    
    
    /**
     * Iterator over values
     */
    public Iterable<V> values() {
        return () -> {
            return new Iterator<V>() {
                Iterator<Entry<CharSequence, V>> i = CharTrie.this.iterator();                
                @Override public boolean hasNext() { return i.hasNext(); }                
                @Override public V       next()    { return i.next().getValue(); }
            };
        };
    }
    
    
    /**
     * Stream of entries
     */
    public Stream<Entry<CharSequence, V>> stream() {
        return StreamSupport.stream(
                  Spliterators.spliterator(iterator(), size, 0), false);
    }
    
    
    
    // ----------------- node access ------------------- //
    
    public Node<V> getRoot() {
        return root;
    }
    
    
    
    // ---------------- Node class ------------------ //
    
    static public final class Node<V> {
        
        int nodeCount;          // TODO: remove (calculate on request)
        CharMap<Node<V>> next;
        V value = null;
               
        private Node() { }       
        
        
        // Adds a char sequence into node, creating subnodes if needed.
        // Returns: 0 - if node count isn't chanded nor value is assigned 
        //              to some empty node
        //          1 - if value is asigned to some existing node
        //    (c + 1) - if node count is changed by `c`
        int put(CharSequence s, int from, V v, 
                BiFunction<? super V, ? super V, ? extends V> resolver) {
            if (from == s.length()) {
                if (value == null) {
                    value = v;
                    return 1;
                } else {
                    value = resolver == null 
                          ? v 
                          : Objects.requireNonNull(resolver.apply(value, v));
                    return 0;
                }
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
            r += n.put(s, from + 1, v, resolver);
            nodeCount += Math.max(0, r - 1);
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

        
        // ------------ public accessors ------------ //
        
        public V getValue() {
            return value;
        }

        
        public Iterator<CharEntry<Node<V>>> children() {
            if (next == null)
                return Collections.emptyIterator();
            return next.iterator();
        }
    }

    
}
