package lingutil;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ru.iitdgroup.lingutil.collect.CharTrieMap;

public class TestCharTrieMap {

    @Test
    public void debug() {
        List<String> fullPrefixed 
            = Arrays.asList("APP", "APPLE", "APPDATA", "APPLE", "APPLES", "BE", "BEING", "BEHIND");
        List<String> splitPrefixed 
            = Arrays.asList("A", "APPLAUSE", "BEHI", "BURMISTER", "BURMA");
            
        CharTrieMap<String> t = new CharTrieMap<>();
        for (String s : fullPrefixed)
            t.put(s, s);
        assertContainsAll(t, fullPrefixed);
        assertContainsNone(t, splitPrefixed);
        
        for (String s : splitPrefixed)
            t.put(s, s);
        assertContainsAll(t, fullPrefixed);
        assertContainsAll(t, splitPrefixed);
        
    }
    
    
    static void assertContainsAll(Map<CharSequence, String> map, List<String> list) {
        for (String s : list) {
            assertTrue(map.containsKey(s));
            assertEquals(s, map.get(s));
        }
    }
    
    static void assertContainsNone(Map<CharSequence, String> map, List<String> list) {
        for (String s : list) {
            assertFalse(map.containsKey(s));
            assertNull(map.get(s));
        }
    }
    
}
