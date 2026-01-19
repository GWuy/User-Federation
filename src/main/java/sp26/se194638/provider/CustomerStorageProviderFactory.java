package sp26.se194638.provider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.HashMap;
import java.util.Map;

public class CustomerStorageProviderFactory
        implements UserStorageProviderFactory<CustomerStorageProvider> {

    private static EntityManagerFactory emf;

    @Override
    public CustomerStorageProvider create(KeycloakSession session, ComponentModel model) {
        CustomerStorageProvider provider = new CustomerStorageProvider();
        provider.setSession(session);
        provider.setModel(model);
        provider.setEm(createEntityManager());
        return provider;
    }

    private EntityManager createEntityManager() {
        if (emf == null) {
            synchronized (CustomerStorageProviderFactory.class) {
                if (emf == null) {
                    HibernatePersistenceProvider pp = new HibernatePersistenceProvider();
                    emf = pp.createEntityManagerFactory("user-store", getProps());
                }
            }
        }
        return emf.createEntityManager();
    }

    private Map<String, Object> getProps() {
        Map<String, Object> p = new HashMap<>();
        p.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        p.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/keycloak");
        p.put("jakarta.persistence.jdbc.user", "postgres");
        p.put("jakarta.persistence.jdbc.password", "12345");
        p.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        p.put("hibernate.show_sql", "true");
        return p;
    }

    @Override
    public String getId() {
        return "custom-user-storage";
    }
}