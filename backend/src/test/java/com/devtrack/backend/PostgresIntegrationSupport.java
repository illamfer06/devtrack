package com.devtrack.backend;

import com.devtrack.backend.model.AppUser;
import com.devtrack.backend.model.Problem;
import com.devtrack.backend.model.StudyBlock;
import com.devtrack.backend.repository.AppUserRepository;
import com.devtrack.backend.repository.ProblemRepository;
import com.devtrack.backend.repository.StudyBlockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.devtrack.backend.model.Difficulty.EASY;
import static org.assertj.core.api.Assertions.assertThat;

// Intentionally no @Transactional: setup commits, and each service/HTTP call owns its transaction.
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        // Fail closed if service-connection wiring is accidentally removed; never use DB_URL.
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:1/do_not_use",
        "spring.datasource.username=unused",
        "spring.datasource.password=unused"
})
@AutoConfigureMockMvc
@Import(PostgresIntegrationSupport.PostgresConfiguration.class)
abstract class PostgresIntegrationSupport {
    static final String PASSWORD = "DevTrack-password-123";

    @TestConfiguration(proxyBeanMethods = false)
    static class PostgresConfiguration {
        // Spring owns the lifecycle, so the container lives as long as the shared application context.
        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>("postgres:17").withDatabaseName("devtrack_integration");
        }
    }

    @Autowired AppUserRepository users;
    @Autowired StudyBlockRepository blocks;
    @Autowired ProblemRepository problems;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @Autowired PostgreSQLContainer<?> postgres;

    @BeforeEach
    void verifyDatabaseAndClean() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).isEqualTo(postgres.getJdbcUrl());
            assertThat(connection.getMetaData().getDatabaseMajorVersion()).isEqualTo(17);
        }
        clean();
    }

    @AfterEach
    void clean() {
        SecurityContextHolder.clearContext();
        problems.deleteAllInBatch();
        blocks.deleteAllInBatch();
        users.deleteAllInBatch();
    }

    Account createAccount(String email) {
        AppUser user = users.saveAndFlush(new AppUser(email, passwordEncoder.encode(PASSWORD)));
        List<Long> blockIds = new ArrayList<>();
        List<Long> problemIds = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            StudyBlock block = blocks.saveAndFlush(new StudyBlock("Block " + i, true, user));
            blockIds.add(block.getId());
            for (int j = 0; j < 2; j++) {
                Problem problem = new Problem("Problem " + j, EASY, "Hash Map", false, "Notes", "https://example.com");
                problem.setStudyBlock(block);
                problemIds.add(problems.saveAndFlush(problem).getId());
            }
        }
        return new Account(user.getId(), email, List.copyOf(blockIds), List.copyOf(problemIds));
    }

    DatabaseSnapshot snapshot() {
        return new DatabaseSnapshot(
                jdbc.queryForList("select * from users order by id"),
                jdbc.queryForList("select * from study_blocks order by id"),
                jdbc.queryForList("select * from problems order by id"));
    }

    record Account(Long id, String email, List<Long> blockIds, List<Long> problemIds) {}
    record DatabaseSnapshot(List<Map<String, Object>> users, List<Map<String, Object>> blocks,
                            List<Map<String, Object>> problems) {}
}
