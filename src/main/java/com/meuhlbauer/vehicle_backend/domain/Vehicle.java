package com.meuhlbauer.vehicle_backend.domain;

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

  @NotNull
  @Min(1886)
  @Max(9999)
  @Column(nullable = false)
  private Integer firstRegistrationYear;

  @NotNull
  @Min(1)
  @Max(9999)
  @Column(nullable = false)
  private Integer cubicCapacity;

  @NotBlank
  @Pattern(regexp = "Diesel|Petrol|Hybrid")
  @Column(nullable = false, length = 10)
  private String fuel;

  @NotNull
  @Min(0)
  @Max(9_999_999)
  @Column(nullable = false)
  private Integer mileage;
}
