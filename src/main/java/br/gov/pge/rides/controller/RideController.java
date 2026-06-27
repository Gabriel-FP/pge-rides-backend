package br.gov.pge.rides.controller;

import br.gov.pge.rides.dto.RideRequestDTO;
import br.gov.pge.rides.dto.RideResponseDTO;
import br.gov.pge.rides.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
