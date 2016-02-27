package ru.iitdgroup.lingutil.collect;

/**
 * Two-dimensional map of chars
 * 
 * @author Salauyou
 * @param <V>
 */
public class CharTable<V> {

    boolean immutable = false;
    
    @SuppressWarnings("unchecked")
    CharMap<CharMap<V>> table = CharMapImpl.MUTABLE_EMPTY;
    
    
    public CharTable<V> put(char f, char s, V value) {
        checkMutability();
        CharMap<V> row = table.get(f);
        if (row == null) {
            row = new CharMapImpl.SingleCharMap<>(s, value);
            table = table.put(f, row);
            return this;
        }
        CharMap<V> nRow = row.put(s, value);
        if (row != nRow)
            table.put(f, nRow);
        return this;
    }
    
    
    public CharTable<V> put(CharSequence c, V value) {
        if (c.length() != 2)
            throw new IllegalArgumentException();
        return put(c.charAt(0), c.charAt(1), value);
    }
    
    
    public V get(char f, char s) {
        CharMap<V> row = table.get(f);
        return row == null ? null : row.get(s);
    }
    
    
    public CharTable<V> makeImmutable() {
        immutable = true;
        return this;
    }
    
    
    // private stuff
    
    private void checkMutability() {
        if (immutable)
            throw new UnsupportedOperationException("This CharTable is immutable");
    }
    
}
