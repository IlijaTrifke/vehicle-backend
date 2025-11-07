package com.meuhlbauer.vehicle_backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Fuel {
    DIESEL,
    PETROL,
    HYBRID;

    @JsonCreator
    public static Fuel from(String val) {
        try {
            return Fuel.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid fuel type: '" + val + "'. Allowed values are: diesel, petrol, hybrid");
        }
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
