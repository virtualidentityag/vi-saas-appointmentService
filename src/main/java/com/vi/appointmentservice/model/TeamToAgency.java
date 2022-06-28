package com.vi.appointmentservice.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "team_to_agency")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamToAgency implements Serializable {
    @Id
    @Column(name = "teamid", unique = true, nullable = false)
    private Long teamid;

    @Column(name = "agencyid", nullable = false)
    private Long agencyId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TeamToAgency that = (TeamToAgency) o;
        return teamid != null && Objects.equals(teamid, that.teamid);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
