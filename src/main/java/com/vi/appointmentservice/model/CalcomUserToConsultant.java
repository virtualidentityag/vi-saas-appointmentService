package com.vi.appointmentservice.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "CalcomUserToConsultant")
@Table(name = "calcom_user_to_consultant")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalcomUserToConsultant implements Serializable {
    @Id
    @Column(name = "consultantid", unique = true, nullable = false)
    private String consultantId;

    @Column(name = "calcomuserid", unique = true, nullable = false)
    private Long calComUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CalcomUserToConsultant that = (CalcomUserToConsultant) o;
        return consultantId != null && Objects.equals(consultantId, that.consultantId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
