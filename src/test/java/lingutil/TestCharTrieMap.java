package lingutil;

import java.util.Arrays;

import org.junit.Test;

import ru.iitdgroup.lingutil.collect.CharTrieMap;

public class TestCharTrieMap {

    @Test
    public void debug() {
        CharTrieMap<String> t = new CharTrieMap<>();
        for (String s : Arrays.asList("APP", "APPLE", "APPDATA", "APPLE", "APPLES", 
                                      "BE", "BEING", "BEHIND"))
            t.put(s, s);
        
        for (String s : Arrays.asList("A", "APPLAUSE", "BEHI"))
            t.put(s, s);
        
    }
    
}
