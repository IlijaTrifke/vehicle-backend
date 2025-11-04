package com.meuhlbauer.vehicle_backend.domain;

import com.meuhlbauer.vehicle_backend.enums.Fuel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 40)
  @Column(nullable = false, length = 40)
  private String model;

  @NotBlank
  @Pattern(regexp = "\\d{4}", message = "Year must have 4 digits")
  @Column(nullable = false, length = 4)
  private String firstRegistrationYear;

  @NotNull
  @Min(1)
  @Max(9999)
  @Column(nullable = false)
  private Integer cubicCapacity;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Fuel fuel;

  @NotNull
  @Min(0)
  @Max(9_999_999)
  @Column(nullable = false)
  private Integer mileage;
}
