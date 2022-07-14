package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.model.CalcomBookingToAsker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalcomBookingToAskerRepository extends
    JpaRepository<CalcomBookingToAsker, String> {

  List<CalcomBookingToAsker> findByAskerId(String askerId);

  CalcomBookingToAsker findByCalcomBookingId(Long calcomBookingId);

  boolean existsByCalcomBookingId(
      Long calcomBookingId);

  boolean existsByAskerId(String askerId);
}