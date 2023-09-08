package com.vi.appointmentservice.api.calcom.service;

import com.vi.appointmentservice.api.calcom.repository.BookingRepository;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.service.onlineberatung.VideoAppointmentService;
import com.vi.appointmentservice.appointmentservice.generated.web.model.Appointment;
import com.vi.appointmentservice.helper.RescheduleHelper;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalComBookingService {

  private final @NonNull RescheduleHelper rescheduleHelper;
  private final @NonNull BookingRepository bookingRepository;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull CalcomLocationsService calcomLocationsService;

  private final @NonNull VideoAppointmentService videoAppointmentService;

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
      if (shouldOverrideDescriptionWithCancellationReason(booking)) {
        booking.setDescription(booking.getCancellationReason());
      }
      Optional<CalcomBookingToAsker> calcomBookingAsker = calcomBookingToAskerRepository
          .findByCalcomBookingId(
              booking.getId());

      if (!calcomBookingAsker.isPresent()) {
        log.warn("Inconsistent data. Asker not found for booking. Trying to fix consistency for bookingId " + booking.getId());
        recreateBookingToAskerRelation(booking);
        calcomBookingAsker = calcomBookingToAskerRepository
            .findByCalcomBookingId(
                booking.getId());
      }
      if (!calcomBookingAsker.isPresent()) {
        log.error("Inconsistent data. Asker not found for bookingId + " + booking.getId());
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

  boolean shouldOverrideDescriptionWithCancellationReason(CalcomBooking booking) {
    return booking.getCancellationReason() != null && StringUtils.isEmpty(booking.getDescription());
  }

  private void recreateBookingToAskerRelation(CalcomBooking booking) {
    Optional<Appointment> appointmentByBookingId = videoAppointmentService.findAppointmentByBookingId(
        booking.getId().intValue());
    if (appointmentByBookingId.isPresent()) {
      String askerId = booking.getAskerId() != null ? booking.getAskerId() : booking.getMetadataUserId();

      if (askerId != null) {
        CalcomBookingToAsker userAssociation = new CalcomBookingToAsker(booking.getId(), askerId,
            appointmentByBookingId.get().getId().toString());
        calcomBookingToAskerRepository.save(userAssociation);
        log.info("Inserted missing booking to asker relation for booking " + booking.getId());
      }
      else {
        log.info("Could not insert missing booking to asker relation for booking " + booking.getId() + " because askerId is null");
      }
    }
  }

  public List<CalcomBooking> getAskerActiveBookings(List<Long> bookingIds) {
    return enrichAskerResultSet(bookingRepository.getAskerActiveBookings(bookingIds));
  }

  List<CalcomBooking> enrichAskerResultSet(List<CalcomBooking> bookings) {
    for (CalcomBooking booking : bookings) {
      if (shouldOverrideDescriptionWithCancellationReason(booking)) {
        booking.setDescription(booking.getCancellationReason());
      }
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
