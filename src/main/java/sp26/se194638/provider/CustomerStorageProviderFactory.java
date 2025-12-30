package sp26.se194638.provider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.HashMap;
import java.util.Map;

public class CustomerStorageProviderFactory implements UserStorageProviderFactory<CustomerStorageProvider> {

    private EntityManagerFactory entityManagerFactory;

    @Override
    public CustomerStorageProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        try {
            CustomerStorageProvider provider = new CustomerStorageProvider();
            provider.setSession(keycloakSession);
            provider.setModel(componentModel);
            provider.setEm(createEntityManager());
            return provider;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create CustomerStorageProvider", e);
        }
    }

    private EntityManager createEntityManager() {
        if (entityManagerFactory == null) {
            createEntityManagerFactory();
        }
        return entityManagerFactory.createEntityManager();
    }

    private synchronized void createEntityManagerFactory() {
        if (entityManagerFactory == null) {
            HibernatePersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
            Map<String, Object> props = getHibernateProperties();
            entityManagerFactory = persistenceProvider.createEntityManagerFactory("user-store", props);
        }
    }

    private Map<String, Object> getHibernateProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        props.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/keycloak");
        props.put("jakarta.persistence.jdbc.user", "postgres");
        props.put("jakarta.persistence.jdbc.password", "12345");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        return props;
    }

    @Override
    public void close() {
        closeEntityManagerFactory();
    }
    private synchronized void closeEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

    @Override
    public String getId() {
        return "custom-user-storage";
    }
}