package org.example.drools_persistance_spring.conf;

import org.drools.core.base.MapGlobalResolver;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.persistence.api.TransactionManager;
import org.example.drools_persistance_spring.service.CustomTimerService;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.spring.persistence.KieSpringJpaManager;
import org.kie.spring.persistence.KieSpringTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by maksim.drobyshev on 01-Sep-20.
 */
@Configuration
public class DroolsConfig {


    @Bean
    @Qualifier("droolsDataSource")
    @ConfigurationProperties(prefix="spring.drools-datasource")
    public DataSource droolsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    @Qualifier("dataSource")
    @ConditionalOnMissingBean(DataSource.class)
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier("droolsEnvironment")
    public Environment droolsEnvironment(
            @Qualifier("droolsDataSource") DataSource dataSource,
            //@Qualifier("drools-entityManagerFactory")
            //        LocalContainerEntityManagerFactoryBean droolsEntityManagerFactory,
            KieServices kieServices) {
        LocalContainerEntityManagerFactoryBean droolsEntityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        droolsEntityManagerFactory.setDataSource(dataSource);
        droolsEntityManagerFactory.setPersistenceUnitName("drools.cookbook.persistence.jpa");
        droolsEntityManagerFactory.afterPropertiesSet();

        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(droolsEntityManagerFactory.getObject());
        jpaTransactionManager.setNestedTransactionAllowed(false);
        jpaTransactionManager.afterPropertiesSet();

        TransactionManager transactionManager = new KieSpringTransactionManager(jpaTransactionManager);

        Environment environment = kieServices.newEnvironment();
        environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, droolsEntityManagerFactory.getObject());
        environment.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new KieSpringJpaManager(environment));
        environment.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
        environment.set(EnvironmentName.GLOBALS, new MapGlobalResolver());
        return environment;
    }

    @Bean
    public KieContainer kieContainer(KieServices kieServices) {
        return kieServices.getKieClasspathContainer();
    }

    @Bean
    public KieBase kieBase(KieContainer kieContainer) {
        return kieContainer.getKieBase("rules");
    }

    @Bean
    public KieSessionConfiguration kieSessionConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("drools.timerService", CustomTimerService.class.getName());
        return KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);
    }

    @Bean
    public KieServices kieServices() {
        return KieServices.get();
    }
}
