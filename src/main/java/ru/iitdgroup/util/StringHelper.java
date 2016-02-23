package ru.iitdgroup.util;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class StringHelper {

    static public String joinNonBlank(CharSequence joinString, Iterable<? extends CharSequence> items) {
        if (items == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : items) {
            if (!StringUtils.isBlank(s))
                sb.append(s).append(joinString);
        }
        return sb.length() == 0 ? "" : sb.substring(0, sb.length() - joinString.length());
    }
    
    
    static public String joinNonBlank(CharSequence joinString, CharSequence... items) {
        return joinNonBlank(joinString, Arrays.asList(items));
    }
    
    
    static public String flatify(String s) {
        return s == null ? null : s.replace('\n', '¬').replace('\t', '→').replace("\r", "");
    }
    
    
}
