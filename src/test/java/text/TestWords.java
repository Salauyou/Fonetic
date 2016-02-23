package text;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import ru.iitdgroup.lingutil.text.Word;
import ru.iitdgroup.lingutil.text.Words;



public class TestWords {

    static List<String> collectValues(List<Word> words) {
        return words.stream().map(Word::value).collect(Collectors.toList());
    } 
    
    static void assertValues(List<Word> words, String... actual) {
        assertEquals(Arrays.asList(actual), collectValues(words));
    }
    
    
    @Test
    public void testSplitToWords() {
        List<Word> words;
        String source = "I~have 12apples ,3-45ba-na-nas and12oranges, okay ? ";

        // simple split by alphanumeric chars sequences
        words = Words.splitIntoWords(source);
        assertValues(words, "I", "have", "12apples", "3", "45ba", "na", "nas", "and12oranges", "okay");
        
        // split disallowing mixed alphanumerical words
        words = Words.splitIntoWords(source, false);
        assertValues(words, "I", "have", "12", "apples", "3", "45", "ba", "na", "nas", "and", "12", "oranges", "okay");
        
        // split allowing some special chars inside words
        words = Words.splitIntoWords(source, false, "-");
        assertValues(words, "I", "have", "12", "apples", "3-45", "ba-na-nas", "and", "12", "oranges", "okay");
        
        // split allowing mixed words and some special chars
        words = Words.splitIntoWords(source, true, "?-=");
        assertValues(words, "I", "have", "12apples", "3-45ba-na-nas", "and12oranges", "okay", "?");
        
        // unicode mix
        source = " ~-- + ПлатёZH ++на сумму 100.00 RØÜBлей";
        words = Words.splitIntoWords(source, false, ".");
        assertValues(words, "ПлатёZH", "на", "сумму", "100.00", "RØÜBлей");
    }
    
    
    
    
    @Test
    public void testSplit() {
        List<Word> words;
        String source = "one, two;.three four, ;.five ,   six";
        
        words = Words.split(source, ' ');
        assertValues(words, "one,", "two;.three", "four,", ";.five", ",", "six");
        
        words = Words.split(source, ",;. ");
        assertValues(words, "one", "two", "three", "four", "five", "six");
        
        source = " ONE ::,TWO";
        assertValues(Words.split(source, " :,"), "ONE", "TWO");
        
        source = "ONE TWO   ,:";
        assertValues(Words.split(source, " :,"), "ONE", "TWO");
        
        
        source = "ONE TWO, THREE:, FOUR";
        Word w = Word.ofSubstring(source, 3, 15);
        assertEquals(" TWO, THREE:", w.value());
        
        words = Words.split(w, ' ');
        assertValues(words, "TWO,", "THREE:");
        assertEquals("TWO,", words.get(0).getMappedSubstring());
        assertEquals("THREE:", words.get(1).getMappedSubstring());
        
        words = Words.split(w, ",: ");
        assertValues(words, "TWO", "THREE");
        assertEquals("TWO", words.get(0).getMappedSubstring());
        assertEquals("THREE", words.get(1).getMappedSubstring());
    }
    
    @Test
    public void testJoin() {
        String s = "A B C";
        Word w1 = Word.ofSubstring(s, 0, 1);
        Word w2 = Word.ofSubstring(s, 2, 3);
        Word w3 = Word.ofSubstring(s, 4, 5);
        assertEquals("A B C", Words.join(" ", w1, w2, w3).value());
    }
    
    
}
