package ru.iitdgroup.lingutil.search;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ru.iitdgroup.lingutil.collect.CharTrie;
import ru.iitdgroup.lingutil.match.ScoredMatch;


/**
 * Interface for different search algorithms
 */
public interface Searcher {

    
    /**
     * Searches for occurrences of a given pattern in a text,
     * sending to consumer matches having score at least `minScore`
     */
    public int search(CharSequence text, CharSequence pattern, double minScore, 
                      Consumer<ScoredMatch> matchConsumer);
    
    
    /**
     * Searches for occurrences in a text of patterns across entire dictionary,
     * sending to consumer matches having score at least `minScore`
     */
    public <T> int search(CharSequence text, CharTrie<? extends T> dictionary, 
                          double minScore, BiConsumer<ScoredMatch, ? super T> matchConsumer);
    
    
    /**
     * Returns the score of how `pattern` is similar to `word`
     */
    public double getScore(CharSequence word, CharSequence pattern);
    
}
