package org.example.drools_persistance_spring.service;

import lombok.extern.slf4j.Slf4j;
import org.example.drools_persistance_spring.EventHandler;
import org.example.drools_persistance_spring.domain.ClientKieSession;
import org.example.drools_persistance_spring.entity.KieSessionContainer;
import org.example.drools_persistance_spring.repository.ClientKieSessionRepository;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.internal.command.CommandFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by maksim.drobyshev on 01-Sep-20.
 */
@Slf4j
@Service
public class DroolsService {
    private final KieBase kieBase;
    private final KieSessionConfiguration kieSessionConfiguration;
    private final Environment environment;
    private final ClientKieSessionRepository clientKieSessionRepository;
    private final Lock creationLock;
    private final KieStoreServices kieStoreServices;
    private final Map<String, KieSessionContainer> kieSessionMap;

    @Autowired
    private DroolsService droolsService;

    public DroolsService(KieBase kieBase, KieSessionConfiguration kieSessionConfiguration,
                         @Qualifier("droolsEnvironment") Environment droolsEnvironment,
                         ClientKieSessionRepository clientKieSessionRepository) {
        this.kieBase = kieBase;
        this.kieSessionConfiguration = kieSessionConfiguration;
        this.environment = droolsEnvironment;
        this.clientKieSessionRepository = clientKieSessionRepository;
        this.creationLock = new ReentrantLock();
        this.kieStoreServices = KieServices.get().getStoreServices();
        this.kieSessionMap = new ConcurrentHashMap<>();
    }

    public void processFacts(String externalId, Object ... facts) {
        log.info("**** Access to session for client: {}", externalId);
        KieSessionContainer kieSession = droolsService.getOrCreate(externalId);
        kieSession.getLock().lock();
        try {
            droolsService.processFacts(kieSession.getKieSession(), facts);
        } finally {
            kieSession.getLock().unlock();
        }
        log.info("**** Finished work with session for client: {}", externalId);
    }

    @Transactional
    protected void processFacts(KieSession kieSession, Object ... facts) {
        for(Object fact : facts) {
            kieSession.execute(CommandFactory.newInsert(fact));
        }
        kieSession.fireAllRules();
    }

    public QueryResults processQuery(String externalId, String query, Object ... args) {
        log.info("**** Access to session for client: {}", externalId);
        KieSessionContainer kieSession = droolsService.getOrCreate(externalId);
        log.info("**** Execute query in session for client: {}", externalId);
        kieSession.getLock().lock();
        try {
            return droolsService.processQuery(kieSession.getKieSession(), query, args);
        } finally {
            kieSession.getLock().unlock();
        }
    }

    @Transactional
    protected QueryResults processQuery(KieSession kieSession, String query, Object ... args) {
        return kieSession.getQueryResults(query, args);
    }

    protected KieSessionContainer getOrCreate(String externalId) {
        if (!kieSessionMap.containsKey(externalId)) {
            creationLock.lock();
            try {
                if (!kieSessionMap.containsKey(externalId)) {
                    ClientKieSession clientKieSession = clientKieSessionRepository.findFirstByClientId(externalId);
                    KieSession kieSession;
                    if (clientKieSession == null) {
                        log.info("**** Create session for client: {}", externalId);
                        kieSession = droolsService.createSession(externalId);
                    } else {
                        log.info("**** Restore session for client: {}", externalId);
                        try {
                            kieSession = droolsService.loadSession(clientKieSession.getSessionIdentifier());
                        } catch (Exception e) {
                            log.error("**** Restore session for error, create new session", e);
                            clientKieSessionRepository.delete(clientKieSession);
                            return droolsService.getOrCreate(externalId);
                        }
                    }
                    kieSession.addEventListener(new EventHandler());
                    kieSessionMap.put(externalId, new KieSessionContainer(kieSession, new ReentrantLock()));
                }
            } finally {
                creationLock.unlock();
            }
        }
        return kieSessionMap.get(externalId);
    }

    @Transactional
    protected KieSession createSession(String externalId) {
        KieSession kieSession = kieStoreServices.newKieSession(kieBase, kieSessionConfiguration, environment);
        ClientKieSession clientKieSession = new ClientKieSession();
        clientKieSession.setSessionIdentifier(kieSession.getIdentifier());
        clientKieSession.setClientId(externalId);
        clientKieSession.setCreated(Timestamp.valueOf(LocalDateTime.now()));
        clientKieSessionRepository.save(clientKieSession);
        return kieSession;
    }

    @Transactional
    protected KieSession loadSession(long identifier) {
        return kieStoreServices.loadKieSession(identifier, kieBase, kieSessionConfiguration, environment);
    }

    public List<Object> sessionDump(String externalId) {
        log.info("**** Access to session for client: {}", externalId);
        KieSessionContainer kieSession = droolsService.getOrCreate(externalId);
        return new ArrayList<>(kieSession.getKieSession().getObjects());
    }
}
