package com.vi.appointmentservice.api.facade;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.helper.RescheduleHelper;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AskerFacade {

  private final @NonNull RescheduleHelper rescheduleHelper;
  private final @NonNull CalcomRepository calcomRepository;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;

  public List<CalcomBooking> getAllBookingsOfAskerHandler(String askerId) {
    if (calcomBookingToAskerRepository.existsByAskerId(askerId)) {
      List<CalcomBookingToAsker> bookingIds = calcomBookingToAskerRepository
          .findByAskerId(askerId);
      List<CalcomBooking> bookings = calcomRepository
          .getByIds(bookingIds.stream().map(el -> el.getCalcomBookingId()).collect(
              Collectors.toList()));
      for (CalcomBooking booking : bookings) {
        rescheduleHelper.attachRescheduleLink(booking);
        rescheduleHelper.attachConsultantName(booking);
      }
      return bookings;
    } else {
      return new ArrayList<>();
    }
  }
}
