package com.devtrack.backend;

import com.devtrack.backend.exception.InvalidCredentialsException;
import com.devtrack.backend.model.AppUser;
import com.devtrack.backend.security.DevTrackUserDetails;
import com.devtrack.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AccountPersistenceIntegrationTest extends PostgresIntegrationSupport {
    @Autowired AuthService authService;

    @Test
    void postgresRejectsDuplicateEmail() {
        AppUser first = users.saveAndFlush(new AppUser("unique@devtrack.com", "hash"));

        Throwable failure = catchThrowable(() ->
                users.saveAndFlush(new AppUser(first.getEmail(), "another-hash")));

        assertThat(failure).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(rootCause(failure)).isInstanceOfSatisfying(SQLException.class,
                sql -> assertThat(sql.getSQLState()).isEqualTo("23505"));
        assertThat(users.findAll()).extracting(AppUser::getId).containsExactly(first.getId());
    }

    @Test
    void deletesEntireAccountAndPreservesOtherUser() {
        Account survivor = createAccount("survivor@devtrack.com");
        DatabaseSnapshot survivorData = snapshot();
        Account deleted = createAccount("deleted@devtrack.com");
        authenticate(deleted);

        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
        authService.deleteAccount(PASSWORD);

        assertThat(users.existsById(deleted.id())).isFalse();
        assertThat(blocks.findAllById(deleted.blockIds())).isEmpty();
        assertThat(problems.findAllById(deleted.problemIds())).isEmpty();
        assertThat(users.existsById(survivor.id())).isTrue();
        assertThat(snapshot()).isEqualTo(survivorData);
    }

    @Test
    void incorrectPasswordPreservesBothAccountsAndAllResources() {
        Account owner = createAccount("owner@devtrack.com");
        createAccount("other@devtrack.com");
        DatabaseSnapshot before = snapshot();
        authenticate(owner);

        assertThatThrownBy(() -> authService.deleteAccount("wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    void databaseFailureAfterChildDeletesRollsBackEntireServiceTransaction() {
        Account owner = createAccount("rollback@devtrack.com");
        createAccount("other@devtrack.com");
        DatabaseSnapshot before = snapshot();
        authenticate(owner);

        // A real PostgreSQL BEFORE DELETE trigger fails at the last step (including a deferred JPA flush).
        // Its message proves the child DELETE statements ran in the SAME transaction before the failure.
        // No repository or service is mocked, and the test itself has no surrounding transaction.
        jdbc.execute("""
                CREATE FUNCTION fail_account_delete() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN
                    IF EXISTS (SELECT 1 FROM study_blocks WHERE user_id = OLD.id) THEN
                        RAISE EXCEPTION 'children still exist';
                    END IF;
                    RAISE EXCEPTION 'controlled failure after child deletes' USING ERRCODE = 'P0001';
                END;
                $$
                """);
        try {
            jdbc.execute("""
                    CREATE TRIGGER fail_account_delete BEFORE DELETE ON users
                    FOR EACH ROW EXECUTE FUNCTION fail_account_delete()
                    """);
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();

            Throwable failure = catchThrowable(() -> authService.deleteAccount(PASSWORD));

            assertThat(failure).isNotNull();
            assertThat(rootCause(failure)).isInstanceOfSatisfying(SQLException.class, sql -> {
                assertThat(sql.getSQLState()).isEqualTo("P0001");
                assertThat(sql.getMessage()).contains("controlled failure after child deletes");
            });
            // Independent committed reads, after the service proxy has completed its rollback.
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            assertThat(snapshot()).isEqualTo(before);
        } finally {
            jdbc.execute("DROP TRIGGER IF EXISTS fail_account_delete ON users");
            jdbc.execute("DROP FUNCTION fail_account_delete()");
        }
    }

    private void authenticate(Account account) {
        var principal = new DevTrackUserDetails(account.id(), account.email(), "unused");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
    }

    private Throwable rootCause(Throwable failure) {
        assertThat(failure).isNotNull();
        while (failure.getCause() != null) {
            failure = failure.getCause();
        }
        return failure;
    }
}
