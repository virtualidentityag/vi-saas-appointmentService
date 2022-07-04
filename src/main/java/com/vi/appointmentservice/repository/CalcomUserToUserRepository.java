package com.vi.appointmentservice.repository;

import com.vi.appointmentservice.model.CalcomUserToUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalcomUserToUserRepository extends JpaRepository<CalcomUserToUser, String> {
    CalcomUserToUser findByUserId(String userId);
}