package com.meuhlbauer.vehicle_backend.dto;

import com.meuhlbauer.vehicle_backend.domain.Vehicle;

public record VehicleResponse(
        Long id, String model, Integer firstRegistrationYear, Integer cubicCapacity, String fuel, Integer mileage) {
    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getModel(), v.getFirstRegistrationYear(), v.getCubicCapacity(),
                v.getFuel(), v.getMileage());
    }
}