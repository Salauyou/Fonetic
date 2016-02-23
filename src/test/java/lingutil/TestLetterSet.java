package lingutil;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ru.iitdgroup.lingutil.collect.LetterSet;



public class TestLetterSet {

    @Test
    public void testSizeContains() {
        LetterSet s = LetterSet.of("");
        assertEquals(0, s.size());
        assertFalse(s.contains('a'));
        assertFalse(s.contains('0'));
        assertFalse(s.contains(' '));
        
        s = LetterSet.of(null);
        assertEquals(0, s.size());
        
        s = LetterSet.of("09AZaz");
        assertEquals(6, s.size());
        for (char c : "09AZaz".toCharArray())
            assertTrue(s.contains(c));
        for (char c : "2345678qwertyuiopsdfghjklxcvbnmQWERTYUIOPSDFGHJKLXCVBNM".toCharArray()) 
            assertFalse(s.contains(c));
        
        s = LetterSet.of('D');
        assertEquals(1, s.size());
        assertTrue(s.contains('D'));
        assertFalse(s.contains('d'));
        assertFalse(s.contains('='));
        
        s = LetterSet.of("QWERTYUIOPASDFGHJKLZXCVBNM");
        assertEquals(26, s.size());
        assertTrue (s.contains('A'));
        assertTrue (s.contains('Z'));
        assertFalse(s.contains('a'));
        assertFalse(s.contains('z'));
        assertFalse(s.contains('0'));
        assertFalse(s.contains('9'));
    }
    
    
    @Test
    public void testIntersect() {
        LetterSet s1 = LetterSet.of("");
        LetterSet s2 = LetterSet.of("");
        assertFalse(s1.intersect(s1));
        
        s2 = LetterSet.of('A');
        assertFalse(s1.intersect(s2));
        
        s1 = LetterSet.of('A');
        assertTrue(s1.intersect(s2));
        
        s1 = LetterSet.of("1590");
        s2 = LetterSet.of("2487");
        assertFalse(s1.intersect(s2));
        
        s2 = LetterSet.of("9847");
        assertTrue(s1.intersect(s2));
        
        s2 = LetterSet.of("1592");
        assertTrue(s1.intersect(s2));
        
        s1 = LetterSet.of("QWERTYUIOPASDFGHJKLZXCVBNM");
        s2 = LetterSet.of("qwertyuiopasdfghjklzxcvbnm");
        assertFalse(s1.intersect(s2));
        
        s2 = LetterSet.of("");
        assertFalse(s1.intersect(s2));
        
        s2 = LetterSet.of('C');
        assertTrue(s1.intersect(s2));
    }
    
    
    @Test
    public void testIterable() {
        LetterSet s = LetterSet.EMPTY;
        assertFalse(s.iterator().hasNext());
        
        s = LetterSet.of("JAVA");
        Set<Character> set = new HashSet<>();
        for (Character c : s)
            set.add(c);
        assertEquals(new HashSet<Character>(Arrays.asList('J', 'A', 'V')), set);
    }
    
}
