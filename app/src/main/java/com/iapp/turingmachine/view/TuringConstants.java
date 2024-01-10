package com.iapp.turingmachine.view;

import java.util.HashMap;
import java.util.Map;

public final class TuringConstants {

    public static Map<String, Map<String, String>> backendStateRegister = new HashMap<>();
    public static Map<Integer, Character> backendTape =  new HashMap<>();
    public static int startTape = 0;
    public static String alphabet = "_";
    public static long lastQ = 0;

}
