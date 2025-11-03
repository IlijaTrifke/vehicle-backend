package com.meuhlbauer.vehicle_backend.util;

public final class Strings {
    private Strings() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}