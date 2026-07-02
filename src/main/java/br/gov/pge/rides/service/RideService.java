package br.gov.pge.rides.service;

import br.gov.pge.rides.cache.RideCacheService;
import br.gov.pge.rides.dto.RideRequestDTO;
import br.gov.pge.rides.dto.RideResponseDTO;
import br.gov.pge.rides.exception.ActiveRideAlreadyExistsException;
import br.gov.pge.rides.exception.RideNotAvailableException;
import br.gov.pge.rides.exception.RideNotFoundException;
import br.gov.pge.rides.mapper.RideMapper;
import br.gov.pge.rides.messaging.RideMessage;
import br.gov.pge.rides.messaging.RideProducer;
import br.gov.pge.rides.messaging.RideStatusUpdate;
import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.model.enums.RideStatus;
import br.gov.pge.rides.notification.ClientNotificationService;
import br.gov.pge.rides.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideService {

    private static final List<RideStatus> ACTIVE_STATUSES =
            List.of(RideStatus.WAITING, RideStatus.IN_PROGRESS);

    private final RideRepository repository;
    private final RideMapper mapper;
    private final RideProducer producer;
    private final RideCacheService cache;
    private final ClientNotificationService clientNotificationService;

    public RideService(RideRepository repository, RideMapper mapper,
                       RideProducer producer, RideCacheService cache,
                       ClientNotificationService clientNotificationService) {
        this.repository = repository;
        this.mapper = mapper;
        this.producer = producer;
        this.cache = cache;
        this.clientNotificationService = clientNotificationService;
    }

    public RideResponseDTO create(RideRequestDTO request) {
        if (repository.existsByUserIdAndStatusIn(request.userId(), ACTIVE_STATUSES)) {
            throw new ActiveRideAlreadyExistsException(request.userId());
        }

        Ride ride = mapper.toEntity(request);
        Ride saved = repository.save(ride);

        producer.publishRideCreated(
                new RideMessage(saved.getId(), saved.getUserId(), saved.getOrigin(), saved.getDestination())
        );

        return mapper.toResponse(saved);
    }

    public RideResponseDTO accept(Long rideId, Long driverId) {
        if (repository.existsByDriverIdAndStatus(driverId, RideStatus.IN_PROGRESS)) {
            throw new RideNotAvailableException("Driver " + driverId + " already has an active ride");
        }

        Ride ride = repository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId));

        if (ride.getStatus() != RideStatus.WAITING) {
            throw new RideNotAvailableException(rideId);
        }

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.IN_PROGRESS);
        Ride saved = repository.save(ride);

        RideResponseDTO response = mapper.toResponse(saved);
        cache.save(response);

        clientNotificationService.notifyStatusChange(
                new RideStatusUpdate(saved.getId(), saved.getUserId(), RideStatus.IN_PROGRESS, driverId));

        return response;
    }

    public RideResponseDTO finish(Long rideId, Long driverId) {
        Ride ride = repository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId));

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new RideNotAvailableException("Ride " + rideId + " is not in progress");
        }

        if (!driverId.equals(ride.getDriverId())) {
            throw new RideNotAvailableException("Driver " + driverId + " is not assigned to ride " + rideId);
        }

        ride.setStatus(RideStatus.COMPLETED);
        Ride saved = repository.save(ride);

        RideResponseDTO response = mapper.toResponse(saved);
        cache.save(response);

        clientNotificationService.notifyStatusChange(
                new RideStatusUpdate(saved.getId(), saved.getUserId(), RideStatus.COMPLETED, driverId));

        return response;
    }

    public RideResponseDTO getStatus(Long rideId) {
        return cache.find(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId));
    }

    public List<RideResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
