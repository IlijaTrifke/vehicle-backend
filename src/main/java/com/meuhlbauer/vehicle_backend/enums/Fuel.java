package com.meuhlbauer.vehicle_backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Fuel {
    DIESEL,
    PETROL,
    HYBRID;

    @JsonCreator
    public static Fuel from(String val) {
        return Fuel.valueOf(val.trim().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
