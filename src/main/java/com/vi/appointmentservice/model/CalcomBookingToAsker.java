package com.vi.appointmentservice.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "CalcomBookingToUser")
@Table(name = "calcom_booking_to_asker")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalcomBookingToAsker implements Serializable {

    @Id
    @Column(name = "calcombookingid", unique = true, nullable = false)
    private Long calcomBookingId;

    @Column(name = "askerId", unique = true, nullable = false)
    private String askerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CalcomBookingToAsker that = (CalcomBookingToAsker) o;
        return calcomBookingId != null && Objects.equals(calcomBookingId, that.calcomBookingId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
