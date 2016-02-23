package ru.iitdgroup.lingutil.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Methods accepting `Word` object(s) always keep their source in returned result.
 * All methods are unicode-safe
 * 
 * @author Salauyou
 */
public final class Words {

    
    static public Word join(CharSequence joiner, Word... words) {
        return join(joiner, Arrays.asList(words));
    }
    
    
    static public Word join(CharSequence joiner, Iterable<Word> words) {
        Iterator<Word> i = words.iterator();
        if (!i.hasNext())
            return Word.EMPTY;
        Word w = i.next();
        String j = joiner.toString();
        while (i.hasNext()) 
            w = w.join(j).join(i.next());
        return w;
    }
    
    
    /**
     * Returns list where words stay in the order of their mapping
     * to the source
     */
    static public List<Word> orderAsInSource(Collection<Word> words) {
        throw new UnsupportedOperationException();
    }
    
    
    
    // ---------------- split by char(s) --------------- //
    
    /**
     * Splits source into non-empty words using provided 
     * char as separator
     */
    static public List<Word> split(String source, char splitChar) {
        return splitByChars(source, new char[]{ splitChar });
    }
    
    
    /**
     * Splits source into non-empty words using each 
     * of provided chars as separator
     */
    static public List<Word> split(String source, CharSequence splitChars) {
        return splitByChars(source, splitChars.toString().toCharArray());
    }
    
    
    /**
     * Splits a given word into non-empty words using
     * provided char as separator
     */
    static public List<Word> split(Word word, char splitChar) {
        return splitByChars(word, new char[]{ splitChar });
    } 
    
    
    /**
     * Splits a given word into non-empty words using each 
     * of provided chars as separator
     */
    static public List<Word> split(Word word, CharSequence splitChars) {
        return splitByChars(word, splitChars.toString().toCharArray());
    }
    
    
    
    static private List<Word> splitByChars(String source, char[] chars) {
        return splitByCharsPositions(source, chars).stream()
                .map(p -> Word.ofSubstring(source, p[0], p[1]))
                .collect(Collectors.toList());
    }
    
    
    static private List<Word> splitByChars(Word word, char[] chars) {
        return splitByCharsPositions(word.value, chars).stream()
                .map(p -> word.crop(p[0], p[1]))
                .collect(Collectors.toList());
    }
    
    
    static private List<int[]> splitByCharsPositions(String source, char[] chars) {
        Arrays.sort(chars);
        List<int[]> res = new ArrayList<>();
        char[] ch = source.toCharArray();
        int start = -1;
        for (int i = 0; i < ch.length; i++) {
            if (Arrays.binarySearch(chars, ch[i]) < 0) 
                start = start < 0 ? i : start;
            else {
                if (start >= 0) {
                    res.add(new int[]{ start, i });
                    start = -1;
                }
            }
        }
        if (start >= 0)
            res.add(new int[]{ start, ch.length });
        return res;
    }
    
    
    
    // ---------------- split by regex ----------------- //
    
    /**
     * Similar to {@link String#split(String)}, 
     * but regex is accepted as {@link Pattern}
     */
    static public List<Word> split(Word word, Pattern regex) {
        throw new UnsupportedOperationException();
    }
   
    
    /**
     * Similar to {@link String#split(String)}, 
     * but regex is accepted as {@link Pattern}
     */
    static public List<Word> split(String source, Pattern regex) {
        throw new UnsupportedOperationException();
    }
    
    
    
    // --------------- extract using regex --------------- //
    
    /**
     * Extract words using regex 
     */
    static public List<Word> extract(Word word, Pattern regex) {
        throw new UnsupportedOperationException();
    }
    
    
    
    
    
    // ------------- split by natural word boundaries ------------- //
    
    /**
     * Splits a given string into words by natural word boundaries,
     * allowing words containing mixed alphabetic and numeric chars
     * <p>
     * See {@link #splitIntoWords(String, boolean, CharSequence)}
     */
    static public List<Word> splitIntoWords(String source) {
        return splitIntoWords(source, true);
    }
   
    
    /**
     * Splits a given string into words by natural word boundaries,
     * with control of can a word contain mixed chars or not
     * <p>
     * See {@link #splitIntoWords(String, boolean, CharSequence)}
     */
    static public List<Word> splitIntoWords(String source, boolean allowMixed) {
        return splitIntoWords(source, allowMixed, "");
    }
     
    
    /**
     * Splits a given string into list of non-empty words by natural word boundaries
     * (positions where alphanumeric char is adjacent to non-alphanumeric), 
     * setting provided string as source of each word
     * 
     * @param source       source string
     * @param allowMixed   whether a word can contain mixed alphabetic and numeric chars
     * @param extraChars   chars that, in addition to alphanumeric, words can contain
     */
    static public List<Word> splitIntoWords(String source, boolean allowMixed, 
                                            CharSequence extraChars) {
        char[] extra = extraChars.toString().toCharArray();
        return splitIntoWordPositions(source, allowMixed, extra)
                .stream()
                .map(p -> Word.ofSubstring(source, p[0], p[1]))
                .collect(Collectors.toList());
    }
    
    
    /**
     * Analogue of {@link Words#splitIntoWords(String)} applied on word
     */
    static public List<Word> splitIntoWords(Word word) {
        return splitIntoWords(word, true);
    }
   
    
    /**
     * Analogue of {@link Words#splitIntoWords(String, boolean)}
     * applied on word
     */
    static public List<Word> splitIntoWords(Word word, boolean allowMixed) {
        return splitIntoWords(word, allowMixed, "");
    }
     
    
    /**
     * Analogue of {@link Words#splitIntoWords(String, boolean, CharSequence)}
     * applied on word
     */
    static public List<Word> splitIntoWords(Word word, boolean allowMixed, 
                                            CharSequence extraChars) {
        char[] extra = extraChars.toString().toCharArray();
        return splitIntoWordPositions(word.value(), allowMixed, extra)
                .stream()
                .map(p -> word.crop(p[0], p[1]))
                .collect(Collectors.toList());
    }
    
    
    
    private static List<int[]> splitIntoWordPositions(String source, boolean allowMixed, char[] extra) {
        Arrays.sort(extra);
        List<int[]> res = new ArrayList<>();
        int start = -1;
        boolean isPredDigit = false;
        char[] ch = source.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            int c = (int) ch[i];
            if (Character.isLetterOrDigit(c) || Arrays.binarySearch(extra, ch[i]) >= 0) {
                if (start < 0) 
                    start = i;
                else if (!allowMixed) {
                    if (isPredDigit && Character.isLetter(c)
                        || !isPredDigit && Character.isDigit(c)) {
                        res.add(new int[]{ start, i });
                        start = i;
                    }
                }
                if (Character.isLetterOrDigit(c))
                    isPredDigit = Character.isDigit(c);
            } else if (start >= 0) {
                res.add(new int[]{ start, i });
                start = -1;
            }
        }
        if (start >= 0)
            res.add(new int[] { start, ch.length });
        return res;
    }
    
    
    
    // prevent instantiation
    private Words() { }

}
