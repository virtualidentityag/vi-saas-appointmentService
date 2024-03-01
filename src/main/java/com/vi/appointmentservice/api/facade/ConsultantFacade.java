package com.vi.appointmentservice.api.facade;

import com.vi.appointmentservice.api.calcom.model.CalcomUser;
import com.vi.appointmentservice.api.calcom.repository.AvailabilityRepository;
import com.vi.appointmentservice.api.calcom.repository.BookingRepository;
import com.vi.appointmentservice.api.calcom.repository.MembershipsRepository;
import com.vi.appointmentservice.api.calcom.repository.ScheduleRepository;
import com.vi.appointmentservice.api.calcom.service.CalComBookingService;
import com.vi.appointmentservice.api.calcom.service.CalComUserService;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.NotFoundException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomToken;
import com.vi.appointmentservice.api.model.ConsultantDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.model.PatchConsultantDTO;
import com.vi.appointmentservice.api.service.AppointmentService;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ConsultantFacade {

  private final @NonNull CalComUserService calComUserService;
  private final @NonNull CalComBookingService calComBookingService;
  private final @NonNull CalcomEventTypeService calComEventTypeService;
  private final @NonNull AppointmentService appointmentService;
  private final @NonNull UserToConsultantRepository userToConsultantRepository;

  private final @NonNull MembershipsRepository calMembershipsRepository;
  private final @NonNull ScheduleRepository scheduleRepository;
  private final @NonNull BookingRepository bookingRepository;
  private final @NonNull AvailabilityRepository availabilityRepository;

  public void createUser(ConsultantDTO consultant) {
    CalcomUser calcomUser = calComUserService
        .createUser(getDisplayNameOrFallbackToFirstname(consultant),
            consultant.getEmail());
    linkConsultantToAppointmentUser(calcomUser, consultant);
    setupDefaultScheduleAndEventType(calcomUser);
  }

  private String getDisplayNameOrFallbackToFirstname(ConsultantDTO consultant) {
    return StringUtils.isBlank(consultant.getDisplayName()) ? consultant.getFirstname() : consultant
        .getDisplayName();
  }

  private String getDisplayNameOrFallbackToFirstname(PatchConsultantDTO consultant) {
    return StringUtils.isBlank(consultant.getDisplayName()) ? consultant.getFirstname() : consultant
        .getDisplayName();
  }

  public void updateAppointmentUser(ConsultantDTO consultant) {
    var name = getDisplayNameOrFallbackToFirstname(consultant);
    Optional<CalcomUserToConsultant> userConsultant = userToConsultantRepository
        .findByConsultantId(consultant.getId());
    calComUserService
        .updateUser(userConsultant.orElseThrow().getCalComUserId(), name, consultant.getEmail());
  }

  public void patchAppointmentUser(String consultantId, PatchConsultantDTO consultant) {
    var name = getDisplayNameOrFallbackToFirstname(consultant);
    Optional<CalcomUserToConsultant> userConsultant = userToConsultantRepository
        .findByConsultantId(consultantId);
    calComUserService
        .updateUsername(userConsultant.orElseThrow().getCalComUserId(), name);
  }

  private void linkConsultantToAppointmentUser(
      CalcomUser calcomUser, ConsultantDTO consultant) {
    userToConsultantRepository.save(
        new CalcomUserToConsultant(consultant.getId(), calcomUser.getId(),
            calcomUser.getPlainPassword()));
  }

  void setupDefaultScheduleAndEventType(CalcomUser calcomUser) {
    Long defaultScheduleId = scheduleRepository.createDefaultSchedule(calcomUser.getId());
    AppointmentType defaultAppointmentType = appointmentService.createDefaultAppointmentType();
    defaultAppointmentType.setTitle("Beratung mit dem / der Berater:in");
    calComEventTypeService
        .createEventType(calcomUser, defaultAppointmentType,
            defaultScheduleId);
  }

  public void deleteConsultantHandler(String consultantId) {
    try {
      // Find associated user
      Long calcomUserId = this.getCalcomUserToConsultantIfExists(consultantId).getCalComUserId();
      // Delete team memberships
      calMembershipsRepository.deleteAllMembershipsOfUser(calcomUserId);
      // Delete personal event-types
      calComEventTypeService.deleteAllEventTypesOfUser(calcomUserId);
      // Delete schedules
      Set<Integer> deletedSchedules = scheduleRepository.deleteUserSchedules(calcomUserId);
      // Delete availabilities for schedules
      for (Integer scheduleId : deletedSchedules) {
        availabilityRepository.deleteAvailabilityByScheduleId(Long.valueOf(scheduleId));
      }

      List<CalcomBooking> bookings = new ArrayList<>();
      bookings.addAll(bookingRepository.getConsultantActiveBookings(calcomUserId));
      bookings.addAll(bookingRepository.getConsultantCancelledBookings(calcomUserId));
      bookings.addAll(bookingRepository.getConsultantExpiredBookings(calcomUserId));

      bookings.forEach(booking -> {
        calComBookingService.cancelBooking(booking.getUid());
        bookingRepository.deleteBooking(booking.getId());
        bookingRepository.deleteAttendeeWithoutBooking();
      });

      // Delete user
      calComUserService.deleteUser(calcomUserId);
      // Remove association
      userToConsultantRepository.deleteByConsultantId(consultantId);
    } catch (Exception e) {
      log.error("Error while deleting consultant", e);
      throw new BadRequestException("Error while deleting consultant with id " + consultantId);
    }
  }

  public List<CalcomBooking> getConsultantActiveBookings(String consultantId) {
    Long calcomUserId = getCalcomUserToConsultantIfExists(consultantId).getCalComUserId();
    return calComBookingService.getConsultantActiveBookings(calcomUserId);
  }

  public List<CalcomBooking> getConsultantCancelledBookings(String consultantId) {
    Long calcomUserId = getCalcomUserToConsultantIfExists(consultantId).getCalComUserId();
    return calComBookingService.getConsultantCancelledBookings(calcomUserId);
  }

  public List<CalcomBooking> getConsultantExpiredBookings(String consultantId) {
    Long calcomUserId = getCalcomUserToConsultantIfExists(consultantId).getCalComUserId();
    return calComBookingService.getConsultantExpiredBookings(calcomUserId);
  }

  public MeetingSlug getConsultantMeetingSlugHandler(String consultantId) {
    getCalcomUserToConsultantIfExists(consultantId);
    MeetingSlug meetingSlug = new MeetingSlug();
    var calcomUserId = getCalcomUserToConsultantIfExists(consultantId).getCalComUserId();
    var username = calComUserService
        .getUserById(calcomUserId)
        .getUsername();
    var eventSlug = calComEventTypeService.getEventTypeByUserId(calcomUserId).getSlug();
    meetingSlug.setSlug(username + "/" + eventSlug);
    return meetingSlug;
  }

  private CalcomUserToConsultant getCalcomUserToConsultantIfExists(String consultantId) {
    Optional<CalcomUserToConsultant> calcomUserToConsultant = userToConsultantRepository
        .findByConsultantId(consultantId);
    if (calcomUserToConsultant.isPresent()) {
      return calcomUserToConsultant.get();
    } else {
      throw new NotFoundException(
          String.format("No calcom user associated to consultant id '%s'", consultantId));
    }
  }

  public CalcomToken getToken(String consultantId) {
    Optional<CalcomUserToConsultant> calcomUserToConsultant = userToConsultantRepository
        .findByConsultantId(consultantId);
    if (calcomUserToConsultant.isEmpty()) {
      throw new BadRequestException("Calcom user doesn't exist for given ID");
    }
    CalcomToken token = new CalcomToken();
    token.setToken(calcomUserToConsultant.get().getToken());
    return token;
  }
}
