package lingutil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import ru.iitdgroup.lingutil.collect.LetterMap;



public class TestLetterMap {

    
    @Test
    public void testNewSizePutContainsKey() {
        LetterMap<String> m = new LetterMap<>();
        assertFalse(m.containsKey('A'));
        assertFalse(m.containsKey('0'));
        assertEquals(0, m.size());
        
        m = new LetterMap<String>()
                  .put('A', "alpha")
                  .put('b', "beta");
        
        assertEquals(2, m.size());
        assertTrue(m.containsKey('A'));
        assertTrue(m.containsKey('b'));
        assertFalse(m.containsKey('Z'));
        assertFalse(m.containsKey('z'));
        assertFalse(m.containsKey('0'));
        assertFalse(m.containsKey('='));
                
        m.put('Z', "dzeta");
        assertEquals(3, m.size());
        assertTrue(m.containsKey('Z'));
        assertFalse(m.containsKey('z'));
    }
    
    
    
    @Test
    public void testRemove() {
        LetterMap<String> m = new LetterMap<String>().put('A', "alpha").put('2', "two");
        assertEquals(2, m.size());
        m.remove('A');
        assertEquals(1, m.size());
        assertFalse(m.containsKey('A'));
        
        m.remove('B');
        assertEquals(1, m.size());
        assertFalse(m.containsKey('A'));
        assertFalse(m.containsKey('B'));
        
        m.remove('2');
        assertEquals(0, m.size());
        for (char c : "1234567890 +=-;'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".toCharArray())
            assertFalse(m.containsKey(c));
    }
    
    
    
    @Test
    public void testPutAll() {
        LetterMap<String> m = new LetterMap<>();
        m.putAll(Pair.of('A', "alpha"), Pair.of('3', "THREE"), Pair.of('Z', "dzeta"));
        assertTrue(m.containsKey('A'));
        assertTrue(m.containsKey('3'));
        assertTrue(m.containsKey('Z'));
        assertEquals(3, m.size());
        assertFalse(m.containsKey('B'));
        assertFalse(m.containsKey('a'));
        
        m.putAll(Pair.of('B', "beta"), Pair.of('a', "alpha-small"), Pair.of('3', "three again"));
        assertEquals(5, m.size());
        assertTrue(m.containsKey('3'));
        assertTrue(m.containsKey('B'));
        assertTrue(m.containsKey('a'));
    }
    
    
    
    @Test
    public void testGet() {
        LetterMap<String> m = new LetterMap<>();
        m.putAll(Pair.of('A', "alpha"), Pair.of('B', "beta"), Pair.of('1', "one"));
        assertEquals(3, m.size());
        assertEquals("alpha", m.get('A'));
        assertEquals("beta", m.get('B'));
        assertEquals("one", m.get('1'));
        assertNull(m.get('C'));
        assertNull(m.get('='));
        
        m.put('1', "ONE").put('A', "ALPHA").put('Z', "DZETA");
        assertEquals(4, m.size());
        assertEquals("ONE", m.get('1'));
        assertEquals("ALPHA", m.get('A'));
        assertEquals("beta", m.get('B'));
        assertNull(m.get('2'));
    }
    
    
    
    @Test
    public void testMakeImmutable() {
        LetterMap<Integer> m = new LetterMap<Integer>().put('0', 0).put('1', 1).put('2', 2).makeImmutable();
        assertEquals(3, m.size());
        
        try {
            m.put('3', 3);
            fail();
        } catch (UnsupportedOperationException e) { }
        assertEquals(3, m.size());
        assertFalse(m.containsKey('3'));
        assertNull(m.get('3'));
        
        try {
            m.putAll(Pair.of('4', 4), Pair.of('5', 5));
            fail();
        } catch (UnsupportedOperationException e) { }
        assertEquals(3, m.size());
        assertFalse(m.containsKey('4'));
        assertNull(m.get('4'));
        assertFalse(m.containsKey('3'));
        
        try {
            m.remove('0');
            fail();
        }  catch (UnsupportedOperationException e) { }
        assertTrue(m.containsKey('0'));
        assertEquals(Integer.valueOf(0), m.get('0'));
        assertEquals(3, m.size());
    }
    
    
}
