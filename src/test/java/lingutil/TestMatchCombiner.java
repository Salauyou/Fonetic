package lingutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.Arrays;
import org.junit.Test;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ru.iitdgroup.lingutil.match.MatchCombiner;
import ru.iitdgroup.lingutil.match.MatchCombiner.Seq;
import ru.iitdgroup.lingutil.match.ScoredMatch;



public class TestMatchCombiner {

    @Test
    public void testfindBestCombinedMatch() {
        
        
        
        //          0 1 2 3 4 5 6 7 8 9
        // word 0:  '---'   '====='      (2, 2)
        //                      '---'    (2)
        //      1:    '===' '---'        (2, 2)
        //      2:  '-'   '='   '-----'  (1, 1, 3)
        
        
        Multimap<Integer, ScoredMatch> ms = HashMultimap.create();
        ms.putAll(0, Arrays.asList(new ScoredMatch(0, 2, 2d), new ScoredMatch(4, 7, 2d), new ScoredMatch(6, 8, 2d)));
        ms.putAll(1, Arrays.asList(new ScoredMatch(1, 3, 2d), new ScoredMatch(4, 6, 2d)));
        ms.putAll(2, Arrays.asList(new ScoredMatch(0, 1, 1d), new ScoredMatch(3, 4, 1d), new ScoredMatch(6, 9, 3d)));
        
        Seq res = MatchCombiner.findBestCombinedMatch(ms, d -> d == 0 ? 1.0 : 0.5);
        assertEquals(1,  res.effectiveStart());
        assertEquals(7,  res.end);
        assertEquals(5d, res.score, 1e-10);
        
        //          0 1 2 3 4
        // word 0:  '---'      (5)
        //            '==='    (8)
        //              '---'  (6)
        
        
        ms.clear();
        ms.putAll(0, Arrays.asList(new ScoredMatch(0, 2, 5d), new ScoredMatch(1, 3, 8d), new ScoredMatch(2, 4, 6d)));
        res = MatchCombiner.findBestCombinedMatch(ms, d -> d == 0 ? 1.0 : 0.5);
        assertEquals(1, res.effectiveStart());
        assertEquals(3, res.end);
        assertEquals(8d, res.score, 1e-10);
        
        ms.clear();
        assertNull(MatchCombiner.findBestCombinedMatch(ms, d -> d == 0 ? 1.0 : 0.5));
    }
    
    
    
}
