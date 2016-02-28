package lingutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;


import ru.iitdgroup.lingutil.collect.CharTrieSet;

public class TestCharTrieSet {

    @Test
    public void testAddContains() {        
        CharTrieSet t = new CharTrieSet();
        
        t.add("ABC");
        t.add("AB");
        t.add("A");        
        assertEquals(3, t.nodeCount());
        assertEquals(3, t.size());
        assertTrue(t.contains("AB"));
        assertFalse(t.contains("B"));
        assertFalse(t.contains("ABCD"));
        assertFalse(t.contains("ABD"));
    
        t.add("BC");
        t.add("B");
        assertEquals(5, t.size());
        assertEquals(5, t.nodeCount());
        
        t.add("ABCD");
        assertEquals(6, t.size());
        assertEquals(6, t.nodeCount());
        assertTrue(t.contains("ABCD"));
        assertTrue(t.contains("AB"));
        assertFalse(t.contains("ABCDE"));
        
        t.add("ACD");
        assertEquals(7, t.size());
        assertEquals(8, t.nodeCount());
        assertTrue(t.contains("ACD"));
    }
    
    
    @Test
    public void testIterator() {
        CharTrieSet t = new CharTrieSet().add("ONE").add("TWO").add("THREE").add("FOUR");
        assertEquals(Arrays.asList("FOUR", "ONE", "THREE", "TWO"), 
                t.stream().map(CharSequence::toString).collect(Collectors.toList()));
    }
    
    
}
