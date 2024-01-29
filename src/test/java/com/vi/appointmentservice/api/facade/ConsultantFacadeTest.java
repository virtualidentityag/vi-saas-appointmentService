package com.vi.appointmentservice.api.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.vi.appointmentservice.api.calcom.repository.AvailabilityRepository;
import com.vi.appointmentservice.api.calcom.repository.BookingRepository;
import com.vi.appointmentservice.api.calcom.repository.MembershipsRepository;
import com.vi.appointmentservice.api.calcom.repository.ScheduleRepository;
import com.vi.appointmentservice.api.calcom.service.CalComBookingService;
import com.vi.appointmentservice.api.calcom.service.CalComUserService;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.model.PatchConsultantDTO;
import com.vi.appointmentservice.api.service.AppointmentService;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultantFacadeTest {

  @InjectMocks
  ConsultantFacade consultantFacade;

  @Mock
  CalComUserService calComUserService;

  @Mock
  UserToConsultantRepository userToConsultantRepository;

  @Mock
  CalComBookingService calComBookingService;

  @Mock
  CalcomEventTypeService calcomEventTypeService;

  @Mock
  AppointmentService appointmentService;

  @Mock
  MembershipsRepository calMembershipsRepository;
  @Mock
  ScheduleRepository scheduleRepository;
  @Mock
  BookingRepository bookingRepository;
  @Mock
  AvailabilityRepository availabilityRepository;
  @Test
  void patchAppointmentUser_Should_PatchUserDisplayName() {
    // given
    when(userToConsultantRepository.findByConsultantId("consultantId"))
        .thenReturn(Optional.of(CalcomUserToConsultant.builder().calComUserId(1L).build()));
    // when
    consultantFacade.patchAppointmentUser("consultantId", new PatchConsultantDTO().displayName("displayName"));
    // then
    Mockito.verify(calComUserService).updateUsername(1L, "displayName");
  }

  @Test
  void patchAppointmentUser_Should_ThrowException_When_ConsultantNotFound() {
    // given
    when(userToConsultantRepository.findByConsultantId("consultantId"))
        .thenReturn(Optional.empty());
    // when
    try {
      consultantFacade.patchAppointmentUser("consultantId",
          new PatchConsultantDTO().displayName("displayName"));
    } catch (NoSuchElementException e) {
      // then
      assertThat(e.getMessage()).isEqualTo("No value present");
    }
  }

}