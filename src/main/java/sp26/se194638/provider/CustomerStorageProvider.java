package sp26.se194638.provider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import lombok.Getter;
import lombok.Setter;
import sp26.se194638.model.User;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@Setter
public class CustomerStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        UserQueryProvider,
        CredentialInputValidator {

    private EntityManager em;
    private ComponentModel model;
    private KeycloakSession session;

    public CustomerStorageProvider() {
    }

    public CustomerStorageProvider(EntityManager em,
            ComponentModel model,
            KeycloakSession session) {
        this.em = em;
        this.model = model;
        this.session = session;
    }

    @Override
    public void close() {
        // không đóng EntityManager ở đây nếu dùng container-managed
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        Long userId = Long.valueOf(StorageId.externalId(id));
        User user = em.find(User.class, userId);
        return user == null ? null : new UserAdapter(session, realm, model, user);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        TypedQuery<User> q = em.createQuery(
                "select u from users u where u.username = :u", User.class);
        q.setParameter("u", username);

        return q.getResultStream()
                .findFirst()
                .map(u -> new UserAdapter(session, realm, model, u))
                .orElse(null);
    }

    @Override
    public CredentialValidationOutput getUserByCredential(RealmModel realm, CredentialInput input) {
        return UserLookupProvider.super.getUserByCredential(realm, input);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        TypedQuery<User> q = em.createQuery(
                "select u from users u where u.email = :e", User.class);
        q.setParameter("e", email);

        return q.getResultStream()
                .findFirst()
                .map(u -> new UserAdapter(session, realm, model, u))
                .orElse(null);
    }

    // ========= SEARCH (bắt buộc) =========

    // @Override
    // public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
    //     return UserQueryProvider.super.searchForUserStream(realm, search);
    // }

    // @Override
    // public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult,
    //         Integer maxResults) {
    //     return UserQueryProvider.super.searchForUserStream(realm, search, firstResult, maxResults);
    // }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params) {
        return UserQueryProvider.super.searchForUserStream(realm, params);
    }

    @Override
    public Stream<UserModel> searchForUserStream(
            RealmModel realm,
            Map<String, String> params,
            Integer first,
            Integer max) {

        String search = params.getOrDefault("search", "");

        TypedQuery<User> q = em.createQuery(
                "select u from users u where u.username like :s or u.email like :s",
                User.class);
        q.setParameter("s", "%" + search + "%");

        if (first != null)
            q.setFirstResult(first);
        if (max != null)
            q.setMaxResults(max);

        return q.getResultStream()
                .map(u -> new UserAdapter(session, realm, model, u));
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group) {
        return UserQueryProvider.super.getGroupMembersStream(realm, group);
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer,
            Integer integer1) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, String search, Boolean exact,
            Integer first, Integer max) {
        return UserQueryProvider.super.getGroupMembersStream(realm, group, search, exact, first, max);
    }

    @Override
    public Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role) {
        return UserQueryProvider.super.getRoleMembersStream(realm, role);
    }

    @Override
    public Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role, Integer firstResult,
            Integer maxResults) {
        return UserQueryProvider.super.getRoleMembersStream(realm, role, firstResult, maxResults);
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        return Stream.empty();
    }

    // ========= PASSWORD =========

    @Override
    public boolean supportsCredentialType(String type) {
        return PasswordCredentialModel.TYPE.equals(type);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String type) {
        return supportsCredentialType(type);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()))
            return false;

        Long userId = Long.valueOf(StorageId.externalId(user.getId()));
        User dbUser = em.find(User.class, userId);

        return dbUser != null
                && dbUser.getPassword() != null
                && dbUser.getPassword().equals(input.getChallengeResponse());
    }

    @Override
    public void preRemove(RealmModel realm) {
        UserStorageProvider.super.preRemove(realm);
    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {
        UserStorageProvider.super.preRemove(realm, group);
    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {
        UserStorageProvider.super.preRemove(realm, role);
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        return UserQueryProvider.super.getUsersCount(realm);
    }

    @Override
    public int getUsersCount(RealmModel realm, Set<String> groupIds) {
        return UserQueryProvider.super.getUsersCount(realm, groupIds);
    }

    // @Override
    // public int getUsersCount(RealmModel realm, String search) {
    //     return UserQueryProvider.super.getUsersCount(realm, search);
    // }

    // @Override
    // public int getUsersCount(RealmModel realm, String search, Set<String> groupIds) {
    //     return UserQueryProvider.super.getUsersCount(realm, search, groupIds);
    // }

    @Override
    public int getUsersCount(RealmModel realm, Map<String, String> params) {
        return UserQueryProvider.super.getUsersCount(realm, params);
    }

    @Override
    public int getUsersCount(RealmModel realm, Map<String, String> params, Set<String> groupIds) {
        return UserQueryProvider.super.getUsersCount(realm, params, groupIds);
    }

    @Override
    public int getUsersCount(RealmModel realm, boolean includeServiceAccount) {
        return UserQueryProvider.super.getUsersCount(realm, includeServiceAccount);
    }
}
