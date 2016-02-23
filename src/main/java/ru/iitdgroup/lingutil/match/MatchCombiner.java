package ru.iitdgroup.lingutil.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntToDoubleFunction;

import com.google.common.collect.Multimap;


/**
 * @author Salauyou
 */
public class MatchCombiner {

    
    /**
     * Finds best, in terms of score sum, combined match where each word occurs 
     * at most once and doesn't overlap with any other
     * @param wordMatches  - matches of words
     * @param distanceCoef - multiplication coefficient applied to score of 
     *                       resulting combined submatch, accepting  
     *                       distance (in chars) between last two words
     */
    static public Seq findBestCombinedMatch(Multimap<Integer, ScoredMatch> wordMatches, 
                                            IntToDoubleFunction distanceCoef) {
        List<Seq> words = new ArrayList<>();
        wordMatches.entries().forEach(e -> words.add(Seq.of(e.getKey(), e.getValue())));
        Collections.sort(words, Comparator.comparing(Seq::effectiveStart));
        Seq best = null;
        List<Seq> seqs = new ArrayList<>();
        for (Seq w : words) {
            int k = seqs.size();
            for (int i = 0; i < k; i++) {
                Seq s  = seqs.get(i);
                Seq ns = s.append(w, distanceCoef.applyAsDouble(w.start - s.end));   // continued sequence
                if (ns != null) {
                    seqs.add(ns);
                    if (best == null || best.score < ns.score)
                        best = ns;
                }
            }
            if (!w.singleWord)
                throw new IllegalArgumentException();
            seqs.add(w);
            if (best == null || best.score < w.score)
                best = w;
        }
        return best;
    }
    

    
    static public final class Seq {
        private final Seq pred;
        private final int wordFlags;
        private final int wordNumber;
        private final double coef;
        
        public final int start, end;
        public final double score;
        public final boolean singleWord;
       
        
        private Seq(int wordNumber, int start, int end, double score) {
            if (wordNumber > 31 || wordNumber < 0)
                throw new IllegalArgumentException();
            pred = null;
            this.start = start;
            this.end = end;
            wordFlags = 1 << wordNumber;
            this.score = score;
            this.wordNumber = wordNumber;
            this.singleWord = true;
            this.coef = 1.0;
        }
        
        
        private Seq(Seq current, Seq appended, double scoreCoef) {
            if (!appended.singleWord)
                throw new IllegalStateException();
            this.pred = current;
            this.start = appended.start;
            this.end = appended.end;
            this.wordFlags = current.wordFlags | (1 << appended.wordNumber);            
            this.score = (current.score + appended.score * current.coef) * scoreCoef;
            this.coef = current.coef * scoreCoef;
            this.wordNumber = appended.wordNumber;
            this.singleWord = false;
        }
        
        
        public static Seq of(int wordNumber, int start, int end, double score) {
            return new Seq(wordNumber, start, end, score);
        }
        
        
        public static Seq of(int wordNumber, ScoredMatch m) {
            return new Seq(wordNumber, m.start, m.end, m.score);
        }
        
        
        public boolean canAppend(Seq s) {
            return s.singleWord && this.end <= s.start
                   && (this.wordFlags | 1 << s.wordNumber) != this.wordFlags;     
        }
        
        
        public Seq append(Seq s, double scoreCoef) {
            return canAppend(s) ? new Seq(this, s, scoreCoef) : null;
        }
        
        
        public int effectiveStart() {
            Seq s = this;
            while (s.pred != null && (s = s.pred) != null);
            return s.start;
        }
        
        
        /**
         * Number of subs in this seq
         */
        public int size() {
            int i = 1;
            Seq s = this;
            while ((s = s.pred) != null && i++ > 0);
            return i;
        }
        
        
        @Override
        public String toString() {
            Seq s = this;
            String ss = "";
            while (s != null) {
                ss = String.format("%s(%s…%s), ", s.wordNumber, s.start, s.end) + ss;
                s = s.pred;
            }
            return String.format("Seq: %s score=%.2f", ss.substring(0, ss.length() - 2), this.score);
        }
    }

    
}
