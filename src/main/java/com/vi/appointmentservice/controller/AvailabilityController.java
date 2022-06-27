package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.Availability;
import com.vi.appointmentservice.generated.api.controller.AvailabilityApi;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for availability API operations.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "availability")
@Slf4j
public class AvailabilityController implements AvailabilityApi {
    @Override
    public ResponseEntity<Void> deleteAvailability(Long availabilityId) {
        return AvailabilityApi.super.deleteAvailability(availabilityId);
    }

    @Override
    public ResponseEntity<Availability> getAvailabilityById(Long availabilityId) {
        return AvailabilityApi.super.getAvailabilityById(availabilityId);
    }

    @Override
    public ResponseEntity<Void> updateAvailability(Long availabilityId) {
        return AvailabilityApi.super.updateAvailability(availabilityId);
    }
}
