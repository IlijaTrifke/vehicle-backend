package com.meuhlbauer.vehicle_backend.dto;

import com.meuhlbauer.vehicle_backend.enums.Fuel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record VehicleRequest(
                @NotBlank @Size(max = 40) @Schema(example = "Audi") String model,
                @NotBlank @Pattern(regexp = "\\d{4}", message = "First registration year must have 4 digits") @Schema(example = "2020") String firstRegistrationYear,
                @NotNull @Digits(integer = 4, fraction = 0) @Positive @Schema(example = "2000") Integer cubicCapacity,
                @NotNull @Schema(example = "diesel") Fuel fuel,
                @NotNull @Digits(integer = 7, fraction = 0) @PositiveOrZero @Schema(example = "120000") Integer mileage) {
}