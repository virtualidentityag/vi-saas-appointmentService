package com.vi.appointmentservice.api.calcom.service;

import static org.junit.jupiter.api.Assertions.*;

import com.vi.appointmentservice.api.calcom.repository.BookingRepository;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.service.onlineberatung.VideoAppointmentService;
import com.vi.appointmentservice.helper.RescheduleHelper;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalComBookingServiceTest {

  @InjectMocks
  CalComBookingService calComBookingService;

  @Mock
  RescheduleHelper rescheduleHelper;

  @Mock
  BookingRepository bookingRepository;
  @Mock
  CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  @Mock
  CalcomLocationsService calcomLocationsService;

  @Mock
  VideoAppointmentService videoAppointmentService;
  @Test
  void should_Not_OverrideDescriptionWithCancellationReason_When_CurrentDescriptionIsNotEmpty() {
    // given
    CalcomBooking calcomBooking = new CalcomBooking().description("description").cancellationReason("cancellationReason");

    // when, then
    assertFalse(calComBookingService.shouldOverrideDescriptionWithCancellationReason(calcomBooking));
  }
  @Test
  void should_OverridDescriptionWithCancellationReason_When_CurrentDescriptionIsEmpty() {
    // given
    CalcomBooking calcomBooking = new CalcomBooking().description("").cancellationReason("cancellationReason");

    // when, then
    assertTrue(calComBookingService.shouldOverrideDescriptionWithCancellationReason(calcomBooking));
  }
}