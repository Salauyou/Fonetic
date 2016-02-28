package ru.iitdgroup.lingutil.collect;

import static java.util.Collections.emptyIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;



/**
 * @author Salauyou
 * @param <V> value type
 */
class CharMapImpl {    

    private CharMapImpl() { }
    
    
    static final class MultiCharMap<V> extends CharMap<V> {

        static final int WIDTH = 16;

        @SuppressWarnings("unchecked")
        final Cme<V>[] table = new Cme[WIDTH];
        int size = 0;

        
        // instantiation within package only
        MultiCharMap() { }
        
        
        @Override
        public int size() {
            return size;
        }

        
        @Override
        public V get(char c) {
            Cme<V> e = table[bitFor(c)];
            while (e != null && e.c != c)
                e = e.next;
            return e == null ? null : e.v;
        }

        
        @Override
        public boolean containsKey(char c) {
            Cme<V> e = table[bitFor(c)];
            while (e != null && e.c != c)
                e = e.next;
            return e != null;
        }

        
        @Override
        public CharMap<V> put(char c, V value) {
            checkMutability();
            int p = bitFor(c);
            Cme<V> e = table[p];
            if (e == null) {
                table[p] = new Cme<>(this, c, value);
                size++;
                return this;
            }
            while (e.c != c && e.next != null) 
                e = e.next;
            if (e.c == c)
                e.v = value;
            else {
                e.next = new Cme<>(this, c, value);
                size++;
            }
            return this;
        }

        
        @Override
        public CharMap<V> merge(char c, V value, 
                   BiFunction<? super V, ? super V, ? extends V> resolver) {
            checkMutability();
            int p = bitFor(c);
            Cme<V> e = table[p];
            if (e == null) {
                table[p] = new Cme<>(this, c, value);
                size++;
                return this;
            }
            while (e.c != c && e.next != null) 
                e = e.next;
            if (e.c == c)
                e.v = Objects.requireNonNull(resolver.apply(e.v, value));
            else {
                e.next = new Cme<>(this, c, value);
                size++;
            }
            return this;
        }
        
        
        @Override
        @SuppressWarnings("unchecked")
        public CharMap<V> remove(char c) {
            checkMutability();
            int p = bitFor(c);
            Cme<V> e = table[p];
            if (size == 1) 
                return e.c == c ? MUTABLE_EMPTY : this;
            Cme<V> pred = null;              
            while (e != null && e.c != c) {
                pred = e;
                e = e.next;
            }
            if (e == null)     // key not found
                return this;
            size--;
            if (pred == null) 
                table[p] = e.next;
            else 
                pred.next = e.next;               
            e.next = null;
            return this;    
        }
        
        
        @Override
        @SuppressWarnings("unchecked")
        public CharMap<V> clear() {
            checkMutability();
            return MUTABLE_EMPTY;
        } 
        
        
        @Override
        public Iterator<CharEntry<V>> iterator() {
            return new Iterator<CharEntry<V>> () {
                int i = 0;
                final List<Cme<V>> cached = cacheEntries();
                
                @Override public boolean hasNext() { 
                    return i < cached.size();
                }

                @Override
                public CharEntry<V> next() {
                    if (i >= cached.size())
                        throw new NoSuchElementException();
                    return cached.get(i++);
                }                
            };
            
        }
        
        
        @Override
        public Iterator<Entry<Character, V>> entries() {

            return new Iterator<Entry<Character, V>> () {
                int i = 0;
                final List<Cme<V>> cached = cacheEntries();
                
                @Override public boolean hasNext() { 
                    return i < cached.size(); 
                }

                @Override
                public Entry<Character, V> next() {
                    if (i >= cached.size())
                        throw new NoSuchElementException();
                    return cached.get(i++);
                }                
            };
        }
        
        
        
        List<Cme<V>> cacheEntries() {
            final List<Cme<V>> cached = new ArrayList<>(size);
            for (Cme<V> e : table) {
            	while (e != null) {
            		cached.add(e);
                    e = e.next;
                }
            }
            Collections.sort(cached);
            return cached;
        }
        
        
        static int bitFor(char c) {
            return c & WIDTH - 1;
        }
        
        
        
        // -------- char map entry ---------- //

        static class Cme<V> implements CharEntry<V>, 
                                       Entry<Character, V>, 
                                       Comparable<Cme<V>> {
            Cme<V> next = null;
            final char c;
            final CharMap<V> m;
            V v;

            Cme(CharMap<V> m, char c, V value) {
                this.c = c;
                this.v = value;
                this.m = m;
            }
           
            @Override public Character getKey()   { return c; }
            @Override public V         getValue() { return v; }

            @Override public V setValue(V value) {
                m.checkMutability();
                Objects.requireNonNull(value);
                V old = v;
                v = value;
                return old;
            }
            
            @Override
            public char getChar() {
                return c;
            }
            
            @Override 
            public int compareTo(Cme<V> o) { 
                return this.c - o.c; 
            }
        }  
    }
    
    
    
