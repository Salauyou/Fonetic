package lingutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ru.iitdgroup.lingutil.collect.SimpleTrieMap;
import ru.iitdgroup.lingutil.collect.TrieMap;
import ru.iitdgroup.lingutil.collect.TrieMap.TrieCursor;

public class TestSimpleTrieMapCursor {

    //    -1   0   1   2   3   4   5   6   7
    // [root]┬[A]─(P)─[P]┬(D)─(A)─(T)─[A]
    //       │ ↓         │ ↓
    //       │           └(L)┬(A)─(U)─(S)─[E]
    //       │               │ ↓
    //       │               └[E]─[S]
    //       ├(B)┬[E]┬[D]
    //       │ ↓ │ ↓ │ ↓
    //       │   │   ├(H)─(I)─(N)─[D]
    //       │   │   │ ↓
    //       │   │   └(I)─(N)─[G]
    //       │   │
    //       │   └(U)─(R)─(M)─[A]
    //       │
    //       └(C)─[A]
    //         0   1   2   3   4   5   6   7
    
    final List<String> keys = Arrays.asList("", "APP", "APPLE", "APPDATA", "APPLES", 
                                            "BE", "BEING", "BEHIND", "A", "APPLAUSE", 
                                            "BED", "BURMA", "CA"); 
    TrieMap<String> t;
    
    @Before
    public void before() {
        t = new SimpleTrieMap<>();
        for (String key : keys) {
            t.put(key, key);
            assertEquals(key, t.get(key));
        }
        assertEquals(keys.size(), t.size());
    }
    
    @Test
    public void testCursorSimpleIteration() {
        TrieCursor<String> c = t.getCursor();
        
        assertTrue(c.hasValue());
        assertEquals("", c.getValue());
        assertEquals(-1, c.currentPosition());
        assertFalse(c.hasMore());
        assertTrue(c.hasNext());

        assertEquals('A', c.next()); assertState(c, 'A', 0, "A", true, true);  
        assertEquals('P', c.next()); assertState(c, 'P', 1, null, false, true);
        assertEquals('P', c.next()); assertState(c, 'P', 2, "APP", false, true);
        
        assertEquals('D', c.next()); assertState(c, 'D', 3, null, true, true);
        assertEquals('A', c.next()); assertState(c, 'A', 4, null, false, true);
        assertEquals('T', c.next()); assertState(c, 'T', 5, null, false, true);
        assertEquals('A', c.next()); assertState(c, 'A', 6, "APPDATA", false, false);
        
        assertEquals('T', c.back());
        assertEquals('A', c.back()); assertState(c, 'A', 4, null, false, true);
        assertEquals('D', c.back()); assertState(c, 'D', 3, null, true, true);
        
        assertEquals('L', c.more()); assertState(c, 'L', 3, null, false, true);
        assertEquals('A', c.next()); assertState(c, 'A', 4, null, true, true);
        assertEquals('U', c.next()); assertState(c, 'U', 5, null, false, true);
        
        assertEquals('A', c.back()); assertState(c, 'A', 4, null, true, true);  // back and forth 2 chars
        assertEquals('L', c.back()); assertState(c, 'L', 3, null, false, true);
        assertEquals('A', c.next()); assertState(c, 'A', 4, null, true, true);
        assertEquals('U', c.next()); assertState(c, 'U', 5, null, false, true);
        
        assertEquals('S', c.next()); assertState(c, 'S', 6, null, false, true);
        assertEquals('E', c.next()); assertState(c, 'E', 7, "APPLAUSE", false, false);
        
        c.back(); 
        c.back(); 
        c.back(); 
        assertState(c, 'A', 4, null, true, true); // 3 chars back
        
        assertEquals('E', c.more()); assertState(c, 'E', 4, "APPLE", false, true);
        assertEquals('S', c.next()); assertState(c, 'S', 5, "APPLES", false, false);
        
        c.back(); 
        c.back(); 
        c.back();  assertState(c, 'P', 2, "APP", false, true);
        c.back();  assertState(c, 'P', 1, null, false, true);
        c.back();  assertState(c, 'A', 0, "A", true, true);   // 5 chars back
        
    }
    
    
    
    static void assertState(TrieCursor<String> c, char cc, int pos, 
                            String value, boolean hasMore, boolean hasNext) {
        assertEquals(cc, c.currentChar());
        assertEquals(pos, c.currentPosition());
        assertEquals(value == null ? false : true, c.hasValue());
        assertEquals(value, c.getValue());
        assertEquals(hasMore, c.hasMore());
        assertEquals(hasNext, c.hasNext());
    } 
    
    
}
