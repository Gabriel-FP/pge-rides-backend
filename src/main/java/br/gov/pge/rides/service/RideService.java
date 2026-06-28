package br.gov.pge.rides.service;

import br.gov.pge.rides.dto.RideRequestDTO;
import br.gov.pge.rides.dto.RideResponseDTO;
import br.gov.pge.rides.mapper.RideMapper;
import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideService {

    private final RideRepository repository;
    private final RideMapper mapper;

    public RideService(RideRepository repository, RideMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public RideResponseDTO create(RideRequestDTO request) {
        Ride ride = mapper.toEntity(request);
        Ride saved = repository.save(ride);
        return mapper.toResponse(saved);
    }

    public List<RideResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
