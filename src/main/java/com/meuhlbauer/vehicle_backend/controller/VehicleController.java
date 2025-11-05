package com.meuhlbauer.vehicle_backend.controller;

import com.meuhlbauer.vehicle_backend.dto.VehicleRequest;
import com.meuhlbauer.vehicle_backend.dto.VehicleResponse;
import com.meuhlbauer.vehicle_backend.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "Create a new vehicle", description = "Creates a new vehicle with the provided data")
    @ApiResponse(responseCode = "201", description = "Vehicle successfully created")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationErrorProblem")
    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest req) {
        var created = vehicleService.create(req);
        return ResponseEntity.created(URI.create("/api/vehicles/" + created.getId()))
                .body(VehicleResponse.from(created));
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