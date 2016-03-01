package ru.iitdgroup.lingutil.collect;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import ru.iitdgroup.lingutil.collect.CharMapImpl.SingleCharMap;


/**
 * Map based on compressed char trie.
 * <p>
 * The main goal of implementation is to provide iterator-like
 * <tt>TrieCursor</tt> for char-by-char traversal over trie nodes. 
 * This is useful in searching algorithms that perform 
 * matching over large set of short char sequences.
 * 
 * @see {@link IterableTrie.TrieCursor}
 *
 * @author Salauyou
 */
public class CharTrieMap<V> extends AbstractMap<CharSequence, V> 
                            implements IterableTrie<V> {
    
    /*
     * This map is designed specially for text searching algorithms 
     * performing char-by-char matching over large set of short strings. 
     * To serve this purpose, it provides access to iterator-like 
     * `TrieCursor`.
     * 
     * For short keys, memory consumption is relatively similar to 
     * other popular `String`-keyed maps. This implementation
     * has much more nodes than, for example, `HashMap`, but 
     * at other hand doesn't require to store keys themselves.
     *
     * Below is `CharTrieMap` performance comparison against popular 
     * map implementations, including Apache Commons `PatriciaTrie`. 
     * For tests, 200K random 3...11-char `String` keys were taken, 
     * equal amount of existing and non-existing keys. Before each 
     * test, all keys are recreated to clear cached hash values.
     *     
     * As compared to: | HashMap       | TreeMap       | PatriciaTrie     
     * ----------------+------------ --+---------------+---------------
     *           get() | ~1.3x slower  | ~2.2x faster  | ~1.5x faster
     *   containsKey() | ~1.3x slower  | ~2.2x faster  | ~1.5x faster
     *           put() | ~1.7x slower  | ~2.2x faster  | ~1.5x faster
     *        remove() |
     *   putIfAbsent() | ~1.7x slower  | ~2.3x faster  | ~1.8x faster
     * entry traversal |
     *   
     */
    
    
    int size = 0;
    
    final Node<V> root = new Node<>(null, "\u0000", 0);
    
    @Override
    public int size() {
        return size;
    }
    

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }
    
    
    @Override
    public V get(Object key) {
        if (!(key instanceof CharSequence))
            return null;
        CharSequence s = (CharSequence) key;
      
        // TODO: optimize - traverse without node tracking, 
        // fail fast if at some point (edge length > remainder part)
        Prefix<V> p = findCommonPrefix(root, s);
        return (p != null && p.length == s.length() && p.cutting == 0) 
               ? p.ending.value 
               : null;
    }
    
    
    @Override
    public V merge(CharSequence key, V value,
                   BiFunction<? super V, ? super V, ? extends V> resolver) {
        
        Objects.requireNonNull(value);
        CharSequence s = (CharSequence) key;
        int len = s.length();

        Prefix<V> p = findCommonPrefix(root, s);  
        Node<V> n = p.ending;
        // prefix is full - modify existing node
        if (p.cutting == 0) {
            // replace existing value
            if (p.length == len) {
                if (n.value == null) {
                    n.value = value;
                    size++;
                    return null;
                } else {
                    V old = n.value;
                    V nv = resolver.apply(old, value);
                    n.value = nv == null ? old : nv;
                    return old;
                }
            }
            // attach a leaf
            Node<V> nn = new Node<>(value, s, p.length);
            char c = s.charAt(p.length);
            n.next = n.next == null 
                   ? new SingleCharMap<>(c, nn)
                   : n.next.put(c, nn);
            size++;
            return null;
        }
        
        // prefix not full - split existing node
        Node<V> sn = n.split(p.cutting);
        // set value at split point
        if (p.length == len) {
            sn.value = value;
        } else {
            // attach a leaf
            Node<V> nn = new Node<>(value, s, p.length);
            sn.next = sn.next == null 
                    ? new SingleCharMap<>(s.charAt(p.length), nn)
                    : sn.next.put(s.charAt(p.length), nn);
        }
        // reassign mapping
        p.pred.next.put(p.key, sn);
        size++;
        return null;
    }
    
    
    
    static <V> Prefix<V> findCommonPrefix(final Node<V> root, 
                                          final CharSequence s) {
        Node<V> pred = null;
        Node<V> current = root;
        char keyChar = '\u0000';
        int len = s.length();
        int pos = 0;
        CharMap<Node<V>> holder = root.next;
        for(;;) {
            if (pos == len)
                return new Prefix<>(pred, keyChar, current, pos, 0);
            keyChar = s.charAt(pos);
            Node<V> n = holder == null 
                      ? null 
                      : holder.get(keyChar);
            if (n == null)
                return new Prefix<>(pred, keyChar, current, pos, 0);
            pred = current;
            current = n;
            pos++;
            // edge is more than 1 char
            if (n.edge != null) {                
                for (int p = 1; p < n.edge.length; p++, pos++) {
                    if (pos == len || n.edge[p] != s.charAt(pos))
                        return new Prefix<>(pred, keyChar, current, pos, p);
                }
            }            
            holder = current.next;
        }
    }
    
    
    static final class Prefix<V> {
        final int length;      // length of common prefix
        final Node<V> ending;  // ending node
        final int cutting;     // position of split of ending node
        final Node<V> pred;    // node, which `next` holds ending node
        final char key;        // key, by which `pred.next` holds ending node
        
        Prefix(Node<V> p, char c, Node<V> e, int len, int cut) {
            key = c;
            pred = p;
            ending = e;
            length = len;
            cutting = cut;
        }
    }
    
    
    
    @Override
    public V put(CharSequence key, V value) {
        Objects.requireNonNull(value);
        return merge(key, value, (v1, v2) -> v2);
    }
    
    
    @Override
    public V putIfAbsent(CharSequence key, V value) {
        Objects.requireNonNull(value);
        return merge(key, value, (v1, v2) -> v1);
    }
    
    
    @Override
    public boolean containsValue(Object value) {
        // TODO: optimize
        return super.containsValue(value);
    }
    
    
    @Override
    public void clear() {
        root.next = null;
        root.value = null;
        size = 0;
    }
    
    
    
    @Override
    public TrieCursor<V> getCursor() {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
    
    
    
    @Override
    public Set<Entry<CharSequence, V>> entrySet() {
        // TODO: optimize
        return new AbstractSet<Entry<CharSequence, V>>() {
            
            @Override
            public Iterator<Entry<CharSequence, V>> iterator() {
                return new Itr();
            }

            @Override
            public int size() {
                return CharTrieMap.this.size;
            }
        };
    }
    
    
    
    final static class Node<V> {
        
        V value;                // value
        final char[] edge;      // edge, if it is more than 1 char
        CharMap<Node<V>> next;  // next node holder
        
        Node(V value, CharSequence s, int from) {
            this.value = value;
            if (s.length() - from == 1)
                edge = null;
            else {
                edge = new char[s.length() - from];
                for (int i = from; i < s.length(); i++)
                    edge[i - from] = s.charAt(i);
            }
        }
        
        Node(V value, char[] s, int from, int to) {
            this.value = value;
            edge = (to - from > 1) ? Arrays.copyOfRange(s, from, to) : null;
        }
        
        Node<V> split(int pos) {
            Node<V> left = new Node<>(null, edge, 0, pos);
            Node<V> right = new Node<>(value, edge, pos, edge.length);
            left.next = new SingleCharMap<>(edge[pos], right);
            right.next = this.next;
            return left;
        }
        
        @Override
        public String toString() {
            return String.format("[%s: %s]", 
                    edge == null ? "" : new StringBuilder().append(edge, 0, edge.length), 
                    value);
        }
    }
    
    
    
    final class Itr implements Iterator<Entry<CharSequence, V>> {
        
        Entry<CharSequence, V> next = null;
        final TrieCursor<V> cur = getCursor();
        boolean finished = false;
        boolean rootExplored = false;        
        
        @Override
        public boolean hasNext() {
            if (!rootExplored) {
                if (cur.hasValue()) 
                    next = new ItrEntry<>(cur);
                rootExplored = true;
            }
            if (next == null && !finished) 
                finished = (next = nextFromCursor()) == null;
            return !finished;
        }

        @Override
        public Entry<CharSequence, V> next() {
            if (!hasNext())
                throw new NoSuchElementException();
            Entry<CharSequence, V> e = next;
            next = null;
            return e;
        }
        
        
        Entry<CharSequence, V> nextFromCursor() {
            // perform DFS from current cursor position 
            for(;;) {
                // try to continue prefix
                while (cur.hasNext()) {
                    cur.next();
                    if (cur.hasValue())
                        return new ItrEntry<>(cur);
                }
                // reduce prefix until find 
                // alternative char for its ending
                while (!cur.hasMore()) {            
                    // if finished, return null
                    if (cur.currentPosition() == 0)
                        return null;                
                    cur.back();
                }
                cur.more();
                if (cur.hasValue())
                    return new ItrEntry<>(cur);
            }
        }
            
        /**
         * Not supported yet
         */
        @Override
        public void remove() {
            // TODO: support this in future
            throw new UnsupportedOperationException();
        }
    }
    


    final static class ItrEntry<V> implements Entry<CharSequence, V> {

        final CharSequence s;
        final V v;
        
        ItrEntry(TrieCursor<V> cur) {
            s = cur.currentPrefix();
            v = cur.getValue();
        }
        
        ItrEntry(CharSequence key, V value) {
            s = key;
            v = value;
        }
        
        @Override
        public CharSequence getKey() {
            return s;
        }

        @Override
        public V getValue() {
            return v;
        }

        /**
         * Not supported yet
         */
        @Override
        public V setValue(V value) {
            // TODO: implement mutability
            throw new UnsupportedOperationException();
        }
        
        @Override
        public String toString() {
            return s + "=" + v;
        }
        
    }
    
    
}
