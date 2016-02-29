package ru.iitdgroup.lingutil.collect;

import java.util.NoSuchElementException;



public interface IterableTrie<V> {

    
    /**
     * Returns new cursor with empty current prefix
     */
    TrieCursor<V> getCursor();
    
    
    /**
     * Cursor to provide char-by-char traversal of <tt>IterableTrie</tt>.
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
     *             CharSequence match = cursor.currentPrefix();
     *             V            value = cursor.getValue();
     *             //... consume match and value
     *         }
     *     }
     * }</pre></blockquote>
     *
     * @author Salauyou
     */
    public static interface TrieCursor<V> {
        
        /**
         * Can current prefix be continued by some char?
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
         * Pick another char that can replace the ending char of current
         * prefix and apply replacement.
         * <p>
         * To be used in conjunction with {@link TrieCursor#hasMore()}
         * to iterate over prefix ending alternatives
         * @throws NoSuchElementException if there is no alternative
         *         char or they all are already traversed
         */
        char more() throws NoSuchElementException;
        
        /**
         * Replace the ending char of current prefix by this char
         * @throws NoSuchElementException if such replacement cannot
         *         be applied (no such prefix exists)
         */
        char more(char c) throws NoSuchElementException;
        
        /**
         * Move one step back reducing prefix by one char
         * @throws NoSuchElementException if prefix is already empty
         */
        char back() throws NoSuchElementException;
        
        
        
        /**
         * Is there is a value mapped to current prefix?
         */
        boolean hasValue();
        
        /**
         * Return the value mapped to current prefix
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
        char currentChar();
        
        /**
         * Returns current prefix
         */
        CharSequence currentPrefix();
    }
    
}
