package vn.login.loginpage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.DatabasePopulator;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import reactor.core.publisher.Mono;

@Configuration
public class DBConfig extends AbstractR2dbcConfiguration {

    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:mysql://localhost:3306/testwebflux?sslMode=DISABLED");
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        // 1. Set session timezone (fixes the Romance Standard Time error)
        DatabasePopulator setTimeZonePopulator = connection -> Mono
                .from(connection.createStatement("SET time_zone = 'Europe/Paris'").execute()).then();

        // 2. Load schema
        ResourceDatabasePopulator schemaPopulator = new ResourceDatabasePopulator(new ClassPathResource("schemas.sql"));

        // 3. Combine both into one initializer
        CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
        populator.addPopulators(setTimeZonePopulator, schemaPopulator);

        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
