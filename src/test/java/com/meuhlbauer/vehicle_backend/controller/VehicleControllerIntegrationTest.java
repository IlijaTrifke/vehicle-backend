package com.meuhlbauer.vehicle_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meuhlbauer.vehicle_backend.dto.VehicleRequest;
import com.meuhlbauer.vehicle_backend.dto.VehicleResponse;
import com.meuhlbauer.vehicle_backend.enums.Fuel;
import com.meuhlbauer.vehicle_backend.repository.VehicleJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VehicleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleJpaRepository vehicleRepository;

    @Test
    void testGetAllVehicles() throws Exception {
        // Test GET /api/vehicles - should return empty list or list of vehicles
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreateVehicle() throws Exception {
        // Test POST /api/vehicles with valid data
        VehicleRequest request = new VehicleRequest(
                "Audi A4",
                "2020",
                2000,
                Fuel.DIESEL,
                120000);

        String requestJson = objectMapper.writeValueAsString(request);

        String createResponse = mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", startsWith("/api/vehicles/")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.model").value("Audi A4"))
                .andExpect(jsonPath("$.firstRegistrationYear").value("2020"))
                .andExpect(jsonPath("$.cubicCapacity").value(2000))
                .andExpect(jsonPath("$.fuel").value("diesel"))
                .andExpect(jsonPath("$.mileage").value(120000))
                .andReturn()
                .getResponse()
                .getContentAsString();

        VehicleResponse created = objectMapper.readValue(createResponse, VehicleResponse.class);
        Long vehicleId = created.id();

        // Verify that vehicle is saved in database
        assertThat(vehicleRepository.count()).isEqualTo(1);

        // Verify that vehicle appears in the list
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==%d)]", vehicleId).exists());
    }

    @Test
    void testDeleteVehicle() throws Exception {
        // First create a vehicle
        VehicleRequest request = new VehicleRequest(
                "BMW 320",
                "2021",
                2000,
                Fuel.PETROL,
                50000);

        String requestJson = objectMapper.writeValueAsString(request);

        // Create vehicle
        String createResponse = mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        VehicleResponse created = objectMapper.readValue(createResponse, VehicleResponse.class);
        Long vehicleId = created.id();

        // Test successful deletion
        mockMvc.perform(delete("/api/vehicles/{id}", vehicleId))
                .andExpect(status().isNoContent());

        // Verify that vehicle is deleted from database
        assertThat(vehicleRepository.count()).isEqualTo(0);

        // Test deletion of non-existent vehicle (404)
        mockMvc.perform(delete("/api/vehicles/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateVehicleWithInvalidData() throws Exception {
        // Test POST /api/vehicles with invalid data - Bean Validation check
        VehicleRequest invalidRequest = new VehicleRequest(
                "", // empty model - @NotBlank
                "20", // invalid year format - @Pattern regexp="\\d{4}"
                0, // invalid cubic capacity - @Positive
                null, // null fuel - @NotNull
                -1 // invalid mileage - @PositiveOrZero
        );

        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.model").exists())
                .andExpect(jsonPath("$.errors.firstRegistrationYear").exists())
                .andExpect(jsonPath("$.errors.cubicCapacity").exists())
                .andExpect(jsonPath("$.errors.fuel").exists())
                .andExpect(jsonPath("$.errors.mileage").exists());
    }
}
