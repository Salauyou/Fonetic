package lingutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ru.iitdgroup.lingutil.collect.LetterTrie;

public class TestLetterTrie {

    @Test
    public void testAddContains() {        
        LetterTrie t = new LetterTrie();
        
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
    
    
    
}
