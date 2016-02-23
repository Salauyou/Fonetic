package ru.iitdgroup.lingutil.collect;

public final class LetterTrie {

    Node root = new Node();
    int size = 0;
    
    
    public LetterTrie add(CharSequence s) {
        if (contains(s))
            return this;
        root.add(s, 0);
        size += 1;
        return this;
    }
    
    
    public boolean contains(CharSequence s) {
        return root.contains(s, 0);
    }
    
    
    public int size() {
        return size;
    }
    
    
    public int nodeCount() {
        return root.nodeCount;
    }
    
    
    
    static private final class Node {
        
        int nodeCount;
        boolean ending;
        LetterMap<Node> next;
        
        // adds a char sequence into node, creating subnodes if needed,
        // returns how node count of this node is changed
        public int add(CharSequence s, int from) {
            if (from == s.length()) {
                ending = true;
                return 0;
            }
            int  r = 0;
            char c = s.charAt(from);
            if (next == null)
                next = new LetterMap<Node>();
            Node n = next.get(c);
            if (n == null) {
                n = new Node();
                r = 1;
                next.put(c, n);
            }
            r += n.add(s, from + 1);
            nodeCount += r;
            return r;
        }
                
        public boolean contains(CharSequence s, int from) {
            if (from == s.length())
                return ending;
            if (next == null)
                return false;
            Node n = next.get(s.charAt(from));
            return n != null && n.contains(s, from + 1);
        }  
    }
    
}
