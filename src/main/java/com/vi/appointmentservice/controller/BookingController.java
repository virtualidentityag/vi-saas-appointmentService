package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.Booking;
import com.vi.appointmentservice.generated.api.controller.BookingApi;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for booking API operations.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "booking")
@Slf4j
public class BookingController implements BookingApi {

    @Override
    public ResponseEntity<Booking> getBookingById(Long bookingId) {
        return BookingApi.super.getBookingById(bookingId);
    }
}
