package com.vi.appointmentservice.api.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.vi.appointmentservice.api.calcom.repository.BookingRepository;
import com.vi.appointmentservice.api.calcom.service.CalComBookingService;
import com.vi.appointmentservice.api.model.AskerDTO;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class AskerFacadeTest {

  public static final long CALCOM_BOOKING_ID = 1L;
  public static final String ASKER_ID = "askerId";
  @InjectMocks
  AskerFacade askerFacade;
  @Mock
  CalcomBookingToAskerRepository calcomBookingToAskerRepository;

  @Mock
  CalComBookingService calComBookingService;

  @Mock
  BookingRepository bookingRepository;

  @Test
  void getAskerActiveBookings_Should_CallCalcomBookingServiceForActiveBookings() {
    // given
    CalcomBookingToAsker calcomBookingToAsker = new CalcomBookingToAsker();
    calcomBookingToAsker.setAskerId(ASKER_ID);
    calcomBookingToAsker.setCalcomBookingId(CALCOM_BOOKING_ID);
    when(calcomBookingToAskerRepository.existsByAskerId(ASKER_ID)).thenReturn(
        true);
    when(calcomBookingToAskerRepository.findByAskerId(ASKER_ID)).thenReturn(
        Lists.newArrayList(calcomBookingToAsker)
        );

    when(calComBookingService.getAskerActiveBookings(Lists.newArrayList(CALCOM_BOOKING_ID))).thenReturn(
        Lists.newArrayList(new CalcomBooking(), new CalcomBooking())
    );

    // when
    List<CalcomBooking> askerActiveBookings = askerFacade.getAskerActiveBookings(ASKER_ID);
    assertThat(askerActiveBookings).hasSize(2);
  }


  @Test
  void deleteAskerData_Should_DeleteAllAskerBookings() {
    // given
    CalcomBookingToAsker calcomBookingToAsker = new CalcomBookingToAsker();
    calcomBookingToAsker.setCalcomBookingId(CALCOM_BOOKING_ID);
    when(calcomBookingToAskerRepository.findByAskerId(ASKER_ID)).thenReturn(Lists.newArrayList(
        calcomBookingToAsker));
    CalcomBooking calcomBooking = new CalcomBooking();
    calcomBooking.setUid("uid");
    when(calComBookingService.getBookingById(Mockito.anyLong())).thenReturn(calcomBooking);

    // when
    askerFacade.deleteAskerData(ASKER_ID);

    // then
    verify(calComBookingService).cancelBooking("uid");
    verify(bookingRepository).deleteBooking(CALCOM_BOOKING_ID);
    verify(bookingRepository).deleteAttendeeWithoutBooking();
    verify(calcomBookingToAskerRepository).deleteByCalcomBookingId(CALCOM_BOOKING_ID);
  }

  @Test
  void updateAskerEmail_Should_updateAskerEmail() {
    // given
    CalcomBookingToAsker calcomBookingToAsker = new CalcomBookingToAsker();
    calcomBookingToAsker.setCalcomBookingId(CALCOM_BOOKING_ID);
    when(calcomBookingToAskerRepository.existsByAskerId(ASKER_ID)).thenReturn(true);
    when(calcomBookingToAskerRepository.findByAskerId(ASKER_ID)).thenReturn(Lists.newArrayList(calcomBookingToAsker));
    ReflectionTestUtils.setField(askerFacade, "dummyEmailSuffix", ".dummy");

    // when
    askerFacade.updateAskerEmail(new AskerDTO().email("test@test.com").id(ASKER_ID));

    // then
    verify(bookingRepository).updateAttendeeEmail(Lists.newArrayList(CALCOM_BOOKING_ID), "test@test.com");
  }

  @Test
  void updateAskerEmail_Should_NotUpdateAskerEmailIfAskerNotFoundInCalcom() {
    // given
    CalcomBookingToAsker calcomBookingToAsker = new CalcomBookingToAsker();
    calcomBookingToAsker.setCalcomBookingId(CALCOM_BOOKING_ID);
    when(calcomBookingToAskerRepository.existsByAskerId(ASKER_ID)).thenReturn(false);
    ReflectionTestUtils.setField(askerFacade, "dummyEmailSuffix", ".dummy");

    // when
    askerFacade.updateAskerEmail(new AskerDTO().email("test@test.com").id(ASKER_ID));

    // then
    verify(bookingRepository, never()).updateAttendeeEmail(Lists.newArrayList(CALCOM_BOOKING_ID), "test@test.com");
  }

}