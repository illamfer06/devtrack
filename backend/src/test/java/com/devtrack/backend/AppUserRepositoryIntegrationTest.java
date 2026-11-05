package com.devtrack.backend;

import com.devtrack.backend.model.AppUser;
import com.devtrack.backend.model.Problem;
import com.devtrack.backend.model.StudyBlock;
import com.devtrack.backend.repository.AppUserRepository;
import com.devtrack.backend.repository.StudyBlockRepository;
import com.devtrack.backend.repository.ProblemRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static com.devtrack.backend.model.Difficulty.EASY;
import static com.devtrack.backend.model.Difficulty.HARD;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
// The disposable container removes the schema; Hibernate must not drop it after the container stops.
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create"})
public class AppUserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private StudyBlockRepository studyBlockRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindUserByEmail() {
        AppUser user = new AppUser();

        user.setEmail("test@devtrack.com");
        user.setPasswordHash("password-hash");

        appUserRepository.saveAndFlush(user);

        Optional<AppUser> result = appUserRepository.findByEmail("test@devtrack.com");

        assertTrue(result.isPresent());
        assertEquals("test@devtrack.com", result.get().getEmail());
    }

    @Test
    void shouldCreateFindUpdateAndDeleteStudyBlocksAndProblems() {
        AppUser user = new AppUser();

        user.setEmail("test@devtrack.com");
        user.setPasswordHash("password-hash");

        appUserRepository.saveAndFlush(user);

        Long userId = user.getId();

        StudyBlock studyBlock = new StudyBlock(
                "TitleTest",
                true,
                user
        );

        studyBlockRepository.saveAndFlush(studyBlock);

        Long studyBlockId = studyBlock.getId();

        Problem problem = new Problem(
                "TitleTest",
                EASY,
                "AlgorithmTest",
                false,
                "NotesTest",
                "URLTest"
        );

        problem.setStudyBlock(studyBlock);

        problemRepository.saveAndFlush(problem);

        Long problemId = problem.getId();

        entityManager.flush();
        entityManager.clear();

        Optional<StudyBlock> resultStudyBlock =
                studyBlockRepository.findByIdAndUserId(studyBlockId, userId);

        Optional<Problem> resultProblem =
                problemRepository.findByIdAndStudyBlockId(problemId, studyBlockId);

        StudyBlock foundStudyBlock = resultStudyBlock.orElseThrow();
        Problem foundProblem = resultProblem.orElseThrow();

        assertEquals(studyBlockId, foundStudyBlock.getId());
        assertEquals(userId, foundStudyBlock.getUser().getId());

        assertEquals(problem.getId(), foundProblem.getId());
        assertEquals(studyBlockId, foundProblem.getStudyBlock().getId());

        foundStudyBlock.setTitle("UpdatedStudyBlock");
        foundProblem.setTitle("UpdatedProblem");

        entityManager.flush();
        entityManager.clear();

        StudyBlock updatedStudyBlock = studyBlockRepository
                .findByIdAndUserId(studyBlockId, userId)
                .orElseThrow();

        Problem updatedProblem = problemRepository
                .findByIdAndStudyBlockId(problemId, studyBlockId)
                .orElseThrow();

        assertEquals("UpdatedStudyBlock", updatedStudyBlock.getTitle());
        assertEquals("UpdatedProblem", updatedProblem.getTitle());

        problemRepository.delete(updatedProblem);
        problemRepository.flush();

        studyBlockRepository.delete(updatedStudyBlock);
        studyBlockRepository.flush();

        entityManager.clear();

        assertTrue(problemRepository
                .findByIdAndStudyBlockId(problemId, studyBlockId)
                .isEmpty());

        assertTrue(studyBlockRepository
                .findByIdAndUserId(studyBlockId, userId)
                .isEmpty());
    }
}
