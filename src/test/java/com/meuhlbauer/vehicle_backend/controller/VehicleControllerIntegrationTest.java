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

import java.util.List;

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
    void testGetAllVehiclesPaged() throws Exception {
        // Create multiple vehicles for pagination testing
        for (int i = 1; i <= 25; i++) {
            VehicleRequest request = new VehicleRequest(
                    "Model " + i,
                    "202" + (i % 10),
                    1000L + i,
                    i % 3 == 0 ? Fuel.DIESEL : (i % 3 == 1 ? Fuel.PETROL : Fuel.HYBRID),
                    10000L * i);
            String requestJson = objectMapper.writeValueAsString(request);
            mockMvc.perform(post("/api/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated());
        }

        // Test default pagination (page 0, size 20)
        mockMvc.perform(get("/api/vehicles/paged"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        // Test second page
        mockMvc.perform(get("/api/vehicles/paged?page=1&size=20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));

        // Test custom page size
        mockMvc.perform(get("/api/vehicles/paged?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        // Test sorting by model ascending
        mockMvc.perform(get("/api/vehicles/paged?page=0&size=5&sort=model,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].model").value("Model 1"))
                .andExpect(jsonPath("$.content[1].model").value("Model 10"))
                .andExpect(jsonPath("$.content[2].model").value("Model 11"));

        // Test sorting by model descending
        mockMvc.perform(get("/api/vehicles/paged?page=0&size=5&sort=model,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].model").value("Model 9"))
                .andExpect(jsonPath("$.content[1].model").value("Model 8"))
                .andExpect(jsonPath("$.content[2].model").value("Model 7"));
    }

    @Test
    void testGetAllVehiclesPagedWithFilters() throws Exception {
        // Create test vehicles with specific attributes for filtering
        VehicleRequest vehicle1 = new VehicleRequest("Audi A4", "2020", 2000L, Fuel.DIESEL, 100000L);
        VehicleRequest vehicle2 = new VehicleRequest("BMW 320", "2020", 1800L, Fuel.PETROL, 80000L);
        VehicleRequest vehicle3 = new VehicleRequest("Audi A6", "2021", 3000L, Fuel.DIESEL, 50000L);
        VehicleRequest vehicle4 = new VehicleRequest("Mercedes C-Class", "2021", 2200L, Fuel.HYBRID, 30000L);
        VehicleRequest vehicle5 = new VehicleRequest("VW Golf", "2022", 1600L, Fuel.PETROL, 20000L);

        for (VehicleRequest req : List.of(vehicle1, vehicle2, vehicle3, vehicle4, vehicle5)) {
            String requestJson = objectMapper.writeValueAsString(req);
            mockMvc.perform(post("/api/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated());
        }

        // Test filter by firstRegistrationYear
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=2020"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].firstRegistrationYear").value(
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("2020"))));

        // Test filter by fuel type (DIESEL)
        mockMvc.perform(get("/api/vehicles/paged?fuel=diesel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].fuel").value(
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("diesel"))));

        // Test filter by fuel type (PETROL)
        mockMvc.perform(get("/api/vehicles/paged?fuel=petrol"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].fuel").value(
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("petrol"))));

        // Test filter by fuel type (HYBRID)
        mockMvc.perform(get("/api/vehicles/paged?fuel=hybrid"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fuel").value("hybrid"))
                .andExpect(jsonPath("$.content[0].model").value("Mercedes C-Class"));

        // Test filter by modelSearch (case-insensitive partial match)
        mockMvc.perform(get("/api/vehicles/paged?modelSearch=audi"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        // Test filter by modelSearch with partial match
        mockMvc.perform(get("/api/vehicles/paged?modelSearch=a"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3)) // Audi A4, Audi A6, Mercedes
                // C-Class
                .andExpect(jsonPath("$.totalElements").value(3));

        // Test combination of filters: year=2021 AND fuel=diesel
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=2021&fuel=diesel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].model").value("Audi A6"))
                .andExpect(jsonPath("$.content[0].firstRegistrationYear").value("2021"))
                .andExpect(jsonPath("$.content[0].fuel").value("diesel"));

        // Test combination of filters: modelSearch=audi AND fuel=diesel
        mockMvc.perform(get("/api/vehicles/paged?modelSearch=audi&fuel=diesel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        // Test combination of all filters
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=2020&fuel=diesel&modelSearch=audi"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].model").value("Audi A4"))
                .andExpect(jsonPath("$.content[0].firstRegistrationYear").value("2020"))
                .andExpect(jsonPath("$.content[0].fuel").value("diesel"));

        // Test filter with no matches
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=2019"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        // Test range format for firstRegistrationYear (2020-2021)
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=2020-2021"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[*].firstRegistrationYear").value(
                        org.hamcrest.Matchers.everyItem(
                                org.hamcrest.Matchers.anyOf(
                                        org.hamcrest.Matchers.is("2020"),
                                        org.hamcrest.Matchers.is("2021")))));

        // Test range format for firstRegistrationYear (2020-2022) - should include all
        // vehicles
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=2020-2022"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(5));

        // Test range format with combination of filters: year range 2020-2021 AND
        // fuel=diesel
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=2020-2021&fuel=diesel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].fuel").value(
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("diesel"))));

        // Test invalid range format (empty start year) - should be ignored
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=-2021"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(5)); // All vehicles returned

        // Test invalid range format (empty end year) - should be ignored
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=2020-"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(5)); // All vehicles returned

        // Test invalid range format (non-numeric) - should be ignored
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=abc-def"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(5)); // All vehicles returned

        // Test range format with same start and end year (e.g., 2020-2020) - should be
        // treated as exact year
        mockMvc.perform(get("/api/vehicles/paged?firstRegistrationYear=2020-2020"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].firstRegistrationYear").value(
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("2020"))));

        // Test filters with pagination and sorting
        mockMvc.perform(get("/api/vehicles/paged?fuel=diesel&page=0&size=1&sort=model,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].model").value("Audi A4"));

        // Test invalid fuel type (should return 400 Bad Request)
        mockMvc.perform(get("/api/vehicles/paged?fuel=electric"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.code").value("INVALID_ENUM"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void testGetVehicleById() throws Exception {
        // First create a vehicle
        VehicleRequest request = new VehicleRequest(
                "Audi A4",
                "2020",
                2000L,
                Fuel.DIESEL,
                120000L);

        String requestJson = objectMapper.writeValueAsString(request);

        String createResponse = mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        VehicleResponse created = objectMapper.readValue(createResponse, VehicleResponse.class);
        Long vehicleId = created.id();

        // Test successful retrieval by ID
        mockMvc.perform(get("/api/vehicles/{id}", vehicleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.model").value("Audi A4"))
                .andExpect(jsonPath("$.firstRegistrationYear").value("2020"))
                .andExpect(jsonPath("$.cubicCapacity").value(2000))
                .andExpect(jsonPath("$.fuel").value("diesel"))
                .andExpect(jsonPath("$.mileage").value(120000));

        // Test retrieval of non-existent vehicle (404)
        mockMvc.perform(get("/api/vehicles/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/vehicles/999"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.resource").value("vehicle"))
                .andExpect(jsonPath("$.resourceId").value("999"))
                .andExpect(jsonPath("$.traceId").exists());

        // Test retrieval with invalid type (string instead of Long)
        mockMvc.perform(get("/api/vehicles/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid parameter type"))
                .andExpect(jsonPath("$.instance").value("/api/vehicles/abc"))
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.id").exists());
    }

    @Test
    void testCreateVehicle() throws Exception {
        // Test POST /api/vehicles with valid data
        VehicleRequest request = new VehicleRequest(
                "Audi A4",
                "2020",
                2000L,
                Fuel.DIESEL,
                120000L);

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
                2000L,
                Fuel.PETROL,
                50000L);

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
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/vehicles/999"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.resource").value("vehicle"))
                .andExpect(jsonPath("$.resourceId").value("999"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void testCreateVehicleWithInvalidData() throws Exception {
        // Test POST /api/vehicles with invalid data - Bean Validation check
        VehicleRequest invalidRequest = new VehicleRequest(
                "", // empty model - @NotBlank
                "20", // invalid year format - @Pattern regexp="\\d{4}"
                0L, // invalid cubic capacity - @Min(1)
                null, // null fuel - @NotNull
                -1L // invalid mileage - @Min(0)
        );

        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.instance").value("/api/vehicles"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.model").exists())
                .andExpect(jsonPath("$.errors.firstRegistrationYear").exists())
                .andExpect(jsonPath("$.errors.cubicCapacity").exists())
                .andExpect(jsonPath("$.errors.fuel").exists())
                .andExpect(jsonPath("$.errors.mileage").exists());
    }

    @Test
    void testUpdateVehicle() throws Exception {
        // First create a vehicle
        VehicleRequest createRequest = new VehicleRequest(
                "BMW 320",
                "2021",
                2000L,
                Fuel.PETROL,
                50000L);

        String createRequestJson = objectMapper.writeValueAsString(createRequest);

        String createResponse = mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        VehicleResponse created = objectMapper.readValue(createResponse, VehicleResponse.class);
        Long vehicleId = created.id();

        // Test successful update
        VehicleRequest updateRequest = new VehicleRequest(
                "BMW 330",
                "2022",
                3000L,
                Fuel.HYBRID,
                75000L);

        String updateRequestJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/vehicles/{id}", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.model").value("BMW 330"))
                .andExpect(jsonPath("$.firstRegistrationYear").value("2022"))
                .andExpect(jsonPath("$.cubicCapacity").value(3000))
                .andExpect(jsonPath("$.fuel").value("hybrid"))
                .andExpect(jsonPath("$.mileage").value(75000));

        // Verify that vehicle is updated in database
        assertThat(vehicleRepository.count()).isEqualTo(1);
        var updatedVehicle = vehicleRepository.findById(vehicleId);
        assertThat(updatedVehicle).isPresent();
        assertThat(updatedVehicle.get().getModel()).isEqualTo("BMW 330");
        assertThat(updatedVehicle.get().getFirstRegistrationYear()).isEqualTo("2022");
        assertThat(updatedVehicle.get().getCubicCapacity()).isEqualTo(3000);
        assertThat(updatedVehicle.get().getFuel()).isEqualTo(Fuel.HYBRID);
        assertThat(updatedVehicle.get().getMileage()).isEqualTo(75000);

        // Test update of non-existent vehicle (404)
        mockMvc.perform(put("/api/vehicles/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/vehicles/999"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.resource").value("vehicle"))
                .andExpect(jsonPath("$.resourceId").value("999"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void testUpdateVehicleWithInvalidData() throws Exception {
        // First create a vehicle
        VehicleRequest createRequest = new VehicleRequest(
                "Audi A4",
                "2020",
                2000L,
                Fuel.DIESEL,
                120000L);

        String createRequestJson = objectMapper.writeValueAsString(createRequest);

        String createResponse = mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        VehicleResponse created = objectMapper.readValue(createResponse, VehicleResponse.class);
        Long vehicleId = created.id();

        // Test PUT /api/vehicles/{id} with invalid data - Bean Validation check
        VehicleRequest invalidRequest = new VehicleRequest(
                "", // empty model - @NotBlank
                "20", // invalid year format - @Pattern regexp="\\d{4}"
                0L, // invalid cubic capacity - @Min(1)
                null, // null fuel - @NotNull
                -1L // invalid mileage - @Min(0)
        );

        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(put("/api/vehicles/{id}", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.instance").value("/api/vehicles/" + vehicleId))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.model").exists())
                .andExpect(jsonPath("$.errors.firstRegistrationYear").exists())
                .andExpect(jsonPath("$.errors.cubicCapacity").exists())
                .andExpect(jsonPath("$.errors.fuel").exists())
                .andExpect(jsonPath("$.errors.mileage").exists());
    }

    @Test
    void testDeleteVehicleWithInvalidType() throws Exception {
        // Test DELETE /api/vehicles/{id} with invalid type (string instead of Long)
        mockMvc.perform(delete("/api/vehicles/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid parameter type"))
                .andExpect(jsonPath("$.instance").value("/api/vehicles/abc"))
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.id").exists());
    }

    @Test
    void testSeedVehicles() throws Exception {
        // Test POST /api/vehicles/seed - should create 10 random vehicles
        long initialCount = vehicleRepository.count();

        mockMvc.perform(post("/api/vehicles/seed"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].model").exists())
                .andExpect(jsonPath("$[0].firstRegistrationYear").exists())
                .andExpect(jsonPath("$[0].cubicCapacity").exists())
                .andExpect(jsonPath("$[0].fuel").exists())
                .andExpect(jsonPath("$[0].mileage").exists())
                .andExpect(jsonPath("$[9].id").exists());

        // Verify that vehicles are saved in database
        assertThat(vehicleRepository.count()).isEqualTo(initialCount + 10);
    }
}
