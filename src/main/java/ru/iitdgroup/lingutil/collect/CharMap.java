package ru.iitdgroup.lingutil.collect;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ru.iitdgroup.lingutil.collect.CharMap.CharEntry;


public abstract class CharMap<V> implements Iterable<CharEntry<V>> {

    
    private boolean immutable = false;
    
    
    /**
     * Returns how much keys are associated with non-null values
     */
    public abstract int size();
    
    
    /**
     * Returns value mapped to a given char key; or null
     * if there is no association, or key char is out of allowed
     * range, or it is mapped to null
     */
    public abstract V get(char c);
    
    
    /**
     * Returns if there is non-null value mapped 
     * to given key char
     */
    public abstract boolean containsKey(char c);
    
    
    /**
     * Maps given value to given key char, replacing any
     * former mapping
     */
    public abstract CharMap<V> put(char c, V value);
    
    
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
     * Iterator of immutable entries
     */
    @Override
    public abstract Iterator<CharEntry<V>> iterator();
    
    
    /**
     * Iterator which allows to modify values (unless map is immutable)
     */
    public abstract Iterator<Map.Entry<Character, V>> entries();
    
    
   
    // child classes should call it before modifications
    void checkMutability() {
        if (immutable)
            throw new UnsupportedOperationException("This CharMap is immutable");
    }
    
    
 
    // ----------- immutable CharEntry ----------- //
    
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
     * Wrapper which is returned by `CharMap.create()`,
     * which internally holds the most appropriate implementation.
     * Outer code always gets a wrapper, package classes
     * may directly instantiate needed implementation
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
