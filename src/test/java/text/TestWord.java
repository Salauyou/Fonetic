package text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import ru.iitdgroup.lingutil.text.Word;
import ru.iitdgroup.lingutil.text.Words;


public class TestWord {

    @Test
    public void testNew() {
        Word w = Word.of("");
        assertEquals(0, w.length());
        assertEquals("", w.getMappedSubstring());
        
        w = Word.of("ABC");
        assertEquals(3, w.length());
        assertEquals("ABC", w.value());
        assertEquals("ABC", w.getMappedSubstring());
        
        w = Word.of(null);
        assertEquals(0, w.length());
        assertEquals("", w.value());
        assertNull(w.getMappedSubstring());
        
        w = Word.ofSubstring("ABC", 0, 10);
        assertEquals(3, w.length());
        assertEquals("ABC", w.getMappedSubstring());
        
        w = Word.ofSubstring(null, -5, 5);
        assertEquals(0, w.length());
        assertNull(w.getMappedSubstring());
    }
    
    
    
    @Test
    public void testCrop() {
        Word w = Word.of("ABCDE");
        w = w.crop();
        assertEquals(5, w.length());
        assertEquals("ABCDE", w.value());
        assertEquals("ABCDE", w.getMappedSubstring());
        
        w = w.crop(1, 4); 
        assertEquals(3, w.length());
        assertEquals("BCD", w.value());
        assertEquals("BCD", w.getMappedSubstring());
        
        w = w.crop().crop().crop();
        assertEquals(3, w.length());
        assertEquals("BCD", w.value());
        assertEquals("BCD", w.getMappedSubstring());
        
        // out of bounds
        w = w.crop(-5, 3);
        assertEquals(3, w.length());
        assertEquals("BCD", w.value());
        assertEquals("BCD", w.getMappedSubstring());
        
        w = w.crop(0, 100);
        assertEquals(3, w.length());
        assertEquals("BCD", w.value());
        assertEquals("BCD", w.getMappedSubstring());
        
        w = w.crop(2, 2);
        assertEquals(0, w.length());
        assertEquals("", w.value());
        assertNull(w.getMappedSubstring());
    }
    
    
    
    @Test
    public void testCutToCutHead() {
        Word w = Word.ofSubstring("012ABCDE890", 3, 8);
        assertEquals(5, w.length());
        assertEquals("ABCDE", w.value());
        assertEquals("ABCDE", w.getMappedSubstring());
        
        w = w.cutTo(3);
        assertEquals("ABC", w.value());
        assertEquals("ABC", w.getMappedSubstring());
        
        w = w.cutHead(2);
        assertEquals(1, w.length());
        assertEquals("C", w.value());
        assertEquals("C", w.getMappedSubstring());
    }
    
    
    
    @Test
    public void testAs() {
        Word w = Word.of("abc");
        w = w.as("ABC");
        assertEquals("ABC", w.value());
        assertEquals("abc", w.getMappedSubstring());
        
        w = Word.ofSubstring("012abcde890", 3, 8);
        w = w.as("ABCDE");
        assertEquals("ABCDE", w.value());
        assertEquals("abcde", w.getMappedSubstring());
        
        // shrink value then expand -- must return the same mapping
        w = w.as("123").as("qwert");
        assertEquals("qwert", w.value());
        assertEquals("abcde", w.getMappedSubstring());
        
        w = w.as("");
        assertEquals("", w.value());
        assertEquals("abcde", w.getMappedSubstring());
        
        // after crop(), empty-value word should lose its source
        assertNull(w.crop().getMappedSubstring());
    }
    
    
    
    @Test
    public void testTransform() {
        Word w = Word.ofSubstring("one two three", 4, 7);
        w = w.transform(String::toUpperCase);
        assertEquals("TWO", w.value());
        assertEquals("two", w.getMappedSubstring());
        
        w = w.transform(Character::toLowerCase);
        w = w.crop();
        assertEquals("two", w.value());
        assertEquals("two", w.getMappedSubstring());
        
        // use case: remove diacritical marks
        String dirty = "Et ça sera sa moitié";
        String clean = "Et ca sera sa moitie";
        w = Word.of(dirty).transform(StringUtils::stripAccents);
        assertEquals(clean, w.value());
        assertEquals(dirty, w.getMappedSubstring());
    }
    
    
    
