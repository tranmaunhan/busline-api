package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.dto.location.LocationResponse;
import com.busline.tranmaunhan.entity.Locations;
import com.busline.tranmaunhan.repository.LocationsRepository;
import com.busline.tranmaunhan.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationsRepository locationsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getAllLocations() {
        return locationsRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private LocationResponse toResponse(Locations location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getType()
        );
    }
}
