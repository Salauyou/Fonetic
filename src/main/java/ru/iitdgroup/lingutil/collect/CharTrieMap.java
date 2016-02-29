package ru.iitdgroup.lingutil.collect;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class CharTrieMap<V> extends AbstractMap<CharSequence, V> 
                            implements IterableTrie<V> {
    
    int size = 0;
    
    @Override
    public int size() {
        return size;
    }
    
    
    @Override
    public boolean containsKey(Object key) {
        // TODO: implement
        return super.containsKey(key);
    }
    
    
    @Override
    public V get(Object key) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
    
    
    @Override
    public V merge(CharSequence key, V value,
                   BiFunction<? super V, ? super V, ? extends V> resolver) {
        Objects.requireNonNull(value);
        // if resolver returns null, don't touch existing mapping !!!
        // TODO: implement
        throw new UnsupportedOperationException();
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
