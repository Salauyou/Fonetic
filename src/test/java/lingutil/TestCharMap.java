package lingutil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import ru.iitdgroup.lingutil.collect.CharMap;
import ru.iitdgroup.lingutil.collect.CharMap.CharEntry;



public class TestCharMap {

    
    @Test
    public void testNewSizePutContainsKey() {
        CharMap<String> m = CharMap.create();
        assertFalse(m.containsKey('A'));
        assertFalse(m.containsKey('0'));
        assertEquals(0, m.size());
        
        m = CharMap.<String>create()
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
        CharMap<String> m 
            = CharMap.<String>create()
                     .put('A', "alpha")
                     .put('2', "two")
                     .put('a', "alpha-small")
                     .put('q', "query");
        
        assertEquals(4, m.size());
        m.remove('A');
        assertEquals(3, m.size());
        assertFalse(m.containsKey('A'));
        
        m.remove('B');
        assertEquals(3, m.size());
        assertFalse(m.containsKey('A'));
        assertFalse(m.containsKey('B'));
        
        m.remove('q');
        assertEquals(2, m.size());
        assertFalse(m.containsKey('q'));
        assertFalse(m.containsKey('A'));
        assertNull(m.get('q'));
        assertNull(m.get('A'));
        
        assertTrue(m.containsKey('a'));
        assertEquals("alpha-small", m.get('a'));
        assertTrue(m.containsKey('2'));
        assertEquals("two", m.get('2'));
        
        m.put('A', "alpha-new");
        m.remove('2');
        assertTrue(m.containsKey('A'));
        assertEquals("alpha-new", m.get('A'));
        assertFalse(m.containsKey('2'));
        assertNull(m.get('2'));
        assertEquals(2, m.size());
        
        // keys
        assertEquals(Arrays.asList('A', 'a'), 
                m.stream().map(CharEntry::getChar).collect(Collectors.toList()));
        
        // values
        assertEquals(Arrays.asList("alpha-new", "alpha-small"), 
                m.stream().map(CharEntry::getValue).collect(Collectors.toList()));
        
        for (char c : "134567890 +=-;'qwertyuiopsdfghjklzxcvbnmQWERTYUIOPSDFGHJKLZXCVBNM".toCharArray())
            assertFalse(m.containsKey(c));
        
        m.remove('A').remove('q').remove('a').remove('1'); 
        assertEquals(0, m.size());
        assertFalse(m.entries().hasNext());
        
    }
    
    
    
    @Test
    public void testPutAll() {
        CharMap<String> m = CharMap.create();
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
        CharMap<String> m = CharMap.create();
        m.put('A', "alpha").put('a', "small-alpha").put('B', "beta").put('1', "one");
        assertEquals(4, m.size());
        assertEquals("alpha", m.get('A'));
        assertEquals("small-alpha", m.get('a'));
        assertEquals("beta", m.get('B'));
        assertEquals("one", m.get('1'));
        assertNull(m.get('C'));
        assertNull(m.get('='));
        
        m.put('1', "ONE").put('A', "ALPHA").put('Z', "DZETA");
        assertEquals(5, m.size());
        assertEquals("ONE", m.get('1'));
        assertEquals("ALPHA", m.get('A'));
        assertEquals("beta", m.get('B'));
        assertNull(m.get('2'));
    }
    
    
    
    @Test
    public void testMakeImmutable() {
        CharMap<Integer> m = CharMap.create();
        m.put('0', 0).put('1', 1).put('2', 2).makeImmutable();
        assertEquals(3, m.size());
        
        try {
            m.put('3', 3);
            fail();
        } catch (UnsupportedOperationException e) { }
        assertEquals(3, m.size());
        assertFalse(m.containsKey('3'));
        assertNull(m.get('3'));
        
        try {
            m = m.put('4', 4).put('5', 5);
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
        
        m = CharMap.create();
        m.makeImmutable();
        try {
            m.put('a', 0);
            fail();
        } catch (UnsupportedOperationException e) { } 
        assertEquals(0, m.size());
        assertFalse(m.containsKey('a'));
        
        m = CharMap.create();
        m.put('a', 0);
        assertEquals((Integer) 0, m.get('a'));
        assertEquals(1, m.size());
        m.makeImmutable();
        try {
            m.put('a', 1);
            fail();
        } catch (UnsupportedOperationException e) { }
        assertEquals(1, m.size());
        assertEquals((Integer) 0, m.get('a')); 
    }
    
    
    @Test
    public void testIterator() {
        
        CharMap<Integer> m = CharMap.create();
        m.put('1', 1).put('4', 4).put('2', 2).put('3', 3)
         .put('q', -1);
        Iterator<CharEntry<Integer>> it = m.iterator();
        CharEntry<Integer> e;
        
        for (int i = 1; i <= 4; i++) {
            assertTrue(it.hasNext());
            e = it.next();
            assertEquals(String.valueOf(i).charAt(0), e.getChar());
            assertEquals((Integer) i, e.getValue());
        }
        assertTrue(it.hasNext());
        e = it.next();
        assertEquals('q', e.getChar());
        assertEquals((Integer) (-1),    e.getValue());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail();
        } catch (NoSuchElementException ex) { }
        
        
        m = CharMap.create();
        assertFalse(m.iterator().hasNext());
        try {
            m.iterator().next();
            fail();
        } catch (NoSuchElementException ex) { }
        
        
        m = CharMap.create();
        m.put('a', 1);
        it = m.iterator();
        assertTrue(it.hasNext());
        e = it.next();
        assertEquals('a', e.getChar());
        assertEquals((Integer) 1, e.getValue());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail();
        } catch (NoSuchElementException ex) { }
    }
    
    
}
