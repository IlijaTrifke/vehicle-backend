package com.meuhlbauer.vehicle_backend.controller;

import com.meuhlbauer.vehicle_backend.dto.VehicleRequest;
import com.meuhlbauer.vehicle_backend.dto.VehicleResponse;
import com.meuhlbauer.vehicle_backend.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping
    public List<VehicleResponse> getAll() {
        return vehicleService.list().stream().map(VehicleResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest req) {
        var created = vehicleService.create(req);
        return ResponseEntity.created(URI.create("/api/vehicles/" + created.getId()))
                .body(VehicleResponse.from(created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}