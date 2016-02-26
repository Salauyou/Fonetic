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
import ru.iitdgroup.lingutil.collect.TrieMap;



public class TrieMapVsJavaMaps {

    Map<String, Integer> hashmap = new HashMap<>();
    Map<String, Integer> treemap = new TreeMap<>();
    PatriciaTrie<Integer>  ptmap = new PatriciaTrie<>();
    TrieMap<Integer>     triemap = new TrieMap<>();   

    List<String> keys = new ArrayList<>();
    
    
    @Before
    public void prepare() {
        int n = 100_000;
        int tl = 0;
        for (int i = 0; i < n; i++) {
            String k = randomString(7);
            tl += k.length();
            keys.add(k);
            hashmap.put(k, i);
            triemap.put(k, i);
            treemap.put(k, i);
            ptmap.put(k, i);
            keys.add(randomString(7));
        }        
        System.out.format("Summary key length: %s, Nodes in TrieMap: %s\n\n", 
                           tl, triemap.nodeCount());
    }
    
    
    
    @Test
    public void testGet() {
        
        final int n = 1_000_000;
        TimeMeasurer.measureTime(5, 
           
            new TestMapTask("java.util.HashMap", hashmap, keys, n),
            new TestMapTask("java.util.TreeMap", treemap, keys, n),
            new TestMapTask("ap.c.PatriciaTrie", ptmap,   keys, n),
            
            // TrieMap
            new Task() {
            
                int c = 0;
                List<String> workKeys = new ArrayList<>();
                
                @Override
                public void prepare() {
                    workKeys.clear();
                    for (int i = 0; i < n; i++) 
                        workKeys.add(keys.get(i % keys.size()));
                    Collections.shuffle(workKeys);
                }
                
                @Override
                public void run() {
                    for (String k : workKeys) {
                        Integer j = triemap.get(k);
                        if (j != null)
                            c += j;
                    }
                }

                @Override
                public void displayTime(long millis) {
                    System.out.format("          TrieMap %s `get()`s took %s ms. Result: %s\n\n", workKeys.size(), millis, c);
                }
            });        
    }
    
    
    
    static class TestMapTask implements Task {
        
        final Map<String, Integer> m;
        final List<String> workKeys = new ArrayList<>();
        final int n;
        int c = 0;
        final String name;
        
        TestMapTask(String name, Map<String, Integer> map, List<String> keys, int n) {
            m = map;
            this.n = n;
            this.name = name;
            for (int i = 0; i < n; i++) 
                workKeys.add(new String(keys.get(i % keys.size()).toCharArray()));  // recreate to clear cached hash
            Collections.shuffle(workKeys);
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
