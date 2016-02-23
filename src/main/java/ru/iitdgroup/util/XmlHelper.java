package ru.iitdgroup.util;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHelper {

    
    static public Iterable<Node> childrenOf(final Node n) {
        return nodesOf(n.getChildNodes());
    }
    
    
    static public Iterable<Node> nodesOf(final NodeList nl) {
        return () -> {
            return new Iterator<Node>() {
                int i = 0;
                @Override public boolean hasNext() {
                    return nl != null && nl.getLength() > i;
                }
                @Override public Node next() {
                    return nl.item(i++);
                }
            };
        };
    }
    
    
    static public Map<String, String> collectValues(final Node node, String... tags) {
        Set<String> ts = new HashSet<>(Arrays.asList(tags));
        Map<String, String> r = new HashMap<>();
        for (Node n : XmlHelper.childrenOf(node)) {
            if (ts.contains(n.getNodeName()))
                r.put(n.getNodeName(), n.getTextContent());
        }
        return r;
    }
    
    
    @SuppressWarnings("static-access")
    static final ThreadLocal<Transformer> TRANSFORMER = new ThreadLocal<>().withInitial(() -> {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            return transformer; 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });
 
            
    static public String tryStringify(Node n, boolean printException) {
        try {
            StringWriter sw = new StringWriter();
            TRANSFORMER.get().transform(new DOMSource(n), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            if (printException)
                e.printStackTrace();
            return null;
        }
    }
 
    
}
