package com.vi.appointmentservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "NotificationSettings")
@Table(name = "notification_settings")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettings implements Serializable {

  @Id
  @Column(name = "asker_id", unique = true, nullable = false)
  private Long askerId;

  @Column(name = "should_receive_calcom_email", nullable = false)
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private boolean shouldReceiveCalcomEmail;
}
