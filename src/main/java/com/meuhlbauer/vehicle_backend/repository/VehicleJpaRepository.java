package com.meuhlbauer.vehicle_backend.repository;

import com.meuhlbauer.vehicle_backend.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleJpaRepository extends JpaRepository<Vehicle, Long> {
}
