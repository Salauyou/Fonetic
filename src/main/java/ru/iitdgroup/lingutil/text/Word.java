package ru.iitdgroup.lingutil.text;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * <p>
 * Class implements conception of "word" as some "value" (its textual view) 
 * that is mapped to some substring of a "source" (from which it is initially 
 * extracted). Word can be modified so its value and mapping is changed
 * (by cutting off chars, assigning a new value, joining with another word etc),
 * but source itself is never lost nor modified (the only exception 
 * is when `crop()` is applied to a word with empty value, which sets source 
 * to null). So after any number of modifications it is possible to discover
 * which part of the source resulting word corresponds to, without 
 * explicit tracking how mapping is changed across modifications.
 * <p>
 * Instances are immutable, making it safe to feed multiple consumers with the
 * same sets of words without any kind of synchronization. 
 * <p>
 * Class contains factory methods to construct words based on given source:
 * {@link Word#of(CharSequence)} and {@link Word#ofSubstring(CharSequence, int, int)},
 * and modification methods that return modified word, leaving
 * 'this' object unchanged, e. g. <tt>cropped = word.crop(2, 5).</tt>
 * <p>
 * Methods that change value but do not change mapping:
 * <ul>
 * <li>{@link Word#as(CharSequence)},
 * <li>{@link Word#transform(CharTransformer)},
 * <li>{@link Word#transform(StringTransformer)},
 * <li>{@link Word#cutMiddle(int, int)}</ul>
 * <p>
 * Methods that change value and mapping:
 * <ul>
 * <li>{@link Word#crop(int, int)},
 * <li>{@link Word#cutHead(int)},
 * <li>{@link Word#cutTo(int)}</ul>
 * <p>
 * Method {@link Word#crop()} can change mapping, but never changes value.
 * <p>
 * `Word` implements {@link CharSequence} of its value, making it possible 
 * to directly pass `Word` instances as arguments onto utility methods like as in 
 * {@link org.apache.commons.lang3.StringUtils}.
 * 
 * @author Salauyou
 * 
 * @Immutable
 */
public final class Word implements CharSequence {
    
    final String source;
    final String value;
    
    final int start;  // starting position in source
    final int end;    // ending position
    final int[] p;    // mapping of value chars to source chars
    
    
    private Word(String source, int start, int end, String value, int[] p) {
        this.source = source;
        this.value = value == null ? "" : value;
        this.start = start;
        this.end = end;
        this.p = p;
    }

    
    private Word(String source, int start, String value) {
        this(source, start, start + lengthOf(value), value, 
             naturalSeq(start, lengthOf(value)));
    }

    

    
    // ------------ public accessors ------------- //
    
    /**
     * Returns textual view of this word
     */
    public String value() { return value; }
    
    
    /**
     * Returns source substring to which this word is mapped
     */
    public String getMappedSubstring() { 
        return source == null ? null : source.substring(start, end); 
    }
    
    
    /**
     * Word having null source and empty value, often helpful 
     * in join manipulations. For example, <tt>w = w.join(Word.EMPTY.as(" "))</tt> 
     * is equivalent to <tt>w = w.as(w.value() + " ")</tt>
     */
    public static final Word EMPTY = new Word(null, -1, "");
    
    
    
    // -------- factory creation methods --------- //
    
    /**
     * Returns a word in which source and value are set to a given string
     */
    public static Word of(CharSequence s) {
        if (s == null) 
            return EMPTY;
        String ss = s.toString();
        return new Word(ss, 0, ss);
    }
    
    
    /**
     * Returns a word in which source is set to a given string, 
     * and value is mapped to its substring
     */
    public static Word ofSubstring(CharSequence s, int start, int end) {
        if (s == null) 
            return EMPTY;
        String ss = s.toString();
        start = Math.max(start, 0);
        end   = Math.min(end, ss.length());
        return new Word(ss, start, ss.substring(start, end));
    }
    
    
    
    // ---------- modification methods ------------ //
    
    /**
     * Appends a given word
     * @throws IllegalArgumentException if offered word has a different source
     */
    public Word join(Word another) throws IllegalArgumentException {
        if (another == EMPTY)
            return this;
        String s = checkAndGetCommonSource(another);
        String nv = this.value + another.value;
        int[] p = nv.length() == 0 ? null : new int[nv.length()];
        if (p != null) {
            int from = 0;
            if (this.p != null) {
                System.arraycopy(this.p, 0, p, 0, this.p.length);
                from = this.p.length;
            }
            if (another.p != null) 
                System.arraycopy(another.p, 0, p, from, another.p.length);
        }
        int newStart; 
        int newEnd;  
        if (this.start < 0) {
            newStart = another.start;
            newEnd = another.end;
        } else if (another.start < 0) {
            newStart = this.start;
            newEnd = this.end;
        } else {
            newStart = Math.min(this.start, another.start);
            newEnd   = Math.max(this.end, another.end);
        }
        return new Word(s, newStart, newEnd, nv, p);
    }
    

    /**
     * Appends a given string to value (shortcut for 
     * <tt>w.as(w.value() + s)</tt>)
     */
    public Word join(CharSequence s) {
        return as(value + s);
    }
    
    
    /**
     * Appends a given words in given order
     * @throws IllegalArgumentException if one of offered words 
     *         has a different source
     */
    public Word join(Word... another) throws IllegalArgumentException {
        Word w = this;
        for (Word a : another)
            w = w.join(a);
        return w;
        // TODO: optimize
    }
    
    
    /**
     * Reduces the word removing last chars, so that value of result 
     * becames of a given length, and mapping is changed accordingly
     */
    public Word cutTo(int length) {
        return subword(0, length, false, true);
    }
    
    
    /**
     * Reduces the word removing first <tt>start</tt> chars, so that
     * mapping is changed accordingly.
     * Similar to {@link String#substring(int)}
     */
    public Word cutHead(int start) {
        return subword(start, value.length(), true, false);
    }
    
    
    /**
     * Reduces the word removing chars between
     * <tt>start</tt> and <tt>end</tt>
     */
    public Word cutMiddle(int start, int end) {
        if (start == end)
            return this;
        start = Math.max(start, 0);
        end   = Math.min(end, value.length());
        return subword(0, start, false, true)
                 .join(subword(end, value.length(), true, false));
        // TODO optimize
    }
    
    
    /**
     * Leaves value unchanded, but reduces width of 
     * mapped substring to minimum possible
     */
    public Word crop() {
        return subword(0, value.length(), true, true);
    }
    
    
    /**
     * Reduces the word leaving chars between <tt>start</tt>
     * and <tt>end</tt>, so that mapping is changed accorgingly. 
     * Similar to {@link String#substring(int, int)}
     */
    public Word crop(int start, int end) {
        return subword(start, end, true, true);
    }
    
    
    /**
     * Replaces value by the new value, leaving mapping unchanged
     */
    public Word as(CharSequence newValue) {
        String nv = newValue == null ? "" : newValue.toString();
        if (Objects.equals(this.value, nv))
            return this;
        if (nv.length() == 0)
            return new Word(source, start, end, nv, null);
        int[] p = new int[nv.length()];
        if (this.p == null) 
            Arrays.fill(p, this.source == null ? -1 : start);
        else { 
            System.arraycopy(this.p, 0, p, 0, Math.min(this.p.length, p.length));
            if (p.length > this.p.length) {
                int last = this.p.length - 1;
                Arrays.fill(p, last + 1, p.length, p[last]);
            }
        }
        return new Word(source, start, end, nv, p);
    }
    
    
    /**
     * Transforms the value applying function to value,
     * leaving mapping unchanged
     */
    public Word transform(StringTransformer transformer) {
        return as(transformer.apply(value));
    }
    
    
    /**
     * Transforms the value applying function to each character
     */
    public Word transform(CharTransformer transformer) {
        StringBuilder sb = new StringBuilder();
        for (char c : this.value.toCharArray())
            sb.append(transformer.apply(c));
        return as(sb.toString());
    }
    
    
    
    // ------------- private stuff -------------- //
    
    
    private static int lengthOf(String s) {
        return s == null ? 0 : s.length();
    }
    
    
    
    private static int[] naturalSeq(int start, int size) {
        if (size == 0)
            return null;
        int[] p = new int[size];
        for (int i = start; i < start + size; p[i - start] = i++);
        return p;
    }
    
    
    
    String checkAndGetCommonSource(Word w) throws IllegalArgumentException {
        if (this.source == w.source) 
            return this.source;
        if (this.source == null)     
            return w.source;
        if (w.source == null)        
            return this.source;
        if (this.source.equals(w.source))
            return this.source;
        else 
            throw new IllegalArgumentException("Joining words must have the same source");
    }
    
    
    
    private Word subword(int start, int end, boolean adjustStart, boolean adjustEnd) {
        start = Math.max(start, 0);
        end   = Math.min(end, value.length());
        int newStart = this.start;
        int newEnd   = this.end;
        int p[] = start == end ? null : Arrays.copyOfRange(this.p, start, end);
        if (adjustStart || adjustEnd) {
            if (p == null) {
                if (adjustStart && adjustEnd) {
                    return EMPTY;
                } else if (adjustStart)
                    newStart = this.end;
                else if (adjustEnd)
                    newEnd = this.start;
            } else {
                int min = p[0];
                int max = p[0];
                for (int i = 1; i < p.length; i++) {
                    if (p[i] >= 0) {
                        min = Math.min(min, p[i]);
                        max = Math.max(max, p[i]);
                    }
                }
                if (adjustStart) newStart = min;
                if (adjustEnd)   newEnd = max + 1;
            } 
        }
        return new Word(source, newStart, newEnd, value.substring(start, end), p);
    }
    
    
    
    
    // ----------- CharSequence methods ---------- //
    
    @Override
    public int length() { 
        return value == null ? 0 : value.length(); 
    }

    
    @Override
    public char charAt(int index) { return value.charAt(index); }
    
    
    @Override
    public CharSequence subSequence(int start, int end) { 
        if (value == null) 
            return null;
        start = Math.max(start, 0);
        end   = Math.min(end, value.length());
        return value.subSequence(start, end);
    }

    
    @Override
    public String toString() { 
        return value == null ? "" : value.toString(); 
    }
 
    
    // ------------------ comparators -------------------- //
        
    /**
     * Comparing words by their value lexicographically
     */
    public static final Comparator<Word> BY_VALUE 
        = (w1, w2) -> w1.value.compareTo(w2.value);
    
        
    /**
     * Comparing words by their position in source:
     * first by starting position, then by ending
     */
    public static final Comparator<Word> BY_SOURCE_POSITION
        = (w1, w2) -> w1.start == w2.start ? w1.end - w2.end : w1.start - w2.start;
    
        
    
    // -------- interfaces to use in transformers -------- //
    
    @FunctionalInterface 
    public interface StringTransformer { String apply(String input); }
    
    @FunctionalInterface
    public interface CharTransformer { char apply(char c); }
    
    @FunctionalInterface
    public interface StringFilter { boolean test(String input); }
    
}
