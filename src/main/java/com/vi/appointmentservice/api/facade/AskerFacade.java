package com.vi.appointmentservice.api.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vi.appointmentservice.api.model.AskerDTO;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.service.calcom.CalComBookingService;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.api.calcom.repository.BookingRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@RequiredArgsConstructor
@Slf4j
public class AskerFacade {

  @Value("${calcom.email.trash}")
  private String trashEmail;
  @Value("${identity.email-dummy-suffix}")
  private String dummyEmailSuffix;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull CalComBookingService calComBookingService;
  private final @NonNull BookingRepository bookingRepository;

  public List<CalcomBooking> getAskerActiveBookings(String askerId) {
    if (calcomBookingToAskerRepository.existsByAskerId(askerId)) {
      List<CalcomBookingToAsker> bookingIds = calcomBookingToAskerRepository
          .findByAskerId(askerId);
      List<CalcomBooking> bookings = calComBookingService.getAskerActiveBookings(
          bookingIds.stream().map(CalcomBookingToAsker::getCalcomBookingId).collect(
              Collectors.toList()));
      return bookings;
    } else {
      return new ArrayList<>();
    }
  }

  @Transactional
  public void deleteAskerData(String askerId) {
    List<CalcomBookingToAsker> bookings = calcomBookingToAskerRepository.findByAskerId(askerId);
    bookings.forEach(booking -> {
      CalcomBooking calcomBooking = calComBookingService
          .getBookingById(booking.getCalcomBookingId());
      if (calcomBooking != null) {
        calComBookingService.cancelBooking(calcomBooking.getUid());
        bookingRepository.deleteBooking(booking.getCalcomBookingId());
        bookingRepository.deleteAttendeeWithoutBooking();
        calcomBookingToAskerRepository.deleteByCalcomBookingId(booking.getCalcomBookingId());
      }
    });
  }

  public void updateAskerEmail(final AskerDTO askerDTO) {
    final String newEmail = resolveEmail(askerDTO);
    if (calcomBookingToAskerRepository.existsByAskerId(askerDTO.getId())) {
      List<Long> bookingIds = calcomBookingToAskerRepository.findByAskerId(askerDTO.getId()).stream()
              .map(CalcomBookingToAsker::getCalcomBookingId)
              .collect(Collectors.toList());
      bookingRepository.updateAttendeeEmail(bookingIds, newEmail);
    } else {
      log.error("Asker with id: {} not existing in calcom repo", askerDTO.getId());
    }
  }

  private String resolveEmail(final AskerDTO askerDTO) {
    if (isBlank(askerDTO.getEmail()) || askerDTO.getEmail().endsWith(dummyEmailSuffix)) {
      return trashEmail;
    }
    return askerDTO.getEmail();
  }
}
