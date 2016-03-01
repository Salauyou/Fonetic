package lingutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.junit.Before;
import org.junit.Test;

import lingutil.TimeMeasurer.Task;
import ru.iitdgroup.lingutil.collect.CharTrieMap;



public class CharTrieMapVsStringMaps {

    Map<String, Integer> hashmap;
    Map<String, Integer> treemap;
    Map<String, Integer> ptmap;
    Map<CharSequence, Integer> triemap;   

    List<String> existingKeys = new ArrayList<>();
    List<String> absentKeys = new ArrayList<>();
    
    @Before
    public void before() {
        hashmap = new HashMap<>();
        treemap = new TreeMap<>();
        ptmap   = new PatriciaTrie<>();
        triemap = new CharTrieMap<>(); 
        
        int n = 100_000;
        for (int i = 0; i < n; i++) {
            String k = randomString(7);
            existingKeys.add(k);
            hashmap.put(k, i);
            triemap.put(k, i);
            treemap.put(k, i);
            ptmap.put(k, i);
            absentKeys.add(randomString(7));
        }        
    }
    
    
    
    @Test
    public void testGet() {
        TimeMeasurer.measureTime(5,                        
            new TestGet("java.util.HashMap", hashmap, existingKeys, absentKeys),
            new TestGet("java.util.TreeMap", treemap, existingKeys, absentKeys),
            new TestGet("ap.c.PatriciaTrie", ptmap,   existingKeys, absentKeys),
            new TestGet("      CharTrieMap", triemap, existingKeys, absentKeys));
        System.out.println();
    }
    
    
    @Test
    public void testContainsKey() {
        TimeMeasurer.measureTime(5,                        
            new TestContainsKey("java.util.HashMap", hashmap, existingKeys, absentKeys),
            new TestContainsKey("java.util.TreeMap", treemap, existingKeys, absentKeys),
            new TestContainsKey("ap.c.PatriciaTrie", ptmap,   existingKeys, absentKeys),
            new TestContainsKey("      CharTrieMap", triemap, existingKeys, absentKeys));
        System.out.println();
    }
    
    
    @Test
    public void testPut() {
        TimeMeasurer.measureTime(5,                        
            new TestPut("java.util.HashMap", hashmap, existingKeys),
            new TestPut("java.util.TreeMap", treemap, existingKeys),
            new TestPut("ap.c.PatriciaTrie", ptmap,   existingKeys),
            new TestPut("      CharTrieMap", triemap, existingKeys));
        System.out.println();
    }
    
    
    @Test
    public void testPutIfAbsent() {
        TimeMeasurer.measureTime(5,                        
            new TestPutIfAbsent("java.util.HashMap", hashmap, existingKeys, absentKeys),
            new TestPutIfAbsent("java.util.TreeMap", treemap, existingKeys, absentKeys),
            new TestPutIfAbsent("ap.c.PatriciaTrie", ptmap,   existingKeys, absentKeys),
            new TestPutIfAbsent("      CharTrieMap", triemap, existingKeys, absentKeys));
        System.out.println();
    }
    
    
    
    
    static class TestGet implements Task {
        
        final Map<? super String, Integer> m;
        List<String> workKeys = new ArrayList<>();
        int c = 0;
        final String name;
        
        TestGet(String name, Map<? super String, Integer> map, List<String> existingKeys, List<String> absentKeys) {
            this.workKeys.addAll(existingKeys);
            this.workKeys.addAll(absentKeys);
            m = map;
            this.name = name;
        }
       
        @Override
        public void prepare() {
            List<String> newKeys = new ArrayList<>();
            for (String k : workKeys) 
                newKeys.add(new String(k.toCharArray()));  // recreate to clear cached hash
            Collections.shuffle(newKeys);
            workKeys = newKeys;
            c = 0;
        }        
        
        @Override
        public void run() {
            for (String k : workKeys) {
                Integer j = m.get(k);
                if (j != null)
                    c += j;
            }
        }

