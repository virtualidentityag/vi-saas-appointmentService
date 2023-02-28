package com.vi.appointmentservice.api.calcom.service;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.calcom.repository.BookingRepository;
import com.vi.appointmentservice.helper.RescheduleHelper;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalComBookingService {

  private final @NonNull RescheduleHelper rescheduleHelper;
  private final @NonNull BookingRepository bookingRepository;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull CalcomLocationsService calcomLocationsService;

  public List<CalcomBooking> getConsultantActiveBookings(Long consultantId) {
    return enrichConsultantResultSet(bookingRepository.getConsultantActiveBookings(consultantId));
  }

  public List<CalcomBooking> getConsultantExpiredBookings(Long consultantId) {
    return enrichConsultantResultSet(bookingRepository.getConsultantExpiredBookings(consultantId));
  }

  public List<CalcomBooking> getConsultantCancelledBookings(Long consultantId) {
    return enrichConsultantResultSet(
        bookingRepository.getConsultantCancelledBookings(consultantId));
  }

  private List<CalcomBooking> enrichConsultantResultSet(List<CalcomBooking> bookings) {
    for (CalcomBooking booking : bookings) {
      Optional<CalcomBookingToAsker> calcomBookingAsker = calcomBookingToAskerRepository
          .findByCalcomBookingId(
              booking.getId());
      if (!calcomBookingAsker.isPresent()) {
        log.error("Inconsistent data. Asker not found for booking + " + booking.getId());
        continue;
      }
      CalcomBookingToAsker entity = calcomBookingAsker.get();
      booking.setVideoAppointmentId(entity.getVideoAppointmentId());
      booking.setAskerId(entity.getAskerId());
      booking.setLocation(calcomLocationsService.resolveLocationType(booking));
      rescheduleHelper.attachRescheduleLink(booking);
    }
    rescheduleHelper.attachAskerNames(bookings);
    return bookings;
  }

  public List<CalcomBooking> getAskerActiveBookings(List<Long> bookingIds) {
    return enrichAskerResultSet(bookingRepository.getAskerActiveBookings(bookingIds));
  }

  List<CalcomBooking> enrichAskerResultSet(List<CalcomBooking> bookings) {
    for (CalcomBooking booking : bookings) {
      Optional<CalcomBookingToAsker> calcomBookingAsker = calcomBookingToAskerRepository
          .findByCalcomBookingId(
              booking.getId());
      if (!calcomBookingAsker.isPresent()) {
        log.error("Inconsistent data. Asker not found for booking + " + booking.getId());
        continue;
      }
      CalcomBookingToAsker entity = calcomBookingAsker.get();
      booking.setAskerId(entity.getAskerId());
      booking.setLocation(calcomLocationsService.resolveLocationType(booking));
      booking.setVideoAppointmentId(entity.getVideoAppointmentId());
      rescheduleHelper.attachRescheduleLink(booking);
    }
    rescheduleHelper.attachConsultantName(bookings);
    return bookings;
  }

  public CalcomBooking getBookingById(Long bookingId) {
    return bookingRepository.getBookingById(bookingId);
  }

  public void cancelBooking(String bookingUid) {
    bookingRepository.cancelBooking(bookingUid);
  }


  public CalcomBooking getBookingByUid(String bookingUid) {
    return bookingRepository.getBookingByUid(bookingUid);
  }
}
