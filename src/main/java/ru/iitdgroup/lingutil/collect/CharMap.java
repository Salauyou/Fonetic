package ru.iitdgroup.lingutil.collect;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ru.iitdgroup.lingutil.collect.CharMap.CharEntry;


public abstract class CharMap<V> implements Iterable<CharEntry<V>> {

    
    private boolean immutable = false;
    
    
    /**
     * Returns how much keys are associated with non-null values
     */
    public abstract int size();
    
    
    /**
     * Returns value mapped to a given char key or null
     * if there is no association
     */
    public abstract V get(char c);
    
    
    /**
     * Returns if there is a value mapped to given key char
     */
    public abstract boolean containsKey(char c);
    
    
    /**
     * Maps given value to given key char, replacing any
     * former mapping. Null value is not allowed
     */
    public abstract CharMap<V> put(char c, V value);
    
    
    /**
     * Maps given value to given key char, resolving
     * conflict if a value already exists. Null value
     * is not allowed, as well as null resolver result
     */
    public abstract CharMap<V> merge(char c, V value, 
            BiFunction<? super V, ? super V, ? extends V> resolver);
    
    
    /**
     * Removes mapping to given key char
     */
    public abstract CharMap<V> remove(char c);
    
    
    /**
     * Makes this map immutable, so it will throw 
     * `UnsupportedOperationException` if any modification 
     * operation is called
     */
    public CharMap<V> makeImmutable() {
        immutable = true;
        return this;
    }

    
    /**
     * Performs multiple mapping, replacing any former mappings
     */
    @SafeVarargs
    public final CharMap<V> putAll(Entry<Character, ? extends V>... entries) {
        CharMap<V> cm = this;
        for (Entry<Character, ? extends V> e : entries)
            cm = cm.put(e.getKey(), e.getValue());
        return cm;
    }
 
    
    /**
     * Iterator over immutable entries
     */
    @Override
    public abstract Iterator<CharEntry<V>> iterator();
    
    
    /**
     * Iterator over entries that support `setValue()` unless map is immutable
     */
    public abstract Iterator<Entry<Character, V>> entries();
    
    
    /**
     * Stream of immutable entries
     */
    public Stream<CharEntry<V>> stream() {
        return StreamSupport.stream(
                  Spliterators.spliterator(iterator(), size(), 0), false);
    }
   
    
    /**
     * Stream of entries that support `setValue()` unless map is immutable
     */
    public Stream<Entry<Character, V>> entryStream() {
        return StreamSupport.stream(
                  Spliterators.spliterator(entries(), size(), 0), false);
    }
    
   
    // child classes should call it before modifications
    void checkMutability() {
        if (immutable)
            throw new UnsupportedOperationException("This CharMap is immutable");
    }
    
    
 
    // ----------- immutable CharEntry ----------- //
    
    /**
     * Immutable entry {char, V}
     */
    public static interface CharEntry<V> {
        char getChar();
        V    getValue();
    }
    
    
    
    // ------------ factory methods --------------- //
    
    /**
     * Creates an empty `CharMap`
     */
    public static <T> CharMap<T> create() {
        // return wrapper to allow work with it as with usual map
        return new CharMapWrapper<>();
    }
    
    
    
    // -------------- wrapper class ------------- //
    
    /** 
     * Wrapper returned by `CharMap.create()`, which internally 
     * holds the most appropriate `CharMap` implementation and switch
     * to another if needed. Outer code always gets a wrapper, package 
     * classes may directly instantiate concrete implementations
     */
    final static class CharMapWrapper<V> extends CharMap<V> {

        @SuppressWarnings("unchecked")
        CharMap<V> cm = CharMapImpl.MUTABLE_EMPTY;
        
        @Override
        public int size() {
            return cm.size();
        }

        @Override
        public V get(char c) {
            return cm.get(c);
        }

        @Override
        public boolean containsKey(char c) {
            return cm.containsKey(c);
        }

        @Override
        public CharMap<V> put(char c, V value) {    
            cm = cm.put(c, value);
            return this;
        }
        
        @Override
        public CharMap<V> merge(char c, V v, 
                BiFunction<? super V, ? super V, ? extends V> resolver) {
            cm = cm.merge(c, v, resolver);
            return cm;
        }

        @Override
        public CharMap<V> remove(char c) {
            cm = cm.remove(c);
            return this;
        }
        
        @Override
        public CharMap<V> makeImmutable() {
            cm = cm.makeImmutable();
            return this;
        }

        @Override
        public Iterator<CharEntry<V>> iterator() {
            return cm.iterator();
        }
        
        @Override
        public Iterator<Map.Entry<Character, V>> entries() {
            return cm.entries();
        }       
    }

}
