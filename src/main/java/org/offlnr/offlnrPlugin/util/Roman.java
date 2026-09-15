package org.offlnr.offlnrPlugin.util;

/** Función para convertir un número a numeral romano (sin límite superior). */
public final class Roman {

    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private Roman() {
    }

    public static String toRoman(int number) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < VALUES.length; i++) {
            while (number >= VALUES[i]) {
                number -= VALUES[i];
                result.append(SYMBOLS[i]);
            }
        }
        return result.toString();
    }
}
