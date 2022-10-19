package com.vi.appointmentservice.api.facade;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.service.calcom.CalComBookingService;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AskerFacade {

  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull CalComBookingService calComBookingService;
  private final @NonNull CalcomRepository calcomRepository;

  public List<CalcomBooking> getAskerActiveBookings(String askerId) {
    if (calcomBookingToAskerRepository.existsByAskerId(askerId)) {
      List<CalcomBookingToAsker> bookingIds = calcomBookingToAskerRepository
          .findByAskerId(askerId);
      List<CalcomBooking> bookings = calComBookingService.getAskerActiveBookings(
          bookingIds.stream().map(CalcomBookingToAsker::getCalcomBookingId).collect(
              Collectors.toList()));
      return bookings;
    } else {
      return new ArrayList<>();
    }
  }

  @Transactional
  public void deleteAskerData(String askerId) {
    List<CalcomBookingToAsker> bookings = calcomBookingToAskerRepository.findByAskerId(askerId);
    bookings.forEach(booking -> {
      CalcomBooking calcomBooking = calComBookingService
          .getBookingById(booking.getCalcomBookingId());
      if (calcomBooking != null) {
        calComBookingService.cancelBooking(calcomBooking.getUid());
        calcomRepository.deleteBooking(booking.getCalcomBookingId());
        calcomRepository.deleteAttendee(booking.getCalcomBookingId());
        calcomBookingToAskerRepository.deleteByCalcomBookingId(booking.getCalcomBookingId());
      }
    });
  }
}
