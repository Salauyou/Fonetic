package ru.iitdgroup.lingutil.collect;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Salauyou
 */
public class TrieSet implements Iterable<CharSequence> {

    final TrieMap<Object> m = new TrieMap<>();
    static final Object X = new Object();
    
    
    public TrieSet add(CharSequence s) {
        m.put(s, X);
        return this;
    }

    
    public int size() {
        return m.size();
    }
    
    
    public int nodeCount() {
        return m.nodeCount();
    }
    
    
    public boolean isEmpty() {
        return m.size() == 0;
    }
    
    
    public boolean contains(CharSequence s) {
        return m.containsKey(s);
    }

    
    public TrieSet remove(CharSequence s) {
        m.remove(s);
        return this;
    }


    @Override
    public Iterator<CharSequence> iterator() {
        return new Iterator<CharSequence>() {
            Iterator<Entry<CharSequence, Object>> i = m.iterator();
            @Override public boolean      hasNext() { return i.hasNext(); }
            @Override public CharSequence next()    { return i.next().getKey(); }
        };
    }
    
    
    public Stream<CharSequence> stream() {
        return StreamSupport.stream(
                  Spliterators.spliterator(iterator(), m.size, 0), false);
    }

}
