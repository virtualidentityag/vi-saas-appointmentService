package com.vi.appointmentservice.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "CalcomUserToUser")
@Table(name = "calcom_user_to_user")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalcomUserToUser implements Serializable {
    @Id
    @Column(name = "userid", unique = true, nullable = false)
    private String userId;

    @Column(name = "calcomuserid", unique = true, nullable = false)
    private Long calComUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CalcomUserToUser that = (CalcomUserToUser) o;
        return userId != null && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
