package sp26.se194638.provider;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import sp26.se194638.model.Account;

import java.util.stream.Stream;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    private final Account account;
    private final String id;

    public UserAdapter(KeycloakSession session,
                       RealmModel realm,
                       ComponentModel model,
                       Account account) {
        super(session, realm, model);
        this.account = account;
        this.id = StorageId.keycloakId(model, account.getId().toString());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public void setUsername(String username) {

    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equalsIgnoreCase(account.getStatus());
    }

    @Override
    public String getFirstName() {
        return null;
    }

    @Override
    public String getLastName() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public boolean isEmailVerified() {
        return true;
    }

    @Override
    public Stream<GroupModel> getGroupsStream(String search, Integer first, Integer max) {
        return super.getGroupsStream(search, first, max);
    }

    @Override
    public long getGroupsCount() {
        return super.getGroupsCount();
    }

    @Override
    public long getGroupsCountByNameContaining(String search) {
        return super.getGroupsCountByNameContaining(search);
    }

    @Override
    public void joinGroup(GroupModel group, MembershipMetadata metadata) {
        super.joinGroup(group, metadata);
    }

    @Override
    public boolean isFederated() {
        return super.isFederated();
    }

    @Override
    public boolean hasDirectRole(RoleModel role) {
        return super.hasDirectRole(role);
    }
}
