package demos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import ru.iitdgroup.lingutil.collect.LetterTrie;
import ru.iitdgroup.lingutil.text.Word;
import ru.iitdgroup.lingutil.text.Words;

public class DonaldVsHillary {

    
    static final String SPAN_DEMOCRATIC = "<span style=\"background: #08f; color: #fff\">%s</span>";
    static final String SPAN_REPUBLICAN = "<span style=\"background: #f00; color: #fff\">%s</span>";
    
    
    
    /**
     * Take html string and replace by asterisks all words 
     * except main 2016 candidate names
     */
    static String hillarize(String html) {
        
        // words we look for
        LetterTrie hillary = new LetterTrie().add("HILLARY").add("CLINTON");
        LetterTrie donald  = new LetterTrie().add("DONALD").add("TRUMP");
        
        // headline counters
        AtomicInteger h = new AtomicInteger(0);
        AtomicInteger d = new AtomicInteger(0);
                
        // extract words from document
        List<Word> words = new ArrayList<>();
        Words.split(html, Pattern.compile("\\s*<[^>]*>\\s*"))  // extract pieces between html tags
             .stream()                                         // (indeed, very wrong way to extract from html)
             .map(w -> w.transform(String::toUpperCase))    
             .map(w -> Words.splitIntoWords(w, true, "#&"))    // split each piece into separate words
             .forEach(words::addAll);
        
        // modify words: 
        // 1) candidates are surrounded by coloured <span>,
        // 2) other words are replaced by asterisks
        words = words
                .stream()
                .map(w -> {
                    if (hillary.contains(w)) {
                        h.incrementAndGet();
                        return w.as(SPAN_DEMOCRATIC.replace("%s", w));
                    } else if (donald.contains(w)) {
                        d.incrementAndGet();
                        return w.as(SPAN_REPUBLICAN.replace("%s", w));
                    } else {
                        return w.transform((char c) -> '*');
                    }
                })
                .collect(Collectors.toList());     

        System.out.format("Seems that %s gonna win: %s Hillary headlines vs %s Donald's\n", 
                           h.get() > d.get() ? "Clinton" : "Trump", 
                           h.get(), d.get());
        
        // put words into source at their initial positions
        return Words.applyToSource(words);
    }
    
    
    

    
    // ---------------------- main() ----------------------- //
    
    static final String WHAT_TO_READ  = "http://edition.cnn.com/politics/";
    static final String WHERE_TO_SAVE = "donaldVsHillary.html";
    
    
    public static void main(String[] args) {
        try (InputStream in = new URL(WHAT_TO_READ).openStream()) {
            String newHtml = hillarize(IOUtils.toString(in));
            FileUtils.writeStringToFile(new File(WHERE_TO_SAVE), newHtml);      
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
}
