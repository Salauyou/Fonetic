package lingutil;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import ru.iitdgroup.lingutil.match.ScoredMatch;
import ru.iitdgroup.lingutil.search.FoneticSearch;


public class TestFoneticSearch {

    @Test
    public void testFoneticSearch() {
        
        FoneticSearch fs = new FoneticSearch();
        fs.findOccurrences("FILLQPHILEFILTTT", "PHILQ", 10);
        String input = "OBSHESTVOSORGONICHENOYOTVETSTVENNOSTYU";
        String word = "OGRANITCHENNOJ";
        List<ScoredMatch> res = fs.findOccurrences(input, word, 10);
        assertEquals(1, res.size());
        assertEquals("ORGONICHENOY", input.subSequence(res.get(0).start, res.get(0).end));
    
        assertFalse(fs.findOccurrences("MUSTAFA", "MOUSTAPHA", 10).isEmpty());  // digraphs in input and word
        assertFalse(fs.findOccurrences("PIZZA", "PITSA", 10).isEmpty());        // digraph intersection (ZZ -> C, TS -> C)
        assertFalse(fs.findOccurrences("PIZZA", "PInCA", 10).isEmpty());        // deletion before digraph
        
        assertFalse(fs.findOccurrences("PVIKZZA", "PITSNA", 10).isEmpty());
        assertTrue(fs.findOccurrences("PIVKZZA", "PITSA", 10).isEmpty());
        assertTrue(fs.findOccurrences("PIVKZZA", "PIZZA", 10).isEmpty());
        
        assertTrue(fs.findOccurrences("PInZZA", "PICA", 10).isEmpty());         // insertion before digraph--won't find
    }
    
    
}
