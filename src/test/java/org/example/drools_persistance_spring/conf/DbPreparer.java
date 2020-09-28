package org.example.drools_persistance_spring.conf;

import com.opentable.db.postgres.embedded.DatabasePreparer;
import com.opentable.db.postgres.embedded.FlywayPreparer;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author maksim.drobyshev
 * Created on 12.10.2018
 */
@Component
public class DbPreparer implements DatabasePreparer {
    private static final String CREATE_FORMAT = "CREATE SCHEMA %s;";
    private static final String SELECT_FORMAT = "ALTER USER %s SET search_path to %s;";

    @Value("${spring.datasource.schema}")
    private String schema;

    @Override
    public void prepare(DataSource ds) throws SQLException {
        PGSimpleDataSource simpleDataSource = (PGSimpleDataSource) ds;
        try(Connection connection = ds.getConnection()) {
            connection.prepareStatement(String.format(CREATE_FORMAT, schema)).execute();
            connection.prepareStatement(String.format(SELECT_FORMAT, simpleDataSource.getUser(), schema)).execute();
        }
        FlywayPreparer.forClasspathLocation("db/migration").prepare(ds);
    }
}
