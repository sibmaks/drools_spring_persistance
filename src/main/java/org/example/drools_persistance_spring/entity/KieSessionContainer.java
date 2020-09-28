package org.example.drools_persistance_spring.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.kie.api.runtime.KieSession;

import java.util.concurrent.locks.Lock;

/**
 * Created by maksim.drobyshev on 28-Sep-20.
 */
@Data
@AllArgsConstructor
public class KieSessionContainer {
    private final KieSession kieSession;
    private final Lock lock;
}
