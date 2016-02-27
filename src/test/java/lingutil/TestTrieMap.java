package lingutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import ru.iitdgroup.lingutil.collect.TrieMap;

public class TestTrieMap {

    // 6 unique + 2 repeats
    String[] keys = {"ABC", "A", "ABC", "BC", "ABCDE", "BC", "ABCD", "B"}; 
    
    
    @Test
    public void testPutSize() {

        TrieMap<String> t = new TrieMap<>();
        assertEquals(0, t.size());
        assertEquals(0, t.nodeCount());
        
        for (String s : keys) 
            t.put(s, s);
        
        assertEquals(6, t.size());
        assertEquals(7, t.nodeCount());
        
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
        
        t.getRoot().children().next().getValue().children().next();
        
    }
    
    
    
    @Test
    public void testPutWithFunction() {
        List<Pair<String, String>> amounts 
            = Arrays.asList(Pair.of("Bill",    "12.00"), 
                            Pair.of("Mary",     "8.45"), 
                            Pair.of("Bill",     "4.57"), 
                            Pair.of("Phillip", "10.10"), 
                            Pair.of("Bill",     "0.43"),
                            Pair.of("Mary",     "1.55"));
        
        TrieMap<BigDecimal> sums = new TrieMap<>();
        
        // let's summarize amounts using lambda
        for (Pair<String, String> a : amounts)            
            sums.merge(a.getKey(), new BigDecimal(a.getValue()), BigDecimal::add);

        assertEquals(3, sums.size());
        assertEquals(new BigDecimal("17.00"), sums.get("Bill"));
        assertEquals(new BigDecimal("10.10"), sums.get("Phillip"));
        assertEquals(new BigDecimal("10.00"), sums.get("Mary"));
    }
    
}
