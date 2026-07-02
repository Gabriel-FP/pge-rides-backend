package br.gov.pge.rides.controller;

import br.gov.pge.rides.dto.RideRequestDTO;
import br.gov.pge.rides.dto.RideResponseDTO;
import br.gov.pge.rides.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rides")
public class RideController {

    private final RideService service;

    public RideController(RideService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RideResponseDTO> create(@Valid @RequestBody RideRequestDTO request) {
        RideResponseDTO created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<RideResponseDTO> findAll() {
        return service.findAll();
    }

    // A driver accepts a ride: links the driver and moves it to IN_PROGRESS.
    @PatchMapping("/{id}/accept")
    public RideResponseDTO accept(@PathVariable Long id, @RequestParam Long driverId) {
        return service.accept(id, driverId);
    }

    @PatchMapping("/{id}/finish")
    public RideResponseDTO finish(@PathVariable Long id, @RequestParam Long driverId) {
        return service.finish(id, driverId);
    }

    @PatchMapping("/{id}/cancel-by-client")
    public RideResponseDTO cancelByClient(@PathVariable Long id, @RequestParam Long userId) {
        return service.cancelByClient(id, userId);
    }

    @PatchMapping("/{id}/cancel-by-driver")
    public RideResponseDTO cancelByDriver(@PathVariable Long id, @RequestParam Long driverId) {
        return service.cancelByDriver(id, driverId);
    }

    // Reads the in-progress ride straight from Redis (for queries).
    @GetMapping("/{id}/status")
    public RideResponseDTO status(@PathVariable Long id) {
        return service.getStatus(id);
    }
}
