package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.location.CreateLocationRequest;
import com.busline.tranmaunhan.dto.location.LocationResponse;

import java.util.List;

public interface LocationService {

    List<LocationResponse> getAllLocations();

    LocationResponse createLocation(CreateLocationRequest request);
}
