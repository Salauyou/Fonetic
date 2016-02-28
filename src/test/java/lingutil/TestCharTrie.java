package lingutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import ru.iitdgroup.lingutil.collect.CharTrie;

public class TestCharTrie {

    // 6 unique + 2 repeats
    List<String> keys = Arrays.asList("ABC", "A", "ABC", "BC", "ABCDE", "BC", "ABCD", "B"); 
    
    
    @Test
    public void testPutSize() {

        CharTrie<String> t = new CharTrie<>();
        assertEquals(0, t.size());
        assertEquals(0, t.nodeCount());
        
        for (String s : keys) 
            t.put(s, s);
        
        assertEquals(6, t.size());
//        assertEquals(7, t.nodeCount());
        
        t.put("", "EMPTY");       // empty string is also string
        assertEquals(7, t.size());
        
        
    }
    
    
    @Test
    public void testContainsGet() {
        CharTrie<String> t = new CharTrie<String>().put("", "EMPTY");
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
    public void testRemove() {
        CharTrie<String> t = new CharTrie<>();
        for (String s : keys)
            t.put(s, s);
        
        assertEquals(6, t.size());
        assertTrue(t.containsKey("ABC"));
        t.remove("ABC");
        assertTrue(t.containsKey("A"));
        assertTrue(t.containsKey("ABCD"));
        assertTrue(t.containsKey("ABCDE"));
        
        t.remove("ABCDE");
        assertFalse(t.containsKey("ABCDE"));
        assertFalse(t.containsKey("ABC"));
        assertTrue(t.containsKey("A"));
        
        t.remove("A");
        assertFalse(t.containsKey("A"));
        assertTrue(t.containsKey("BC"));
        assertTrue(t.containsKey("ABCD"));
        
        t.remove("ABCD");
        assertFalse(t.containsKey("A"));
        assertFalse(t.containsKey("ABCD"));
    }
    
    
    
    @Test
    public void testCombined() {
        final List<String> keys 
            = Arrays.asList("ABC", "A", "ABC", "BC", "ABCDE", "AF", "ABCDEF",
                            "BCF", "ABCE", "AAA", "BB", "C", "EC", "BDB", "ABCDFF",
                            "BC", "ABCD", "CD", "ABDAB", "E", "AA", "", "B", "BFG",
                            "CA", "CB", "CAB", "CBA", "CBAF", "CFE", "DEF");
        final Random rnd = new Random();
        for (int i = 0; i < 100; i++) {
            Set<String> existing = new HashSet<>(keys);
            Set<String> removed  = new HashSet<>();
            CharTrie<String> t = new CharTrie<>();
            for (String s : keys)
                t.put(s, s);
            assertEquals(existing.size(), t.size());
            for (int j = 0; j < 1000; j++) {
                
                // remove random key
                String s = keys.get(rnd.nextInt(keys.size()));
                existing.remove(s);
                removed.add(s);
                t.remove(s);
                verifyContents(t, existing, removed);
                
                // put random key
                s = keys.get(rnd.nextInt(keys.size()));
                existing.add(s);
                removed.remove(s);
                t.put(s, s);
                verifyContents(t, existing, removed);
            }
        }
    }
    
    
    static void verifyContents(CharTrie<String> m, 
                              Collection<String> expected, 
                              Collection<String> notExpected) {
        assertEquals(expected.size(), m.size());
        for (String s : expected) {
            assertTrue(m.containsKey(s));
            assertEquals(s, m.get(s));
        }
        for (String s : notExpected) {
            assertFalse(m.containsKey(s));
            assertNull(m.get(s));
        }
    }
    
    
    
    @Test
    public void testMerge() {
        List<Pair<String, String>> amounts 
            = Arrays.asList(Pair.of("Bill",    "12.00"), 
                            Pair.of("Mary",     "8.45"), 
                            Pair.of("Bill",     "4.57"), 
                            Pair.of("Phillip", "10.10"), 
                            Pair.of("Bill",     "0.43"),
                            Pair.of("Mary",     "1.55"));
        
        CharTrie<BigDecimal> sums = new CharTrie<>();
        
        // let's summarize amounts using lambda
        for (Pair<String, String> a : amounts)            
            sums.merge(a.getKey(), new BigDecimal(a.getValue()), BigDecimal::add);

        assertEquals(3, sums.size());
        assertEquals(new BigDecimal("17.00"), sums.get("Bill"));
        assertEquals(new BigDecimal("10.10"), sums.get("Phillip"));
        assertEquals(new BigDecimal("10.00"), sums.get("Mary"));
    }
    
}
