package com.meuhlbauer.vehicle_backend.service;

import com.meuhlbauer.vehicle_backend.domain.Vehicle;
import com.meuhlbauer.vehicle_backend.dto.VehicleRequest;
import com.meuhlbauer.vehicle_backend.enums.Fuel;
import com.meuhlbauer.vehicle_backend.exception.NotFoundException;
import com.meuhlbauer.vehicle_backend.repository.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
     * Retrieves paginated vehicles from the database.
     *
     * @param pageable pagination information (page, size, sort)
     * @return page of vehicles
     */
    @Transactional(readOnly = true)
    public Page<Vehicle> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    /**
     * Retrieves paginated vehicles from the database with optional filters.
     *
     * @param firstRegistrationYear optional filter for first registration year
     *                              (exact year or range format: "YYYY-YYYY")
     * @param fuel                  optional filter for fuel type
     * @param modelSearch           optional search term for model (case-insensitive
     *                              partial match)
     * @param pageable              pagination information (page, size, sort)
     * @return page of vehicles matching the filters
     */
    @Transactional(readOnly = true)
    public Page<Vehicle> findAllWithFilters(String firstRegistrationYear, Fuel fuel, String modelSearch,
                                            Pageable pageable) {
        Specification<Vehicle> spec = Specification.where(null);

        if (firstRegistrationYear != null && !firstRegistrationYear.isBlank()) {
            if (firstRegistrationYear.contains("-")) {
                // Range format: "YYYY-YYYY"
                String[] parts = firstRegistrationYear.split("-", 2);
                if (parts.length == 2) {
                    String startYear = parts[0].trim();
                    String endYear = parts[1].trim();

                    if (!startYear.isEmpty() && !endYear.isEmpty()
                            && startYear.matches("\\d{4}") && endYear.matches("\\d{4}")) {
                        // If start and end year are the same, treat as exact year
                        if (startYear.equals(endYear)) {
                            spec = spec
                                    .and((root, query, cb) -> cb.equal(root.get("firstRegistrationYear"), startYear));
                        } else {
                            spec = spec.and((root, query, cb) -> cb.and(
                                    cb.greaterThanOrEqualTo(root.get("firstRegistrationYear"), startYear),
                                    cb.lessThanOrEqualTo(root.get("firstRegistrationYear"), endYear)));
                        }
                    }
                }
            } else {
                spec = spec
                        .and((root, query, cb) -> cb.equal(root.get("firstRegistrationYear"), firstRegistrationYear));
            }
        }

        if (fuel != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("fuel"), fuel));
        }

        if (modelSearch != null && !modelSearch.isBlank()) {
            spec = spec.and(
                    (root, query, cb) -> cb.like(cb.lower(root.get("model")), "%" + modelSearch.toLowerCase() + "%"));
        }

        return repo.findAll(spec, pageable);
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
     * Updates an existing vehicle by its ID.
     *
     * @param id  the vehicle ID
     * @param req the vehicle request data with updated values
     * @return the updated vehicle
     * @throws NotFoundException if vehicle with given ID does not exist
     */
    @Transactional
    public Vehicle update(Long id, VehicleRequest req) {
        Vehicle vehicle = findById(id);
        vehicle.setModel(req.model());
        vehicle.setFirstRegistrationYear(req.firstRegistrationYear());
        vehicle.setCubicCapacity(req.cubicCapacity().intValue());
        vehicle.setFuel(req.fuel());
        vehicle.setMileage(req.mileage().intValue());
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

    /**
     * Generates and saves 10 random vehicles with valid data.
     *
     * @return list of created vehicles
     */
    @Transactional
    public List<Vehicle> seedVehicles() {
        List<String> models = List.of(
                "Audi A4", "BMW 320", "Mercedes C-Class", "VW Golf", "Toyota Corolla",
                "Ford Focus", "Opel Astra", "Škoda Octavia", "Peugeot 308", "Renault Clio");
        Fuel[] fuels = Fuel.values();
        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String model = models.get(ThreadLocalRandom.current().nextInt(models.size()));
            String year = String.valueOf(ThreadLocalRandom.current().nextInt(2000, 2025));
            Integer cubicCapacity = ThreadLocalRandom.current().nextInt(1000, 5001);
            Fuel fuel = fuels[ThreadLocalRandom.current().nextInt(fuels.length)];
            Integer mileage = ThreadLocalRandom.current().nextInt(0, 500001);

            Vehicle vehicle = Vehicle.builder()
                    .model(model)
                    .firstRegistrationYear(year)
                    .cubicCapacity(cubicCapacity)
                    .fuel(fuel)
                    .mileage(mileage)
                    .build();

            vehicles.add(repo.save(vehicle));
        }

        return vehicles;
    }
}