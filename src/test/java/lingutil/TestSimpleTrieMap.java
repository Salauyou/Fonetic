package lingutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import ru.iitdgroup.lingutil.collect.SimpleTrieMap;
import ru.iitdgroup.lingutil.collect.TrieMap;

public class TestSimpleTrieMap {

    // 6 unique + 2 repeats
    List<String> keys = Arrays.asList("ABC", "A", "ABC", "BC", "ABCDE", "BC", "ABCD", "B"); 
    
    
    @Test
    public void testPutSize() {

        TrieMap<String> t = new SimpleTrieMap<>();
        assertEquals(0, t.size());
        
        for (String s : keys) 
            t.put(s, s);
        
        assertEquals(6, t.size());
        t.put("", "EMPTY");        // empty string is also a valid key
        assertEquals(7, t.size());
    }
    
    
    @Test
    public void testContainsGet() {
        TrieMap<String> t = new SimpleTrieMap<>(); 
        Collection<String> ks = new ArrayList<>(keys);
        ks.add("");
        for (String s : ks) 
            t.put(s, s);
        
        assertTrue(t.containsKey(""));
        assertFalse(t.containsKey("EMPTY"));
        assertEquals("", t.get(""));
        verifyContents(t, ks, null);
                
        assertFalse(t.containsKey("BCD"));
        assertFalse(t.containsKey("BA"));
        assertNull(t.get("BCD"));
        assertNull(t.get("BA"));
    }
    
    
    @Test
    public void testRemove() {
        TrieMap<String> t = new SimpleTrieMap<>();
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
            TrieMap<String> t = new SimpleTrieMap<>();
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
    
    
  
    
    @Test
    public void testMerge() {
        List<Pair<String, String>> amounts 
            = Arrays.asList(Pair.of("Bill",    "12.00"), 
                            Pair.of("Mary",     "8.45"), 
                            Pair.of("Bill",     "4.57"), 
                            Pair.of("Phillip", "10.10"), 
                            Pair.of("Bill",     "0.43"),
                            Pair.of("Mary",     "1.55"));
        
        TrieMap<BigDecimal> sums = new SimpleTrieMap<>();
        
        // let's summarize amounts using lambda
        for (Pair<String, String> a : amounts)            
            sums.merge(a.getKey(), new BigDecimal(a.getValue()), BigDecimal::add);

        assertEquals(3, sums.size());
        assertEquals(new BigDecimal("17.00"), sums.get("Bill"));
        assertEquals(new BigDecimal("10.10"), sums.get("Phillip"));
        assertEquals(new BigDecimal("10.00"), sums.get("Mary"));
    }
    
    
    

    @Test
    public void debug() {
        List<String> fullPrefixed 
            = Arrays.asList("APP", "APPLE", "APPDATA", "APPLE", "APPLES", "BE", "BEING", "BEHIND");
        List<String> splitPrefixed 
            = Arrays.asList("A", "APPLAUSE", "BEHI", "BURMISTER", "BURMA");
            
        SimpleTrieMap<String> t = new SimpleTrieMap<>();
        for (String s : fullPrefixed)
            t.put(s, s);
        verifyContents(t, fullPrefixed, splitPrefixed);
        
        for (String s : splitPrefixed)
            t.put(s, s);
        
        Collection<String> c = new ArrayList<>(fullPrefixed);
        c.addAll(splitPrefixed);
        verifyContents(t, c, null);
    }
    
    
    
    static void verifyContents(TrieMap<?> m, 
                               Collection<?> expected, 
                               Collection<?> notExpected) {
        assertEquals(new HashSet<>(expected).size(), m.size());
        if (expected != null) {
            for (Object k : expected) {
                assertTrue(m.containsKey(k));
                assertEquals(k, m.get(k));
            }
        }
        if (notExpected != null) {
            for (Object k : notExpected) {
                assertFalse(m.containsKey(k));
                assertNull(m.get(k));
            }
        }
    }
    

}
