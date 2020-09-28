package org.example.drools_persistance_spring.repository;

import org.example.drools_persistance_spring.domain.ClientKieSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by maksim.drobyshev on 23-Sep-20.
 */
@Repository
public interface ClientKieSessionRepository extends CrudRepository<ClientKieSession, Long> {
    ClientKieSession findFirstByClientId(String clientId);
}
