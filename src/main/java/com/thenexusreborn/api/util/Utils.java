package com.thenexusreborn.api.util;

import com.starmediadev.starlib.Code;

import java.text.DecimalFormat;
import java.util.*;

public final class Utils {
    
    public static String formatNumber(Number number) {
        return new DecimalFormat("#.#").format(number);
    }
    
    public static String generateCode(int amount, boolean caps, boolean useAlphabet, boolean useNumbers) {
        return new Code(amount, caps, useAlphabet, useNumbers).getValue();
    }
    
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void printCurrentStack() {
        System.out.println(Arrays.toString(new Throwable().getStackTrace()));
    }
}