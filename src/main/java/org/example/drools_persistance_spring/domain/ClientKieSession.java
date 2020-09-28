package org.example.drools_persistance_spring.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created by maksim.drobyshev on 23-Sep-20.
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "client_kie_session")
public class ClientKieSession {
    @Id
    @Column(name = "session_identifier")
    private long sessionIdentifier;
    @Column(name = "client_id", unique = true)
    private String clientId;
    @Column(name = "updated", columnDefinition = "TIMESTAMP")
    private Timestamp updated;
    @Column(name = "created", nullable = false, columnDefinition = "TIMESTAMP")
    private Timestamp created;
}
