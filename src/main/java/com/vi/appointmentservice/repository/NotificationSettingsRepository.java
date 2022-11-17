package com.vi.appointmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vi.appointmentservice.model.NotificationSettings;

public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, String> {


}
