package com.vi.appointmentservice.helper;

import com.vi.appointmentservice.api.calcom.model.CalcomTeam;
import com.vi.appointmentservice.api.calcom.model.CalcomUser;
import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.calcom.service.CalComTeamService;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.calcom.service.CalComUserService;
import com.vi.appointmentservice.api.service.onlineberatung.AdminUserService;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RescheduleHelper {

  private final @NonNull CalComUserService calComUserService;
  private final @NonNull AdminUserService adminUserService;
  private final @NonNull UserToConsultantRepository userToConsultantRepository;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull CalComTeamService calComTeamService;
  private final @NonNull CalcomEventTypeService calcomEventTypeService;

  public CalcomBooking attachRescheduleLink(CalcomBooking calcomBooking) {
    CalcomUser registeredCalcomUser = this.calComUserService
        .getUserById(Long.valueOf(calcomBooking.getUserId()));
    var teamId = getTeamIdForBooking(calcomBooking);
    String slug = null;
    if (teamId.isPresent()) {
      CalcomTeam team = calComTeamService.getTeamById(teamId.get());
      slug = "team/" + team.getSlug();
    } else {
      slug = registeredCalcomUser.getUsername();
    }

    return attachRescheduleLinkIfEventTypeIsFound(calcomBooking, slug);
  }

  private CalcomBooking attachRescheduleLinkIfEventTypeIsFound(CalcomBooking calcomBooking, String slug) {
    var optionalEventType = this.calcomEventTypeService.findEventTypeById(
        Long.valueOf(calcomBooking.getEventTypeId()));

    if (optionalEventType.isEmpty()) {
      log.warn("EventType not found for bookingId " + calcomBooking.getId());
      return calcomBooking;
    } else {
      calcomBooking.setRescheduleLink(
          "/" + slug + "/" + optionalEventType.get().getSlug() + "?rescheduleUid=" + calcomBooking.getUid());
    }
    return calcomBooking;
  }

  private Optional<Number> getTeamIdForBooking(CalcomBooking calcomBooking) {
    Optional<CalcomEventType> eventType = calcomEventTypeService
        .findEventTypeById(Long.valueOf(calcomBooking.getEventTypeId()));
    if (eventType.isEmpty() || eventType.get().getTeamId() == null) {
      return Optional.empty();
    } else {
      return Optional.of(eventType.get().getTeamId());
    }
  }

  public void attachConsultantName(List<CalcomBooking> bookings) {
    Map<Integer, String> bookingUserIdConsultantId = new HashMap<>();
    bookings.stream().forEach(booking -> {
      Optional<CalcomUserToConsultant> calcomUserToConsultant = this.userToConsultantRepository
          .findByCalComUserId(
              Long.valueOf(booking.getUserId()));
      if (calcomUserToConsultant.isPresent()) {
        bookingUserIdConsultantId
            .put(booking.getUserId(), calcomUserToConsultant.get().getConsultantId());
      } else {
        bookingUserIdConsultantId.put(booking.getUserId(), null);
      }
    });

    Map<String, String> consultantNamesForIds = this.adminUserService
        .getConsultantNamesForIds(bookingUserIdConsultantId.values());

    bookings.stream().forEach(booking -> {
      if (bookingUserIdConsultantId.get(booking.getUserId()) != null) {
        booking.setConsultantName(
            consultantNamesForIds.get(bookingUserIdConsultantId.get(booking.getUserId())));
      } else {
        booking.setConsultantName("Unknown name");
        log.error("Unknown asker name for bookingId " + booking.getId());
      }
    });

  }

  public void attachAskerNames(List<CalcomBooking> calcomBookings) {
    Map<Long, String> bookingIdAskerId = new HashMap<>();
    calcomBookings.stream().forEach(booking -> {
      Optional<CalcomBookingToAsker> byCalcomBookingId = this.calcomBookingToAskerRepository
          .findByCalcomBookingId(
              booking.getId());
      if (byCalcomBookingId.isPresent()) {
          bookingIdAskerId.put(booking.getId(), byCalcomBookingId.get().getAskerId());
      } else {
        if (booking.getMetadataUserId() != null) {
          bookingIdAskerId.put(booking.getId(), booking.getMetadataUserId());
        } else {
          log.info("No asker found for bookingId " + booking.getId());
        }
      }
    });

    Map<String, String> askerUserNamesForIds = this.adminUserService
        .getAskerUserNamesForIds(bookingIdAskerId.values());
    calcomBookings.stream().forEach(booking ->{
      String askerId = getAskerId(booking);
      if (askerUserNamesForIds.get(askerId) != null) {
        booking.setAskerName(askerUserNamesForIds.get(askerId));
      } else {
        booking.setAskerName("Unknown name");
      }
    });

  }

  private String getAskerId(CalcomBooking booking) {
    return booking.getAskerId() == null ? booking.getMetadataUserId() : booking.getAskerId();
  }

}
