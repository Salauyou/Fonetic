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

public class CharTrieMap<V> extends AbstractMap<CharSequence, V> 
                            implements IterableTrie<V> {
    
    int size = 0;
    
    V rootValue = null;
    @SuppressWarnings("unchecked")
    CharMap<Node<V>> root = CharMapImpl.MUTABLE_EMPTY;
    
    
    @Override
    public int size() {
        return size;
    }
    

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof CharSequence))
            return false;
        CharSequence s = (CharSequence) key;
        if (s.length() == 0)
            return rootValue != null;
        Prefix<V> p = findCommonPrefix(root, s);
        return p != null && p.length == s.length();
    }
    
    
    @Override
    public V get(Object key) {
        if (!(key instanceof CharSequence))
            return null;
        CharSequence s = (CharSequence) key;
        if (s.length() == 0)
            return rootValue;
        
        // TODO: optimize (traverse from root, 
        // failing fast if edge length > remainder part)
        Prefix<V> p = findCommonPrefix(root, s);
        return p != null && p.length == s.length() ? p.ending.value : null;
    }
    
    
    @Override
    public V merge(CharSequence key, V value,
                   BiFunction<? super V, ? super V, ? extends V> resolver) {
        
        Objects.requireNonNull(value);
        CharSequence s = (CharSequence) key;
        int len = s.length();
        
        // Special case for empty key
        if (len == 0) {
            V old = rootValue;
            V nv = resolver.apply(old, value);
            rootValue = nv == null ? old : nv;
            return old;
        }

        Prefix<V> p = findCommonPrefix(root, s);  
        Node<V> n = p.ending;
        // prefix is full - replace existing value 
        // or append a leaf
        if (p.cutting == 0) {
            // 1. Replace existing value
            if (p.length == len) {
                if (n.value == null) {
                    n.value = value;
                    return null;
                } else {
                    V old = n.value;
                    V nv = resolver.apply(old, value);
                    n.value = nv == null ? old : nv;
                    return old;
                }
            }
            // 2. Attach a leaf
            Node<V> nn = new Node<>(value, s, p.length);
            char c = s.charAt(p.length);
            if (n == null) 
                root = root.put(c, nn);
            else if (n.next == null) 
                n.next = new SingleCharMap<>(c, nn);
            else
                n.next = n.next.put(c, nn);
            return null;
        }
        
        // need to split existing node
        Node<V> sn = n.split(p.cutting);
        if (p.length == len) {
            sn.value = value;
        } else {
            Node<V> nn = new Node<>(value, s, p.length);
            sn.next = new SingleCharMap<>(s.charAt(p.length), nn);
        }
        if (p.pred == null) { 
            root = root.put(p.key, sn);
        } else {
            p.pred.next = p.pred.next == null 
                   ? new SingleCharMap<>(p.key, sn) 
                   : p.pred.next.put(p.key, sn);
        }
        return null;
    }
    
    
    
    static <V> Prefix<V> findCommonPrefix(final CharMap<Node<V>> root, 
                                          final CharSequence s) {
        CharMap<Node<V>> holder = root;
        Node<V> pred = null;
        Node<V> current = null;
        char keyChar = s.charAt(0);
        int len = s.length();
        int pos = 0;
        for(;;) {
            Node<V> n = holder == null ? null : holder.get(keyChar);
            if (n == null)
                return new Prefix<>(pred, keyChar, current, pos, 0);
            pred = current;
            current = n;
            // edge is more than 1 char
            if (n.edge != null) {                
                int p = 1;
                pos++;
                while (p < n.edge.length) {
                    if (pos == len || n.edge[p++] != s.charAt(pos++))
                        return new Prefix<>(pred, keyChar, current, pos - 1, p - 1);
                }
            } else {
                pos++;
            }
            if (pos == len)
                return new Prefix<>(pred, keyChar, current, pos, 0);
            holder = current.next;
            keyChar = s.charAt(pos);
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
        // TODO: implement
        throw new UnsupportedOperationException();
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
            Node<V> right = new Node<>(value, edge, 0, pos);
            Node<V> left = new Node<>(null, edge, pos, edge.length);
            left.next = new SingleCharMap<>(edge[pos], right);
            return left;
        }
        
        @Override
        public String toString() {
            return (edge == null ? "[]" : new StringBuilder().append('[').append(edge, 0, edge.length).append(']')) 
                    + ": " + String.valueOf(value);
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
