package ru.iitdgroup.lingutil.collect;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ru.iitdgroup.lingutil.collect.CharMap.CharEntry;
import ru.iitdgroup.lingutil.collect.CharMapImpl.SingleCharMap;


/**
 * Map based on uncompressed char trie.
 * <p>
 * The main goal of implementation is to disclose access to char-keyed 
 * nodes, which is useful in searching algorithms that perform 
 * char-by-char matching over large set of short char sequences
 * 
 * @author Salauyou
 */
public class CharTrie<V> implements Iterable<Entry<CharSequence, V>> {

    /*
     * This map is designed specially for text searching algorithms 
     * performing char-by-char matching over large set of short strings. 
     * It provides external access to char-keyed nodes and their 
     * traversal using `Node#children()` iterator.
     * 
     * The price for such functionality is large amount of intermediate
     * nodes (with no value) and nodes that have only one child. In 
     * natural language dictionaries proportion of such nodes gains 3/4, 
     * so this implementation requires much more memory than standard 
     * string-key maps (~twice more than `HashMap<String, V>`, even with 
     * use of special implementation for nodes having one child).
     *
     * Below is `TrieMap` performance comparison against popular map 
     * implementations, including Apache Commons `PatriciaTrie`. Tests 
     * were performed on 200K random 3...11-char `String` keys, equal 
     * amount of existing and non-existing keys. Before each test, 
     * all keys were recreated to clear cached hash values.
     *     
     * As compared to: | HashMap       | TreeMap       | PatriciaTrie     
     * ----------------+------------ --+---------------+---------------
     *           get() | ~1.4x slower  | ~2x faster    | ~1.5x faster
     *   containsKey() | ~1.4x slower  | ~2x faster    | ~1.5x faster
     *           put() | ~1.8x slower  | ~2.2x faster  | ~1.3x faster
     *        remove() |
     *   putIfAbsent() | ~1.8x slower  | ~2.3x faster  | ~1.8x faster
     * entry traversal |
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
     * Puts and entry if the key is not already associated with some value
     */
    public CharTrie<V> putIfAbsent(CharSequence s, V v) {
        return merge(s, v, (v1, v2) -> v1);
    }
    
    
    /**
     * Puts an entry, resolving conflict if the key is already associated 
     * with some value.
     * <p>E. g., to sum amounts use <tt>map.put(name, amount, Integer::sum)</tt>
     * 
     * @param resolver function of (existing value, offered value) whose result 
     *                 will be associated with the key
     */
    public CharTrie<V> merge(CharSequence s, V v, 
                             BiFunction<? super V, ? super V, ? extends V> resolver) {
        requireNonNull(v);
        // search existing prefix for key
        Prefix<V> pf = findLongestPrefix(root, s, 0);
        Node<V> n = pf.ending;
        int     p = pf.p;
        // prefix covers the key?
        if (p == s.length()) {
            V old = n.value;
            if (old == null) {
                n.value = v;
                size++;
            } else 
                n.value = resolver == null 
                        ? v : requireNonNull(resolver.apply(old, v));
            return this;
        }
        // need to append linear branch
        Node<V> b = buildLinearBranch(s, p, v);
        n.next = n.next == null 
               ? new SingleCharMap<>(s.charAt(p), b)
               : n.next.put(s.charAt(p), b);
        size++;
        return this;
    }
   

    /**
     * Checks whether there is a value associated with the key
     */
    public boolean containsKey(CharSequence s) {
        return get(s) != null;
    }
        
    
    /**
     * Returns the value associated with the key
     */
    public V get(CharSequence s) {
        Prefix<V> pf = findLongestPrefix(root, s, 0);
        return pf.p == s.length() ? pf.ending.value : null;
    }
    
    
    public int size() {
        return size;
    }
    
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    
    public int nodeCount() {
        return 0;
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
    
    
    
    // --------------- utility stuff --------------- //
    
    static <V> Node<V> buildLinearBranch(CharSequence s, int from, V v) {
        Node<V> nn;
        Node<V> b = new Node<V>(v);
        for (int i = s.length() - 1; i > from; i--) {
            nn = new Node<>();
            nn.next = new SingleCharMap<>(s.charAt(i), b);
            b = nn;
        }
        return b;
    }
    
    
    static <V> Prefix<V> findLongestPrefix(Node<V> n, CharSequence s, int from) {
        int len = s.length();
        Node<V> nn;
        int p = from;        
        while (p < len && n.next != null 
               && (nn = n.next.get(s.charAt(p))) != null) {
            n = nn;
            p++;
        }
        return new Prefix<>(n, p);
    }
    
    
    static final class Prefix<V> {
        final Node<V> ending;
        final int p;
        
        Prefix(Node<V> node, int p) {
            this.ending = node;
            this.p = p;
        }
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

        CharMap<Node<V>> next;
        V value = null;
               
        private Node() { }
        
        private Node(V value) { 
            this.value = value;
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
