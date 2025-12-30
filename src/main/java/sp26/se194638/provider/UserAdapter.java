package sp26.se194638.provider;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import sp26.se194638.model.User;

import java.util.stream.Stream;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    private final User user;
    private final String id;

    public UserAdapter(KeycloakSession session,
                       RealmModel realm,
                       ComponentModel model,
                       User user) {
        super(session, realm, model);
        this.user = user;
        this.id = StorageId.keycloakId(model, user.getId().toString());

        setUsername(user.getUserName());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setEmail(user.getEmail());
        setEnabled(user.getActive() == 1);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public void setUsername(String s) {

    }

    @Override
    public boolean isEnabled() {
        return user.getActive() == 1;
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
