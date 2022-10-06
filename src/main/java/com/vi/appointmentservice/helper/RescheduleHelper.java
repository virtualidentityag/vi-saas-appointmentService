package com.vi.appointmentservice.helper;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.CalcomTeam;
import com.vi.appointmentservice.api.model.CalcomUser;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.calcom.CalComUserService;
import com.vi.appointmentservice.api.service.calcom.team.CalComTeamService;
import com.vi.appointmentservice.api.service.onlineberatung.AdminUserService;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.useradminservice.generated.web.model.AskerResponseDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RescheduleHelper {

  private final @NonNull CalComEventTypeService eventTypeService;
  private final @NonNull CalComUserService calComUserService;

  private final @NonNull AdminUserService adminUserService;
  private final @NonNull CalcomUserToConsultantRepository calcomUserToConsultantRepository;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull CalComEventTypeService calComEventTypeService;
  private final @NonNull CalComTeamService calComTeamService;

  public CalcomBooking attachRescheduleLink(CalcomBooking calcomBooking) {
    CalcomUser registeredCalcomUser = this.calComUserService
        .getUserById(Long.valueOf(calcomBooking.getUserId()));
    var teamId = getTeamIdForBooking(calcomBooking);
    String slug = null;
    if (teamId != null) {
      CalcomTeam team = calComTeamService.getTeamById(Long.valueOf(teamId));
      slug = "team/" + team.getSlug();
    } else {
      slug = registeredCalcomUser.getUsername();
    }

    String eventTypeSlug = this.eventTypeService.getEventTypeById(
        Long.valueOf(calcomBooking.getEventTypeId())).getSlug();
    calcomBooking.setRescheduleLink(
        "/" + slug + "/" + eventTypeSlug + "?rescheduleUid=" + calcomBooking.getUid());

    return calcomBooking;
  }

  private Integer getTeamIdForBooking(CalcomBooking calcomBooking) {
    CalcomEventTypeDTO eventType = calComEventTypeService
        .getEventTypeById(Long.valueOf(calcomBooking.getEventTypeId()));
    return eventType.getTeamId();
  }

  public void attachConsultantName(List<CalcomBooking> bookings) {
    Map<Integer, String> bookingUserIdConsultantId = new HashMap<>();
    bookings.stream().forEach(booking -> {
      Optional<CalcomUserToConsultant> calcomUserToConsultant = this.calcomUserToConsultantRepository
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
      }
    });

  }

  public void attachAskerNames(List<CalcomBooking> calcomBookings) {
    Map<Long, String> bookingIdAskerId = new HashMap<Long, String>();
    calcomBookings.stream().forEach(booking -> {
      Optional<CalcomBookingToAsker> byCalcomBookingId = this.calcomBookingToAskerRepository
          .findByCalcomBookingId(
              booking.getId());
      if (byCalcomBookingId.isPresent()) {
          bookingIdAskerId.put(booking.getId(), byCalcomBookingId.get().getAskerId());
      } else {
        bookingIdAskerId.put(booking.getId(), null);
      }
    });

    Map<String, String> askerUserNamesForIds = this.adminUserService
        .getAskerUserNamesForIds(bookingIdAskerId.values());
    calcomBookings.stream().forEach(booking ->{
      if (askerUserNamesForIds.get(booking.getAskerId()) != null) {
        booking.setAskerName(
            askerUserNamesForIds.get(askerUserNamesForIds.get(booking.getAskerId())));
      } else {
        booking.setAskerName("Unknown name");
      }
    });

  }

}