    // ----------- empty char maps --------- //
    
    @SuppressWarnings("rawtypes")
    final static CharMap MUTABLE_EMPTY = new CharMap() {
        
        @Override public Object   get(char c)         { return null; }
        @Override public boolean  containsKey(char c) { return false; }
        @Override public int      size()              { return 0; }
        @Override public CharMap  remove(char c)      { return this; }        
        @Override public CharMap  clear()             { return this; }
        @Override public CharMap  makeImmutable()     { return IMMUTABLE_EMPTY; }
        @Override public Iterator iterator()          { return emptyIterator(); }
        @Override public Iterator entries()           { return emptyIterator(); }        
        
        @Override
        public CharMap put(char c, Object value) { 
            Objects.requireNonNull(value);
            return new SingleCharMap<>(c, value); 
        }
        
        @Override
        public CharMap merge(char c, Object value, BiFunction resolver) {
            return put(c, value);
        }
    };
    

    @SuppressWarnings("rawtypes")
    final static CharMap IMMUTABLE_EMPTY = new CharMap() {
        
        @Override public Object   get(char c)         { return null; }
        @Override public boolean  containsKey(char c) { return false; }
        @Override public int      size()              { return 0; }       
        @Override public Iterator iterator()          { return emptyIterator(); }
        @Override public Iterator entries()           { return emptyIterator(); }
        
        @Override public CharMap put(char c, Object value) { 
            Objects.requireNonNull(value);
            checkMutability();                // this will always throw
            return this;
        }
        
        @Override
        public CharMap merge(char c, Object value, BiFunction resolver) {
            return this.put(c, value);        // this will always throw
        }
        
        @Override public CharMap remove(char c) { 
            checkMutability();                // this will always throw
            return this; 
        }
        
        @Override public CharMap clear() {
            checkMutability();                // this will always throw
            return this;
        }
    }.makeImmutable();
    

    
    // ------------ single-key char map ----------- //
    
    final static class SingleCharMap<V> extends CharMap<V> {

        final char c;
        V v;  
        
        
        // instaitiation within package only
        SingleCharMap(char c, V v) {
            this.c = c;
            this.v = v;
        }        
        
        
        @Override public V       get(char c)         { return this.c == c ? v : null; }
        @Override public boolean containsKey(char c) { return this.c == c; }
        @Override public int     size()              { return 1; }
         
        
        @Override
        public CharMap<V> put(char c, V value) { 
            return merge(c, value, (v1, v2) -> v2);
        }       
                
        
        @Override
        public CharMap<V> merge(char c, V value,
                BiFunction<? super V, ? super V, ? extends V> resolver) {
            Objects.requireNonNull(value);
            checkMutability();
            if (this.c == c) {
                this.v = Objects.requireNonNull(resolver.apply(this.v, value));
                return this;
            } else
                return new MultiCharMap<V>()
                            .put(this.c, this.v)
                            .put(c, value);
            }
        
        
        @Override
        @SuppressWarnings("unchecked")
        public CharMap<V> remove(char c) { 
            checkMutability();
            return c == this.c ? MUTABLE_EMPTY : this;
        }
        
        
        @Override
        @SuppressWarnings("unchecked")
        public CharMap<V> clear() { 
            checkMutability();
            return MUTABLE_EMPTY;
        }
        
        
        @Override
        public Iterator<CharEntry<V>> iterator() {
            return new Iterator<CharEntry<V>>() {
                boolean used = false;                
                @Override public boolean hasNext() { return !used; }
                @Override public CharEntry<V> next() {
                    if (used)
                        throw new NoSuchElementException();    
                    used = true;
                    return new CharEntry<V>() {
                        @Override public char getChar()  { return c; }
                        @Override public V    getValue() { return v; }                        
                    };
                }                
            };
        }        
        
        
        @Override
        public Iterator<Entry<Character, V>> entries() {
            return new Iterator<Map.Entry<Character, V>>() {
                boolean used = false;                
                @Override public boolean hasNext() { return !used; }
                @Override public Entry<Character, V> next() {
                    if (used)
                        throw new NoSuchElementException();    
                    used = true;
                    return singleEntry();
                }                
            };
        }
            
        
        Entry<Character, V> singleEntry() {
            return new Entry<Character, V>() {
                @Override public Character getKey()   { return c; }
                @Override public V         getValue() { return v; }

                @Override public V setValue(V value) {
                    checkMutability();
                    V old = v;
                    v = value;
                    return old;
                }
            };
        }
    }

    
}
