package com.meuhlbauer.vehicle_backend.service;

import com.meuhlbauer.vehicle_backend.domain.Vehicle;
import com.meuhlbauer.vehicle_backend.dto.VehicleRequest;
import com.meuhlbauer.vehicle_backend.exception.NotFoundException;
import com.meuhlbauer.vehicle_backend.repository.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleJpaRepository repo;

    /**
     * Retrieves all vehicles from the database.
     *
     * @return list of all vehicles
     */
    @Transactional(readOnly = true)
    public List<Vehicle> list() {
        return repo.findAll();
    }

    /**
     * Finds a vehicle by its ID.
     *
     * @param id the vehicle ID
     * @return the vehicle with the given ID
     * @throws NotFoundException if vehicle with given ID does not exist
     */
    @Transactional(readOnly = true)
    public Vehicle findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("vehicle", String.valueOf(id),
                        "Vehicle %d not found".formatted(id)));
    }

    /**
     * Creates a new vehicle.
     *
     * @param req the vehicle request data
     * @return the created vehicle
     */
    @Transactional
    public Vehicle create(VehicleRequest req) {
        Vehicle vehicle = Vehicle.builder()
                .model(req.model())
                .firstRegistrationYear(req.firstRegistrationYear())
                .cubicCapacity(req.cubicCapacity().intValue())
                .fuel(req.fuel())
                .mileage(req.mileage().intValue())
                .build();
        return repo.save(vehicle);
    }

    /**
     * Deletes a vehicle by its ID.
     *
     * @param id the vehicle ID
     * @throws NotFoundException if vehicle with given ID does not exist
     */
    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = findById(id);
        repo.delete(vehicle);
    }
}