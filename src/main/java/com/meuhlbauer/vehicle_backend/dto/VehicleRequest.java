package com.meuhlbauer.vehicle_backend.dto;

import com.meuhlbauer.vehicle_backend.enums.Fuel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record VehicleRequest(
        @NotBlank @Size(max = 40) @Schema(example = "Audi") String model,
        @NotBlank @Pattern(regexp = "\\d{4}", message = "First registration year must have 4 digits") @Schema(example = "2020") String firstRegistrationYear,
        @NotNull @Min(1) @Max(9999) @Schema(example = "2000") Long cubicCapacity,
        @NotNull @Schema(example = "diesel") Fuel fuel,
        @NotNull @Min(0) @Max(9_999_999) @Schema(example = "120000") Long mileage) {
}