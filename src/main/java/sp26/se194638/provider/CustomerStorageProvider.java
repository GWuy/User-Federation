package sp26.se194638.provider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import sp26.se194638.model.Account;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
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

    @Override
    public void close() {
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        Long accountId = Long.valueOf(StorageId.externalId(id));
        Account account = em.find(Account.class, accountId);
        return account == null ? null : new UserAdapter(session, realm, model, account);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        TypedQuery<Account> q = em.createQuery(
                "select a from Account a where a.username = :u", Account.class);
        q.setParameter("u", username);

        return q.getResultStream()
                .findFirst()
                .map(a -> new UserAdapter(session, realm, model, a))
                .orElse(null);
    }

    @Override
    public CredentialValidationOutput getUserByCredential(RealmModel realm, CredentialInput input) {
        return UserLookupProvider.super.getUserByCredential(realm, input);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return null; // Account không có email → OK
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
        return UserQueryProvider.super.searchForUserStream(realm, search);
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        return UserQueryProvider.super.searchForUserStream(realm, search, firstResult, maxResults);
    }

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

        TypedQuery<Account> q = em.createQuery(
                "select a from Account a where a.username like :s",
                Account.class);
        q.setParameter("s", "%" + search + "%");

        if (first != null) q.setFirstResult(first);
        if (max != null) q.setMaxResults(max);

        return q.getResultStream()
                .map(a -> new UserAdapter(session, realm, model, a));
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group) {
        return UserQueryProvider.super.getGroupMembersStream(realm, group);
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, String search, Boolean exact, Integer first, Integer max) {
        return UserQueryProvider.super.getGroupMembersStream(realm, group, search, exact, first, max);
    }

    @Override
    public Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role) {
        return UserQueryProvider.super.getRoleMembersStream(realm, role);
    }

    @Override
    public Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role, Integer firstResult, Integer maxResults) {
        return UserQueryProvider.super.getRoleMembersStream(realm, role, firstResult, maxResults);
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
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
        if (!supportsCredentialType(input.getType())) return false;

        Long accountId = Long.valueOf(StorageId.externalId(user.getId()));
        Account account = em.find(Account.class, accountId);

        if (account == null || account.getPasswordHash() == null) return false;

        PasswordHashProvider php =
                session.getProvider(PasswordHashProvider.class, "bcrypt");

        PasswordCredentialModel pcm =
                PasswordCredentialModel.createFromValues(
                        "bcrypt",
                        null,
                        -1,
                        account.getPasswordHash()
                );

        return php.verify(input.getChallengeResponse(), pcm);
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

    @Override
    public int getUsersCount(RealmModel realm, String search) {
        return UserQueryProvider.super.getUsersCount(realm, search);
    }

    @Override
    public int getUsersCount(RealmModel realm, String search, Set<String> groupIds) {
        return UserQueryProvider.super.getUsersCount(realm, search, groupIds);
    }

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
