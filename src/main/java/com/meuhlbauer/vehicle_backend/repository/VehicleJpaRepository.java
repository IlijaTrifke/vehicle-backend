package com.meuhlbauer.vehicle_backend.repository;

import com.meuhlbauer.vehicle_backend.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehicleJpaRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
}
