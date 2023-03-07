package com.vi.appointmentservice.repository;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WebhookRepository {
  private final @NotNull JdbcTemplate jdbcTemplate;

  @Value("${app.base.url}")
  private String appBaseUrl;

  @Value("${calcom.webhook.secret}")
  private String webhookSecret;

  public void updateUserWebhook(Long calcomUserId) {
    String DELETE_QUERY = "delete from \"Webhook\" where \"userId\"=" + calcomUserId;
    jdbcTemplate.update(DELETE_QUERY);

    String INSERT_QUERY = "insert into \"Webhook\" (\"id\", \"userId\", \"subscriberUrl\", \"secret\", \"active\", \"eventTriggers\") values ($idParam, $userIdParam, $urlParam, $secretParam, true, '{BOOKING_CANCELLED,BOOKING_CREATED,BOOKING_RESCHEDULED}')";
    INSERT_QUERY = INSERT_QUERY
        .replace("$idParam", "'"+ UUID.randomUUID() +"'")
        .replace("$userIdParam", calcomUserId.toString())
        .replace("$urlParam" ,"'" + appBaseUrl + "/service/appointservice/processBooking'")
        .replace("$secretParam" ,"'" + webhookSecret + "'");
    jdbcTemplate.update(INSERT_QUERY);
  }


}
