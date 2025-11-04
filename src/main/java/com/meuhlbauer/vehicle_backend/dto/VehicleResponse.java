package com.meuhlbauer.vehicle_backend.dto;

import com.meuhlbauer.vehicle_backend.domain.Vehicle;
import com.meuhlbauer.vehicle_backend.enums.Fuel;
import io.swagger.v3.oas.annotations.media.Schema;

public record VehicleResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Audi") String model,
        @Schema(example = "2020") String firstRegistrationYear,
        @Schema(example = "2000") Integer cubicCapacity,
        @Schema(example = "diesel") Fuel fuel,
        @Schema(example = "120000") Integer mileage) {
    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getModel(), v.getFirstRegistrationYear(), v.getCubicCapacity(),
                v.getFuel(), v.getMileage());
    }
}