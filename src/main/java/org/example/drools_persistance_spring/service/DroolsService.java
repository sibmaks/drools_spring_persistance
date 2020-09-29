package org.example.drools_persistance_spring.service;

import lombok.extern.slf4j.Slf4j;
import org.drools.persistence.api.TransactionManager;
import org.example.drools_persistance_spring.EventHandler;
import org.example.drools_persistance_spring.domain.ClientKieSession;
import org.example.drools_persistance_spring.entity.KieSessionContainer;
import org.example.drools_persistance_spring.repository.ClientKieSessionRepository;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.internal.command.CommandFactory;
import org.kie.spring.persistence.KieSpringJpaManager;
import org.kie.spring.persistence.KieSpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.kie.api.runtime.EnvironmentName.ENTITY_MANAGER_FACTORY;

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
    private final KieServices kieServices;

    @Autowired
    private DroolsService droolsService;

    public DroolsService(KieBase kieBase, KieSessionConfiguration kieSessionConfiguration,
                         @Qualifier("droolsEnvironment") Environment droolsEnvironment,
                         ClientKieSessionRepository clientKieSessionRepository,
                         KieServices kieServices) {
        this.kieBase = kieBase;
        this.kieSessionConfiguration = kieSessionConfiguration;
        this.environment = droolsEnvironment;
        this.clientKieSessionRepository = clientKieSessionRepository;
        this.creationLock = new ReentrantLock();
        this.kieStoreServices = kieServices.getStoreServices();
        this.kieSessionMap = new ConcurrentHashMap<>();
        this.kieServices = kieServices;
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

    protected void processFacts(KieSession kieSession, Object ... facts) {
        Environment environment = kieSession.getEnvironment();
        TransactionManager transactionManager = (TransactionManager) environment.get(EnvironmentName.TRANSACTION_MANAGER);

        if(!transactionManager.begin()) {
            throw new IllegalStateException("Transaction not started");
        }
        try {
            for (Object fact : facts) {
                kieSession.execute(CommandFactory.newInsert(fact));
            }
            kieSession.fireAllRules();
            transactionManager.commit(true);
        } catch (Throwable t) {
            try {
                int status = transactionManager.getStatus();
                if(status == TransactionManager.STATUS_ACTIVE) {
                    transactionManager.rollback(true);
                } else {
                    log.error("Transaction is not active, status: {}", status);
                }
            } catch (Exception e) {
                log.error("Error rollback transaction", e);
                throw e;
            }
            throw t;
        }
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

    protected KieSession createSession(String externalId) {
        KieSession kieSession = kieStoreServices.newKieSession(kieBase, kieSessionConfiguration, createEnvironment());
        ClientKieSession clientKieSession = new ClientKieSession();
        clientKieSession.setSessionIdentifier(kieSession.getIdentifier());
        clientKieSession.setClientId(externalId);
        clientKieSession.setCreated(Timestamp.valueOf(LocalDateTime.now()));
        clientKieSessionRepository.save(clientKieSession);
        return kieSession;
    }

    protected Environment createEnvironment() {
        EntityManagerFactory entityManagerFactory = (EntityManagerFactory) environment.get(ENTITY_MANAGER_FACTORY);
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(entityManagerFactory);
        jpaTransactionManager.afterPropertiesSet();

        TransactionManager transactionManager = new KieSpringTransactionManager(jpaTransactionManager);

        Environment environment = kieServices.newEnvironment();
        environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory);
        environment.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
        environment.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new KieSpringJpaManager(environment));
        return environment;
    }

    protected KieSession loadSession(long identifier) {
        return kieStoreServices.loadKieSession(identifier, kieBase, kieSessionConfiguration, createEnvironment());
    }

    public List<Object> sessionDump(String externalId) {
        log.info("**** Access to session for client: {}", externalId);
        KieSessionContainer kieSession = droolsService.getOrCreate(externalId);
        return new ArrayList<>(kieSession.getKieSession().getObjects());
    }
}