    @Test
    public void testCutMiddle() {
        Word w = Word.of("ONE TWO THREE");
        w = w.cutMiddle(3, 8);
        assertEquals("ONETHREE", w.value());
        assertEquals("ONE TWO THREE", w.getMappedSubstring());
        
        w = w.crop();
        assertEquals("ONETHREE", w.value());
        assertEquals("ONE TWO THREE", w.getMappedSubstring());
        
        w = w.cutMiddle(0, 3);
        assertEquals("THREE", w.value());
        assertEquals("ONE TWO THREE", w.getMappedSubstring());
        
        w = w.crop();
        assertEquals("THREE", w.getMappedSubstring());
        
        w = w.cutMiddle(0, 2);
        assertEquals("REE", w.value());
        assertEquals("THREE", w.getMappedSubstring());
        
        w = w.crop();
        assertEquals("REE", w.getMappedSubstring());   
        
        w = w.cutMiddle(2, 3);
        assertEquals("RE", w.value());
        assertEquals("REE", w.getMappedSubstring());
        
        w = w.crop();
        assertEquals("RE", w.getMappedSubstring());
        
        // out of bounds
        w = Word.of("ABCDE").cutMiddle(-2, 0);
        assertEquals("ABCDE", w.value());
        assertEquals("ABCDE", w.getMappedSubstring());
        w = w.crop();
        assertEquals("ABCDE", w.getMappedSubstring());
        
        w = w.cutMiddle(4, 100);
        assertEquals("ABCD", w.value());
        assertEquals("ABCDE", w.getMappedSubstring());
        w = w.crop();
        assertEquals("ABCD", w.getMappedSubstring());
        
        w = w.cutMiddle(100, 200);
        assertEquals("ABCD", w.value());
        assertEquals("ABCD", w.getMappedSubstring());
    }
    
    
    
    @Test
    public void testJoin() {
        String source = "ONE TWO THREE FOUR";
        Word two  = Word.ofSubstring(source, 4, 7);    // TWO
        Word four = Word.ofSubstring(source, 14, 18);  // FOUR
        
        Word w = two.join(four);
        assertEquals("TWOFOUR", w.value());
        assertEquals("TWO THREE FOUR", w.getMappedSubstring());
        
        w = two.join(Word.EMPTY.as(" ")).join(four);
        assertEquals("TWO FOUR", w.value());
        assertEquals("TWO THREE FOUR", w.getMappedSubstring());
        w = w.crop();
        assertEquals("TWO THREE FOUR", w.getMappedSubstring());
        
        w = w.cutTo(3);
        assertEquals("TWO", w.value());
        assertEquals("TWO", w.getMappedSubstring());
        
        Word empty = Word.EMPTY.join(Word.EMPTY).crop().join(Word.EMPTY);
        assertTrue(empty == Word.EMPTY);
        assertEquals("", empty.value());
        assertNull(empty.getMappedSubstring());
        
        w = empty.join(two).join(empty).crop();
        assertEquals("TWO", w.value());
        assertEquals("TWO", w.getMappedSubstring());
        
        w = four.join(empty.as(" ")).join(two);
        assertEquals("FOUR TWO", w.value());
        assertEquals("TWO THREE FOUR", w.getMappedSubstring());
        
        w = four.join(" ");
        assertEquals("FOUR ", w.value());
        assertEquals("FOUR", w.getMappedSubstring());
        
        w = w.join(" ").join(two);
        assertEquals("FOUR  TWO", w.value());
        assertEquals("TWO THREE FOUR", w.getMappedSubstring());
        w = w.crop();
        assertEquals("TWO THREE FOUR", w.getMappedSubstring());
        
        w = two.join(" ").join(two, empty.as(" "), four, empty.as(" "), two);
        assertEquals("TWO TWO FOUR TWO", w.value());
        assertEquals("TWO THREE FOUR", w.getMappedSubstring());
        
        w = w.crop();
        assertEquals("TWO THREE FOUR", w.getMappedSubstring());
        
        w = w.crop(4, 7); // TWO
        assertEquals("TWO", w.value());
        assertEquals("TWO", w.getMappedSubstring());
        
    }
    
    
    
    @Test
    public void testCharSequence() {
        Word w = Word.ofSubstring("ONE TWO THREE", 4, 7);
        assertTrue(StringUtils.endsWith(w, "WO"));
        assertTrue(StringUtils.startsWith(w, "TW"));
        assertEquals(1, StringUtils.getLevenshteinDistance(w, "TO"));
    }
    
    
    @Test
    public void testComparators() {
        List<Word> ws = Words.splitIntoWords("ZERO ONE TWO THREE");
        ws.sort(Word.BY_VALUE);
        assertEquals("ONE",   ws.get(0).value());
        assertEquals("THREE", ws.get(1).value());
        assertEquals("TWO",   ws.get(2).value());
        assertEquals("ZERO",  ws.get(3).value());
        
        ws.add(ws.get(0).join(ws.get(2)));    // ONETWO
        ws.sort(Word.BY_SOURCE_POSITION);
        assertEquals("ZERO",   ws.get(0).value());
        assertEquals("ONE",    ws.get(1).value());
        assertEquals("ONETWO", ws.get(2).value());
        assertEquals("TWO",    ws.get(3).value());
        assertEquals("THREE",  ws.get(4).value());
    }
    
}
