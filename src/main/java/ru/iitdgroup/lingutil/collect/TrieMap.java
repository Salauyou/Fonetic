package ru.iitdgroup.lingutil.collect;

import java.util.Map;
import java.util.NoSuchElementException;


/**
 * The main goal of this interface is to provide iterator-like 
 * <tt>TrieCursor</tt> for char-by-char traversal over trie nodes. This 
 * is useful in searching algorithms that perform matching over large 
 * set of short patterns.
 * <p>
 * At other hand, by extending <tt>Map&lt;String, V&gt;</tt> it allows to
 * operate data stored in tries in a very common way.
 * <p>
 * Although <tt>CharSequence</tt> might be conceptually more correct
 * candidate for key type since any trie is built upon sequences, not solid 
 * objects, <tt>String</tt> is much better choice because of immutability, 
 * consistency and completly predicted behavior. More than that, it allows 
 * to compare view sets and perform bulk operations mutually with other
 * maps where keys are <tt>String</tt>, which in practice occurs
 * more often than any other type.
 * 
 * 
 * @see {@link TrieMap.TrieCursor}
 *
 * @param <V> value type
 * 
 * @author Salauyou
 */
public interface TrieMap<V> extends Map<String, V> {

    
    /**
     * Returns new cursor with empty current prefix
     */
    TrieCursor<V> getCursor();
    
    
    /**
     * Cursor to perform char-by-char traversal over <tt>TrieMap</tt>.
     * <p>
     * Here is an example showing how <tt>TrieCursor</tt> can be
     * applied to effective lookup of dictionary keys in a string:
     * <p>
     * <blockquote><pre>
     * // Starting from each position i in the text, build the longest
     * // prefix existing in a dictionary trie that is common to 
     * // substring starting at i, checking for dictionary key matches
     * // at intermediate points
     * //
     * for (int i = 0; i < text.length() - 1; i++) {
     *     char c;
     *     int pos = i;
     *     TrieCursor&lt;V&gt; cursor = dictionary.getCursor();
     *     while (pos < text.length() && cursor.hasNext(c = text.charAt(pos++))) {
     *         cursor.next(c);
     *         if (cursor.hasValue()) {
     *             String match = cursor.currentPrefix();
     *             V      value = cursor.getValue();
     *             //... consume match and value
     *         }
     *     }
     * }</pre></blockquote>
     *
     * @author Salauyou
     * 
     */
    public static interface TrieCursor<V> {
        
        /**
         * Can current prefix be continued with some char?
         */
        boolean hasNext();
        
        /**
         * Can the specific char continue current prefix?
         */
        boolean hasNext(char c);
        
        /**
         * Picks a char continuing current prefix and appends it to prefix.
         * <p>
         * If multiple chars can continue current prefix, some "first" char 
         * is selected, usually by meaning of alphabetical order. To iterate 
         * over alternatives, use {@link TrieCursor#hasMore()} and 
         * {@link TrieCursor#more()}
         * @throws NoSuchElementException if prefix cannot be continued from
         *         current position
         */
        char next() throws NoSuchElementException;
        
        /**
         * Picks specific char continuing current prefix and appends it
         * to prefix
         * @throws NoSuchElementException if prefix cannot be continued 
         *         by the specific char (no such prefix exists)
         */
        char next(char c) throws NoSuchElementException;
        
        /**
         * Is there one or more alternative chars that can replace the 
         * ending char in current prefix?
         * <p>
         * To be used in conjunction with {@link TrieCursor#more()}
         * to iterate over prefix ending alternatives
         */
        boolean hasMore();
        
        /**
         * Can the specific char replace the ending char in current prefix?
         */
        boolean hasMore(char c);
        
        /**
         * Picks another char that can replace the ending char of current
         * prefix and applies replacement.
         * <p>
         * To be used in conjunction with {@link TrieCursor#hasMore()}
         * to iterate over prefix ending alternatives
         * @throws NoSuchElementException if there is no alternative
         *         char or they are already all traversed
         */
        char more() throws NoSuchElementException;
        
        /**
         * Replaces the ending char of current prefix by this char
         * @throws NoSuchElementException if such replacement cannot
         *         be applied (no such prefix exists)
         */
        char more(char c) throws NoSuchElementException;
        
        /**
         * Moves one step back reducing prefix by one char
         * @throws NoSuchElementException if prefix is already empty
         */
        char back() throws NoSuchElementException;
        
        
        
        /**
         * Is there is a value mapped to current prefix?
         */
        boolean hasValue();
        
        /**
         * Returns the value mapped to current prefix
         */
        V getValue();
        
        /**
         * Maps another value to current prefix
         */
        default V setValue(V value) {
            throw new UnsupportedOperationException();
        }
        
        /**
         * Returns cursor position, i. e. current prefix length - 1
         */
        int currentPosition();
        
        /**
         * Returns ending char of current prefix
         */
        char currentChar() throws NoSuchElementException;
        
        /**
         * Returns current prefix
         */
        String currentPrefix();
    }
    
}
