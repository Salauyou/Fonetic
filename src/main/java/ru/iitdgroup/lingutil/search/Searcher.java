package ru.iitdgroup.lingutil.search;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ru.iitdgroup.lingutil.collect.TrieMap;
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
     * Searches for occurrences of all patterns from dictinary in a text,
     * sending to consumer matches having score at least `minScore`
     */
    public <T> int search(CharSequence text, TrieMap<? extends T> dictionary, double minScore, 
                          BiConsumer<ScoredMatch, ? super T> matchConsumer);
    
    
    /**
     * Returns the score of how `pattern` is similar to `word`
     */
    public double getScore(CharSequence word, CharSequence pattern);
    
}
