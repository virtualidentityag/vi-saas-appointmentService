package com.vi.appointmentservice.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomUser;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.service.onlineberatung.AdminUserService;
import com.vi.appointmentservice.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.service.calcom.CalComUserService;
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

  public CalcomBooking attachRescheduleLink(CalcomBooking calcomBooking)
      throws JsonProcessingException {
    CalcomUser registeredCalcomUser = this.calComUserService.getUserById(Long.valueOf(calcomBooking.getUserId()));
    if (registeredCalcomUser != null && registeredCalcomUser.getUsername() != null) {
      String userSlug = registeredCalcomUser.getUsername();
      String eventTypeSlug = this.eventTypeService.getEventTypeById(
          Long.valueOf(calcomBooking.getEventTypeId())).getSlug();
      calcomBooking.setRescheduleLink(
          "/" + userSlug + "/" + eventTypeSlug + "?rescheduleUid=" + calcomBooking.getUid());
    }
    return calcomBooking;
  }

  public CalcomBooking attachConsultantName(CalcomBooking calcomBooking)
      throws JsonProcessingException {
    if (this.calcomUserToConsultantRepository.existsByCalComUserId(
        Long.valueOf(calcomBooking.getUserId()))) {
      String consultantId = this.calcomUserToConsultantRepository.findByCalComUserId(
          Long.valueOf(calcomBooking.getUserId())).getConsultantId();
      ConsultantDTO consultant = this.adminUserService.getConsultantById(consultantId);
      calcomBooking.setConsultantName(consultant.getFirstname() + " " + consultant.getLastname());
    } else {
      calcomBooking.setConsultantName("Unknown Consultant");
    }
    return calcomBooking;
  }

  public CalcomBooking attachAskerName(CalcomBooking calcomBooking)
      throws JsonProcessingException {
    if (this.calcomBookingToAskerRepository.existsByCalcomBookingId(
        Long.valueOf(calcomBooking.getId()))) {
      String askerId = this.calcomBookingToAskerRepository.findByCalcomBookingId(
          Long.valueOf(calcomBooking.getId())).getAskerId();
      AskerResponseDTO asker = this.adminUserService.getAskerById(askerId);
      calcomBooking.setAskerName(asker.getUsername());
    }
    return calcomBooking;
  }

}
