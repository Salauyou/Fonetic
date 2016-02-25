package lingutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ru.iitdgroup.lingutil.collect.TrieMap;

public class TestTrieMap {

    // 6 unique + 2 repeats
    String[] keys = {"ABC", "A", "ABC", "BC", "ABCDE", "BC", "ABCD", "B"}; 
    
    
    @Test
    public void testPutSize() {

        TrieMap<String> t = new TrieMap<>();
        assertEquals(0, t.size());
        
        for (String s : keys) 
            t.put(s, s);
        
        assertEquals(6, t.size());
        
        t.put("", "EMPTY");       // empty string is also string
        assertEquals(7, t.size());
        
        
    }
    
    
    @Test
    public void testContainsGet() {
        TrieMap<String> t = new TrieMap<String>().put("", "EMPTY");
        for (String s : keys) 
            t.put(s, s);
        
        assertTrue(t.containsKey(""));
        assertFalse(t.containsKey("EMPTY"));
        assertEquals("EMPTY", t.get(""));
        for (String s : keys) {
            assertTrue(t.containsKey(s));
            assertEquals(s, t.get(s));
        }
                
        assertFalse(t.containsKey("BCD"));
        assertFalse(t.containsKey("BA"));
        assertNull(t.get("BCD"));
        assertNull(t.get("BA"));
    }
    
}
