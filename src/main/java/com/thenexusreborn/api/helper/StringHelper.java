package com.thenexusreborn.api.helper;

import java.util.*;

public final class StringHelper {
    public static String capitalizeEveryWord(String string) {
        string = string.toLowerCase();
        String[] words = string.split("_");
        StringBuilder name = new StringBuilder();
        for (int w = 0; w < words.length; w++) {
            String word = words[w];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                if (i == 0) {
                    sb.append(Character.toUpperCase(word.charAt(i)));
                } else {
                    sb.append(word.charAt(i));
                }
            }
            name.append(sb);
            if (w < (words.length - 1)) {
                name.append(" ");
            }
        }
        
        return name.toString();
    }
    
    public static String join(Object[] objects, String separator) {
        return join(Arrays.asList(objects), separator);
    }
    
    public static String join(Collection<?> collection, String separator) {
        Iterator<?> iterator = collection.iterator();
        if (!iterator.hasNext()) {
            return "";
        } else {
            Object first = iterator.next();
            if (first == null)
                return "";
            if (!iterator.hasNext()) {
                return first.toString();
            } else {
                StringBuilder buf = new StringBuilder();
                buf.append(first);
    
                while (iterator.hasNext()) {
                    if (separator != null) {
                        buf.append(separator);
                    }

                    Object obj = iterator.next();
                    if (obj != null) {
                        buf.append(obj);
                    }
                }

                return buf.toString();
            }
        }
    }
    
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
