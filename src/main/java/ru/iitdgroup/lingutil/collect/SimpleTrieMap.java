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
 * <tt>TrieMap&lt;V&gt;</tt> implementation based on compressed char trie.
 * <p>
 * This map does not allow null values in order not change behavior
 * when implementation will be switched to concurrent version in future.
 * 
 * @see {@link TrieMap}
 * @see {@link TrieMap.TrieCursor}
 *
 * @author Salauyou
 */
public class SimpleTrieMap<V> extends AbstractMap<String, V> 
                              implements TrieMap<V> {
    
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
     * Below is `SimpleTrieMap` performance comparison against popular 
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
     * For keys of length ~50 performance degrades at about 1.5x, but 
     * still remains the best comparing to `TreeMap` and `PatriciaTrie`. 
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
        if (!(key instanceof CharSequence))
            return false;
        Node<V> n = findNode(root, (CharSequence) key);
        return n != null && n.value != null;
    }
    
    
    @Override
    public V get(Object key) {
        if (!(key instanceof CharSequence))
            return null;
        Node<V> n = findNode(root, (CharSequence) key);
        return n == null ? null : n.value;
    }
    
    

    /**
     * Returns node which exactly corresponds to the key, 
     * or null if such node doesn't exist
     */
    static <V> Node<V> findNode(Node<V> root, CharSequence key) {
        int len = key.length(), step = 0, p = 0;
        Node<V> n = root;
        CharMap<Node<V>> h = root.next;
        while (p < len) {
            if (h == null)
                return null;
            if ((n = h.get(key.charAt(p))) == null)
                return null;
            if (n.edge == null) 
                step = 1;
            else if (n.edge.length > len - p)
                return null;
            else if ((step = walkEdge(n, key, p)) < n.edge.length)
                return null;
            p += step;
            h = n.next;
        }
        return n;
    }
  
    
    
    /**
     * Returns position in node edge from where `s` cannot 
     * continue match
     */
    static <V> int walkEdge(Node<V> n, CharSequence s, int from) {
        int len = s.length(), p = 1, pos = from + 1;
        while (pos < len && p < n.edge.length 
               && s.charAt(pos) == n.edge[p]) {
            pos++;
            p++;
        }
        return pos - from;
    }
    
    
    
    @Override
    public V merge(String s, V value,
                   BiFunction<? super V, ? super V, ? extends V> resolver) {
        
        Objects.requireNonNull(value);
        int len = s.length();

        Prefix<V> p = findPrefix(root, s);  
        Node<V> n = p.ending;
        
        // prefix is full - modify existing node
        if (p.cutting == 0) {
            // replace existing value...
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
            // ...or attach a leaf
            attachLeaf(p.ending, value, s, p.length);
            size++;
            return null;
        }
        
        // prefix not full - split ending node
        n = n.split(p.cutting);
        if (p.length == len) 
            n.value = value; 
        else            
            attachLeaf(n, value, s, p.length);
        p.pred.next.put(p.key, n);
        size++;
        return null;
    }
    
    
    
    static <V> void attachLeaf(Node<V> node, V value, CharSequence s, int from) {
        Node<V> next = new Node<>(value, s, from);
        node.next = node.next == null 
                  ? new SingleCharMap<>(s.charAt(from), next)
                  : node.next.put(s.charAt(from), next);
    }
    
    
    
    static <V> Prefix<V> findPrefix(final Node<V> root, final CharSequence s) {
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
            Node<V> n = holder == null ? null : holder.get(keyChar);
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
    public V put(String key, V value) {
        Objects.requireNonNull(value);
        return merge(key, value, (v1, v2) -> v2);
    }
    
    
    @Override
    public V putIfAbsent(String key, V value) {
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
    public Set<Entry<String, V>> entrySet() {
        // TODO: optimize
        return new AbstractSet<Entry<String, V>>() {
            
            @Override
            public Iterator<Entry<String, V>> iterator() {
                return new Itr();
            }

            @Override
            public int size() {
                return SimpleTrieMap.this.size;
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
    
    
    
    final class Itr implements Iterator<Entry<String, V>> {
        
        Entry<String, V> next = null;
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
        public Entry<String, V> next() {
            if (!hasNext())
                throw new NoSuchElementException();
            Entry<String, V> e = next;
            next = null;
            return e;
        }
        
        
        Entry<String, V> nextFromCursor() {
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
    


    final static class ItrEntry<V> implements Entry<String, V> {

        final String s;
        final V v;
        
        ItrEntry(TrieCursor<V> cur) {
            s = cur.currentPrefix();
            v = cur.getValue();
        }
        
        ItrEntry(String key, V value) {
            s = key;
            v = value;
        }
        
        @Override
        public String getKey() {
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
