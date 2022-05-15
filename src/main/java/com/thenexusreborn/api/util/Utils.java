package com.thenexusreborn.api.util;

import java.util.*;

public final class Utils {
    
    private static final String alphabet = "abcdefghigklmnopqrstuvwzyz", numbers = "0123456789";
    
    public static String generateCode(int amount, boolean caps, boolean useAlphabet, boolean useNumbers) {
        if (!useAlphabet && !useNumbers) {
            throw new IllegalArgumentException("Invalid parameters, you must specify to use the alphabet or to use numbers, both cannot be false");
        }
        
        List<String> chars = new LinkedList<>();
        if (useAlphabet) {
            chars.addAll(Arrays.asList(alphabet.split("")));
        } 
        if (useNumbers) {
            chars.addAll(Arrays.asList(numbers.split("")));
        }
                
        List<String> allowedCharacters = Collections.unmodifiableList(chars);
    
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < amount; i++) {
            String c = allowedCharacters.get(random.nextInt(allowedCharacters.size()));
            if (caps) {
                if (random.nextInt(100) < 50) {
                    c = c.toUpperCase();
                }
            }
            codeBuilder.append(c);
        }
        
        return codeBuilder.toString();
    }
    
    public static void printCurrentStack() {
        System.out.println(Arrays.toString(new Throwable().getStackTrace()));
    }
}