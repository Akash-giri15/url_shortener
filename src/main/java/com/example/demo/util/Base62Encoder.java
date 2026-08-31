package com.example.demo.util;

public class Base62Encoder {

    // digits first, then lowercase, then uppercase -- 62 symbols total, order is
    // arbitrary but must stay fixed once you start storing codes with it
    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length(); // 62

    private Base62Encoder() {} // utility class -- never instantiated

    public static String encode(long value) {
        if (value == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        long n = value;
        while (n > 0) {
            int remainder = (int) (n % BASE);     // which of the 62 symbols this position holds
            sb.append(ALPHABET.charAt(remainder));
            n /= BASE;                             // shift down to the next digit, same as base10 long division
        }
        return sb.reverse().toString();            // built least-significant-digit first, so flip it
    }

    public static long decode(String code) {
        long result = 0;
        for (int i = 0; i < code.length(); i++) {
            int digitValue = ALPHABET.indexOf(code.charAt(i));
            if (digitValue == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + code.charAt(i));
            }
            result = result * BASE + digitValue;   // each existing digit shifts up one "place" as we read left to right
        }
        return result;
    }
}