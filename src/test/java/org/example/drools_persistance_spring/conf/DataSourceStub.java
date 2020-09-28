package org.example.drools_persistance_spring.conf;

import com.opentable.db.postgres.embedded.PreparedDbProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author maksim.drobyshev
 * Created on 12.10.2018
 */
@TestConfiguration
public class DataSourceStub {
    private static final ReentrantLock SET_CACHED_DATA_SOURCE_LOCK = new ReentrantLock();
    private static DataSource cachedDataSource;
    private static PreparedDbProvider provider;

    @Autowired
    private DbPreparer dbPreparer;

    @PostConstruct
    public void init() {
        if(provider == null) {
            provider = PreparedDbProvider.forPreparer(dbPreparer);
        }
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        if(cachedDataSource == null) {
            try {
                SET_CACHED_DATA_SOURCE_LOCK.lock();
                if(cachedDataSource == null) {
                    cachedDataSource = provider.createDataSource();
                }
            } finally {
                SET_CACHED_DATA_SOURCE_LOCK.unlock();
            }
        }
        return cachedDataSource;
    }
}
