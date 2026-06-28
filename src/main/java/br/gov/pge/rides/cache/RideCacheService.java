package br.gov.pge.rides.cache;

import br.gov.pge.rides.dto.RideResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RideCacheService {

    private static final Logger log = LoggerFactory.getLogger(RideCacheService.class);
    private static final String KEY_PREFIX = "ride:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RideCacheService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    // Stores the in-progress ride in Redis so it can be queried without hitting Postgres.
    public void save(RideResponseDTO ride) {
        try {
            String json = objectMapper.writeValueAsString(ride);
            redis.opsForValue().set(KEY_PREFIX + ride.id(), json);
            log.info("Ride {} cached in Redis (IN_PROGRESS)", ride.id());
        } catch (Exception ex) {
            log.error("Failed to cache ride {} in Redis", ride.id(), ex);
        }
    }

    public Optional<RideResponseDTO> find(Long rideId) {
        String json = redis.opsForValue().get(KEY_PREFIX + rideId);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, RideResponseDTO.class));
        } catch (Exception ex) {
            log.error("Failed to read ride {} from Redis", rideId, ex);
            return Optional.empty();
        }
    }
}
