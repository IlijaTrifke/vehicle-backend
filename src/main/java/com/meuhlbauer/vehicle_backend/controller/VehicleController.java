package com.meuhlbauer.vehicle_backend.controller;

import com.meuhlbauer.vehicle_backend.domain.Vehicle;
import com.meuhlbauer.vehicle_backend.dto.VehicleRequest;
import com.meuhlbauer.vehicle_backend.dto.VehicleResponse;
import com.meuhlbauer.vehicle_backend.enums.Fuel;
import com.meuhlbauer.vehicle_backend.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle management API")
@ApiResponses({
        @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalErrorProblem")
})
public class VehicleController {
    private final VehicleService vehicleService;

    @Operation(summary = "Get all vehicles", description = "Retrieves a list of all vehicles")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of vehicles")
    @GetMapping
    public List<VehicleResponse> getAll() {
        return vehicleService.list().stream().map(VehicleResponse::from).toList();
    }

    @Operation(summary = "Get all vehicles (paginated)", description = "Retrieves a paginated list of vehicles. " +
            "Supports query parameters: page (default: 0), size (default: 20), sort (e.g., sort=model,asc). "
            +
            "Optional filters: firstRegistrationYear (exact year or range format: YYYY-YYYY, e.g., 2000-2024), fuel "
            +
            "(diesel/petrol/hybrid), modelSearch (partial match)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list of vehicles")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/InvalidEnumProblem")
    @GetMapping("/paged")
    public ResponseEntity<Page<VehicleResponse>> getAllPaged(
            @Parameter(description = "Filter by first registration year (exact year, e.g., 2020, or range format, e.g"
                    +
                    "., 2000-2024)") @RequestParam(required = false) String firstRegistrationYear,
            @Parameter(description = "Filter by fuel type (diesel, petrol, or hybrid)") @RequestParam(required =
                    false) String fuel,
            @Parameter(description = "Search by model name (case-insensitive partial match)") @RequestParam(required
                    = false) String modelSearch,
            @PageableDefault(size = 20) Pageable pageable) {
        Fuel fuelEnum = null;
        if (fuel != null && !fuel.isBlank()) {
            fuelEnum = Fuel.from(fuel);
        }
        Page<VehicleResponse> page = vehicleService
                .findAllWithFilters(firstRegistrationYear, fuelEnum, modelSearch, pageable)
                .map(VehicleResponse::from);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get a vehicle by ID", description = "Retrieves a single vehicle by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/TypeMismatchProblem")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFoundProblem")
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.findById(id);
        return ResponseEntity.ok(VehicleResponse.from(vehicle));
    }

    @Operation(summary = "Create a new vehicle", description = "Creates a new vehicle with the provided data")
    @ApiResponse(responseCode = "201", description = "Vehicle successfully created")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationErrorProblem")
    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest req) {
        Vehicle created = vehicleService.create(req);
        return ResponseEntity.created(URI.create("/api/vehicles/" + created.getId()))
                .body(VehicleResponse.from(created));
    }

    @Operation(summary = "Seed vehicles", description = "Generates and saves 10 random vehicles with valid data")
    @ApiResponse(responseCode = "201", description = "10 vehicles successfully created")
    @PostMapping("/seed")
    public ResponseEntity<List<VehicleResponse>> seed() {
        List<VehicleResponse> vehicles = vehicleService.seedVehicles().stream()
                .map(VehicleResponse::from)
                .toList();
        return ResponseEntity.status(201).body(vehicles);
    }

    @Operation(summary = "Update a vehicle", description = "Updates an existing vehicle by its ID with the provided "
            +
            "data")
    @ApiResponse(responseCode = "200", description = "Vehicle successfully updated")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationErrorProblem")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFoundProblem")
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id, @Valid @RequestBody VehicleRequest req) {
        Vehicle updated = vehicleService.update(id, req);
        return ResponseEntity.ok(VehicleResponse.from(updated));
    }

    @Operation(summary = "Delete a vehicle", description = "Deletes a vehicle by its ID")
    @ApiResponse(responseCode = "204", description = "Vehicle successfully deleted")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/TypeMismatchProblem")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFoundProblem")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}