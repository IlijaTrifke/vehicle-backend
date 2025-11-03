package com.meuhlbauer.vehicle_backend.dto;

import jakarta.validation.constraints.*;

public record VehicleRequest(
        @NotBlank @Size(max = 40) String model,
        @NotNull @Min(1886) @Max(9999) Integer firstRegistrationYear,
        @NotNull @Min(1) @Max(9999) Integer cubicCapacity,
        @NotBlank @Pattern(regexp = "Diesel|Petrol|Hybrid") String fuel,
        @NotNull @Min(0) @Max(9_999_999) Integer mileage) {
}