package com.meuhlbauer.vehicle_backend.service;

import com.meuhlbauer.vehicle_backend.domain.Vehicle;
import com.meuhlbauer.vehicle_backend.dto.VehicleRequest;
import com.meuhlbauer.vehicle_backend.repository.VehicleJpaRepository;
import com.meuhlbauer.vehicle_backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleJpaRepository repo;

    public List<Vehicle> list() {
        return repo.findAll();
    }

    public Vehicle create(VehicleRequest req) {
        var v = Vehicle.builder()
                .model(req.model())
                .firstRegistrationYear(req.firstRegistrationYear())
                .cubicCapacity(req.cubicCapacity())
                .fuel(req.fuel())
                .mileage(req.mileage())
                .build();
        return repo.save(v);
    }

    public void delete(Long id) {
        if (!repo.existsById(id))
            throw new NotFoundException("Vehicle %d not found".formatted(id));
        repo.deleteById(id);
    }
}