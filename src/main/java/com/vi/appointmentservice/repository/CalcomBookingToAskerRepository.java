package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.model.CalcomBookingToAsker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalcomBookingToAskerRepository extends JpaRepository<CalcomBookingToAsker, String> {

    List<CalcomBookingToAsker> findByAskerId(String askerId);
    CalcomBookingToAsker findByCalcomBookingId(Long bookingId);
    void deleteByCalcomBookingId(Long calcomBookingId);
}