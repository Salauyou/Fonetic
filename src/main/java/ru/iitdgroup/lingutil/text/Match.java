package ru.iitdgroup.lingutil.text;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import ru.iitdgroup.util.StringHelper;




/** 
 * @Immutable
 * @param <T> match target type
 */
public final class Match<T> {

    final String source;
    final int start;
    final int end;
    final T target;
    final double score;
    
    
    public String  getSource()  { return source; }
    public int     getStart()   { return start; }
    public int     getEnd()     { return end; }
    public T       getTarget()  { return target; }
    public double  getScore()   { return score; }
    
    
    private Match(String source, int start, int end, T target, double score) {
        this.source = source;
        this.start = start;
        this.end = end;
        this.target = target;
        this.score = score;
    }
    
    
    public String getMatchedText() {
        return StringUtils.substring(source, start, end);
    }
    
    
    
    // ------------- factory creation methods -------------- //
    
    
    public static <T> Match<T> fullMatch(T target, CharSequence source) {
        if (source instanceof Word) {
            Word w = (Word) source;
            return new Match<T>(w.source, w.start, w.end, target, 1.0); 
        } else {
            String s = source.toString();
            return new Match<T>(s, 0, s.length(), target, 1.0);
        }
    }
    
    
    public static <T> Match<T> atWord(T target, double score, Word word) {
        return new Match<T>(word.source, word.start, word.end, target, score); 
    }
    
    
    public static <T> Match<T> atWords(T target, double score, Collection<Word> words) {
        Word word = words.iterator().next();
        String s = word.source;
        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        for (Word w : words) {
            if (w.source == null)
                continue;
            s = word.checkAndGetCommonSource(w);
            word = w;
            if (w.source != null) {
                start = Math.min(start, w.start);
                end = Math.max(end, w.end);
            }
        }
        return s == null 
                 ? new Match<T>(null, 0, 0, target, score) 
                 : new Match<T>(s, start, end, target, score);
    }
    
    
    public static <T> Match<T> atWords(T target, double score, Word... words) {
        return atWords(target, score, Arrays.asList(words));
    }
  
    
    
    @Override
    public String toString() {
        int chars = 9;
        String s = getSource() == null ? "" : getSource();
        int start = getStart();
        int end = getEnd();
        StringBuilder sb = new StringBuilder();
        if (start > chars)
            sb.append('…');
        sb.append(s.substring(Math.max(0, start - chars), start))
          .append("[")
          .append(s.substring(start, end))
          .append("]")
          .append(s.substring(end, Math.min(s.length(), end + chars)));
        if (s.length() - end > chars)
            sb.append('…');
        return String.format(" Matched: %s\n  Target: %s\n   Score: %.2f", 
                    StringHelper.flatify(sb.toString()), 
                    Objects.toString(target), score);
    }
    
    
}
