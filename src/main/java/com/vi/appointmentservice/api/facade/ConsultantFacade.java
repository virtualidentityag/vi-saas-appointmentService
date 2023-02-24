package com.vi.appointmentservice.api.facade;

import com.vi.appointmentservice.api.calcom.CalComVIAdapter;
import com.vi.appointmentservice.api.calcom.service.CalComUserService;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.NotFoundException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.CalcomToken;
import com.vi.appointmentservice.api.model.ConsultantDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.service.calcom.CalComBookingService;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.api.calcom.repository.AvailabilityRepository;
import com.vi.appointmentservice.api.calcom.repository.BookingRepository;
import com.vi.appointmentservice.api.calcom.repository.MembershipsRepository;
import com.vi.appointmentservice.api.calcom.repository.ScheduleRepository;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import com.vi.appointmentservice.api.calcom.repository.WebhookRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ConsultantFacade {

  private final @NonNull CalComUserService calComUserService;
  private final @NonNull MembershipsRepository calMembershipsRepository;
  private final @NonNull CalComBookingService calComBookingService;
  private final @NonNull UserToConsultantRepository userToConsultantRepository;
  private final @NonNull ScheduleRepository scheduleRepository;
  private final @NonNull WebhookRepository webhookRepository;
  private final @NonNull BookingRepository bookingRepository;
  private final @NonNull CalComVIAdapter calComVIAdapter;
  private final @NonNull CalcomEventTypeService calComEventTypeService;


  private static final String DEFAULT_EVENT_DESCRIPTION =
      "Bitte wählen Sie Ihre gewünschte Terminart. Wir bemühen uns, Ihren Wunsch zu erfüllen. "
          + "Die Berater:innen werden Sie ggf per Chat auf unserer Plattform informieren. "
          + "Loggen Sie sich also vor einem Termin auf jeden Fall ein!";
  private final @NonNull AvailabilityRepository availabilityRepository;
  @Value("${app.base.url}")
  private String appBaseUrl;


  public void createUser(ConsultantDTO consultant) {
    com.vi.appointmentservice.api.calcom.model.CalcomUser calcomUser = calComUserService
        .createUser(consultant.getFirstname() + " " + consultant.getLastname(),
            consultant.getEmail());
    linkConsultantToAppointmentUser(calcomUser, consultant);
    setupDefaultScheduleAndEventType(calcomUser);
  }

  public void updateAppointmentUser(ConsultantDTO consultant) {
    var name = consultant.getFirstname() + " " + consultant.getLastname();
    Optional<CalcomUserToConsultant> userConsultant = userToConsultantRepository
        .findByConsultantId(consultant.getId());
    calComUserService.updateUser(userConsultant.get().getCalComUserId(), name);
  }

  private void linkConsultantToAppointmentUser(
      com.vi.appointmentservice.api.calcom.model.CalcomUser calcomUser, ConsultantDTO consultant) {
    userToConsultantRepository.save(
        new CalcomUserToConsultant(consultant.getId(), calcomUser.getId(),
            calcomUser.getPlainPassword()));
  }

  void setupDefaultScheduleAndEventType(
      com.vi.appointmentservice.api.calcom.model.CalcomUser calcomUser) {
    webhookRepository.updateUserWebhook(calcomUser.getId());
    Long defaultScheduleId = scheduleRepository.createDefaultSchedule(calcomUser.getId());
    calComEventTypeService
        .createEventType(calcomUser, createDefaultAppointmentType(), defaultScheduleId);
  }

  private AppointmentType createDefaultAppointmentType() {
    AppointmentType appointmentType = new AppointmentType();
    appointmentType.setAfterEventBuffer(10);
    appointmentType.setBeforeEventBuffer(0);
    appointmentType.setDescription(DEFAULT_EVENT_DESCRIPTION);
    appointmentType.setLength(50);
    appointmentType.setMinimumBookingNotice(240);
    appointmentType.setSlotInterval(15);
    appointmentType.setTitle("Beratung mit");
    return appointmentType;
  }

  public HttpStatus deleteConsultantHandler(String consultantId) {
    // Find associated user
    Long calcomUserId = this.getCalcomUserToConsultantIfExists(consultantId).getCalComUserId();
    // Delete team memberships
    calMembershipsRepository.deleteAllMembershipsOfUser(calcomUserId);
    // Delete personal event-types
    calComVIAdapter.deleteAllEventTypesOfUser(calcomUserId);
    // Delete schedules
    List<Integer> deletedSchedules = scheduleRepository.deleteUserSchedules(calcomUserId);
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
    HttpStatus deleteResponseCode = calComUserService.deleteUser(calcomUserId);
    // Remove association
    if (deleteResponseCode == HttpStatus.OK) {
      userToConsultantRepository.deleteByConsultantId(consultantId);
    }
    return deleteResponseCode;

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

  public List<CalcomEventTypeDTO> getAllEventTypesOfConsultantHandler(String consultantId) {
    getCalcomUserToConsultantIfExists(consultantId);
    return calComVIAdapter
        .getAllEventTypesOfUser(getCalcomUserToConsultantIfExists(consultantId).getCalComUserId());
  }

  public MeetingSlug getConsultantMeetingSlugHandler(String consultantId) {
    getCalcomUserToConsultantIfExists(consultantId);
    MeetingSlug meetingSlug = new MeetingSlug();
    meetingSlug.setSlug(calComUserService
        .getUserById(getCalcomUserToConsultantIfExists(consultantId).getCalComUserId())
        .getUsername());
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