        @Override
        public void displayTime(long millis) {
            System.out.format("%s %s `get()`s took %s ms. Result: %s\n", name, workKeys.size(), millis, c);
        }        
    }
    
    
    static class TestContainsKey implements Task {
        
        final Map<? super String, Integer> m;
        List<String> workKeys = new ArrayList<>();
        int c = 0;
        final String name;
        
        TestContainsKey(String name, Map<? super String, Integer> map, List<String> existingKeys, List<String> absentKeys) {
            this.workKeys.addAll(existingKeys);
            this.workKeys.addAll(absentKeys);
            m = map;
            this.name = name;
        }
       
        @Override
        public void prepare() {
            List<String> newKeys = new ArrayList<>();
            for (String k : workKeys) 
                newKeys.add(new String(k.toCharArray()));  // recreate to clear cached hash
            Collections.shuffle(newKeys);
            workKeys = newKeys;
            c = 0;
        }   
        
        @Override
        public void run() {
            for (String k : workKeys)
                c += m.containsKey(k) ? 1 : 0;
        }

        @Override
        public void displayTime(long millis) {
            System.out.format("%s %s `containsKey()`s took %s ms. Result: %s\n", name, workKeys.size(), millis, c);
        }        
    }
    
    
    
    
    static class TestPutIfAbsent implements Task {
        
        final Map<? super String, Integer> m;
        final List<String> workKeys = new ArrayList<>();
        final String name;
        final List<String> existingKeys;
        final List<String> absentKeys;
        
        TestPutIfAbsent(String name, Map<? super String, Integer> map, List<String> existingKeys, List<String> absentKeys) {
            this.existingKeys = Collections.unmodifiableList(existingKeys);
            this.absentKeys = Collections.unmodifiableList(absentKeys);
            m = map;
            this.name = name;
        }
       
        @Override
        public void prepare() {
            workKeys.clear();
            m.clear();
            for (String k : existingKeys) {
                m.put(k, 1);
                workKeys.add(new String(k.toCharArray()));
            }

            for (String k : absentKeys)
                workKeys.add(new String(k.toCharArray()));
            Collections.shuffle(workKeys);
        }       
        
        @Override
        public void run() {
            for (String k : workKeys)
                m.putIfAbsent(k, 1);
        }
        
        @Override
        public void displayTime(long millis) {
            System.out.format("%s %s `putIfAbsent()`s took %s ms.\n", name, workKeys.size(), millis);
        }        
    }
    
    
    
    static class TestPut implements Task {
        
        final Map<? super String, Integer> m;
        final List<String> workKeys = new ArrayList<>();
        final String name;
        final List<String> keys;
        int c;
        
        TestPut(String name, Map<? super String, Integer> map, List<String> keys) {
            this.keys = Collections.unmodifiableList(keys);
            m = map;
            this.name = name;
        }
       
        @Override
        public void prepare() {
            workKeys.clear();
            m.clear();            
            for (String k : keys) {
                workKeys.add(new String(k.toCharArray()));  // 2 copies
                workKeys.add(new String(k.toCharArray())); 
            }
            Collections.shuffle(workKeys);
            c = 0;
        }        
        
        @Override
        public void run() {
            for (String k : workKeys)
                m.put(k, c++);
        }
        
        @Override
        public void displayTime(long millis) {
            System.out.format("%s %s `put()`s took %s ms.\n", name, workKeys.size(), millis);
        }        
    }
    
    
    
    static final Random RND = new Random();
    static final char[] LETTERS = "AOUIEQWRTYPSDFGHJKLZXCVBNM".toCharArray();
    
    
    static public String randomString(int expectedLength) {
        StringBuilder sb = new StringBuilder();
        int len = Math.max(1, expectedLength + (int) (RND.nextGaussian() * 3.0));
        for (int i = 0; i < len; i++) {
            if (RND.nextDouble() < 0.5)         
                sb.append(LETTERS[RND.nextInt(5)]);                      // wovel
            else
                sb.append(LETTERS[RND.nextInt(LETTERS.length - 5) + 5]); // consolantial
        }
        return sb.toString();
    }
    
    
}
