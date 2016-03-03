package lingutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ru.iitdgroup.lingutil.collect.SimpleTrieMap;

public class TestSimpleTrieMap {

    @Test
    public void debug() {
        List<String> fullPrefixed 
            = Arrays.asList("APP", "APPLE", "APPDATA", "APPLE", "APPLES", "BE", "BEING", "BEHIND");
        List<String> splitPrefixed 
            = Arrays.asList("A", "APPLAUSE", "BEHI", "BURMISTER", "BURMA");
            
        SimpleTrieMap<String> t = new SimpleTrieMap<>();
        for (String s : fullPrefixed)
            t.put(s, s);
        assertContainsAll(t, fullPrefixed);
        assertContainsNone(t, splitPrefixed);
        
        for (String s : splitPrefixed)
            t.put(s, s);
        assertContainsAll(t, fullPrefixed);
        assertContainsAll(t, splitPrefixed);
        
    }
    
    
    static void assertContainsAll(Map<?, ?> map, List<?> keys) {
        for (Object k : keys) {
            assertTrue(map.containsKey(k));
            assertEquals(k, map.get(k));
        }
    }
    
    
    static void assertContainsNone(Map<?, ?> map, List<?> keys) {
        for (Object k : keys) {
            assertFalse(map.containsKey(k));
            assertNull(map.get(k));
        }
    }
    
}
