package com.vi.appointmentservice.helper;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CalcomTeam;
import com.vi.appointmentservice.api.model.CalcomUser;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.calcom.team.CalComTeamService;
import com.vi.appointmentservice.api.service.calcom.CalComUserService;
import com.vi.appointmentservice.api.service.onlineberatung.AdminUserService;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.useradminservice.generated.web.model.AskerResponseDTO;
import com.vi.appointmentservice.useradminservice.generated.web.model.ConsultantDTO;
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
    CalcomEventType eventType = calComEventTypeService
        .getEventTypeById(Long.valueOf(calcomBooking.getEventTypeId()));
    return eventType.getTeamId();
  }

  public CalcomBooking attachConsultantName(CalcomBooking calcomBooking) {
    if (this.calcomUserToConsultantRepository.existsByCalComUserId(
        Long.valueOf(calcomBooking.getUserId()))) {
      String consultantId = this.calcomUserToConsultantRepository.findByCalComUserId(
          Long.valueOf(calcomBooking.getUserId())).getConsultantId();
      ConsultantDTO consultant = null;
      consultant = this.adminUserService.getConsultantById(consultantId);
      calcomBooking.setConsultantName(consultant.getFirstname() + " " + consultant.getLastname());
    } else {
      calcomBooking.setConsultantName("Unknown Consultant");
    }
    return calcomBooking;
  }

  public CalcomBooking attachAskerName(CalcomBooking calcomBooking) {
    if (this.calcomBookingToAskerRepository.existsByCalcomBookingId(
        calcomBooking.getId())) {
      String askerId = this.calcomBookingToAskerRepository.findByCalcomBookingId(
          calcomBooking.getId()).getAskerId();
      AskerResponseDTO asker = this.adminUserService.getAskerById(askerId);
      calcomBooking.setAskerName(asker.getUsername());
    }
    return calcomBooking;
  }

}
