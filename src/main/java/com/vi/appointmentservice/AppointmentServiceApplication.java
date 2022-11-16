package com.vi.appointmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Starter class for the application.
 */
@EnableAsync
@SpringBootApplication
public class AppointmentServiceApplication {

  /**
   * Global application entry point.
   *
   * @param args possible provided args
   */
  public static void main(String[] args) {
    SpringApplication.run(AppointmentServiceApplication.class, args);
  }
}
