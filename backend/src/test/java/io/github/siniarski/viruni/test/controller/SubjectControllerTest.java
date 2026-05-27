package io.github.siniarski.viruni.test.controller;

import io.github.siniarski.viruni.dto.request.CreateSubjectRequest;
import io.github.siniarski.viruni.dto.request.UpdateSubjectRequest;
import io.github.siniarski.viruni.dto.response.PagedResponse;
import io.github.siniarski.viruni.dto.response.SubjectResponse;
import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.repository.SubjectRepository;
import io.github.siniarski.viruni.test.BaseIntegrationTest;
import io.github.siniarski.viruni.test.ContainerizedConfiguration;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ContainerizedConfiguration.class)
public class SubjectControllerTest extends BaseIntegrationTest {
    @LocalServerPort
    private Integer serverPort;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost:" + serverPort;
    }

    @AfterEach
    void afterEach() {
        AUTH_RESPONSES.clear();
    }

    static Stream<Arguments> accountsWithoutSubjectManagmentPermissionsStream() {
        return Stream.of(
                Arguments.of("aliciaprice", "magics"),
                Arguments.of("charlesangelica", "secret")
        );
    }

    @Test
    @DisplayName("GET /subject forbids unauthenticated users")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void getSubject_forbidsUnauthenticated() {
        given()
                .contentType(ContentType.JSON)
                .log().ifValidationFails()
                .get("/subject")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("GET /subject lists subjects")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void getSubject_listing() {
        var resp = givenAuthenticatedAs("admin", "admin")
                .contentType(ContentType.JSON)
                .log().ifValidationFails()
                .get("/subject")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<PagedResponse<Subject>>(){});

        assertThat(resp.totalElements()).isEqualTo(6);
        assertThat(resp.content().stream().map(Subject::getId))
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
    }

    @Test
    @DisplayName("GET /subject?participant=<id> filters by participants")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void getSubject_filtersByParticipant() {
        var participant = accountRepository.findById(2L).orElseThrow();

        var resp = givenAuthenticatedAs("charlesangelica", "secret")
                .contentType(ContentType.JSON)
                .log().ifValidationFails()
                .queryParam("participant", participant.getId())
                .get("/subject")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<PagedResponse<Subject>>(){});

        assertThat(resp.totalElements()).isEqualTo(3);
        assertThat(resp.content().stream().map(Subject::getId))
                .containsExactly(2L, 4L, 5L);
    }

    @ParameterizedTest
    @DisplayName("POST /subject forbids creating new subjects by teachers and regular users")
    @MethodSource("accountsWithoutSubjectManagmentPermissionsStream")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void postSubject_forbidsTeachersAndRegularUsers(String username, String password) {
        var leadingTeacher = accountRepository.findById(8L).orElseThrow();

        givenAuthenticatedAs(username, password)
                .contentType(ContentType.JSON)
                .body(new CreateSubjectRequest(
                        "Scripting Languages",
                        null,
                        null,
                        leadingTeacher.getUsername()
                ))
                .log().ifValidationFails()
                .post("/subject")
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }

    @ParameterizedTest
    @DisplayName("DELETE /subject/<id> forbids deleting subjects by teachers and regular users")
    @MethodSource("accountsWithoutSubjectManagmentPermissionsStream")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void deleteSubject_forbidsTeachersAndRegularUsers(String username, String password) {
        // Given subject of id 1 exists
        var subject = subjectRepository.findById(1L).orElseThrow();

        givenAuthenticatedAs(username, password)
                .contentType(ContentType.JSON)
                .log().ifValidationFails()
                .delete("/subject/"+subject.getId())
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }

    @Test
    @DisplayName("POST /subject permits creating new subjects by admins")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void postSubject_permitsAdmins() {
        var leadingTeacher = accountRepository.findById(9L).orElseThrow();

        var resp = givenAuthenticatedAs("admin", "admin")
                .contentType(ContentType.JSON)
                .body(new CreateSubjectRequest(
                        "Virtual Reality",
                        null,
                        null,
                        leadingTeacher.getUsername()
                ))
                .log().ifValidationFails()
                .post("/subject")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .extract().as(SubjectResponse.class);
    }

    @Test
    @DisplayName("DELETE /subject/<id> permits deleting subjects by admins")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void deleteSubject_permitsAdmins() {
        // Given subject of id 1 exists
        var subject = subjectRepository.findById(1L).orElseThrow();

        givenAuthenticatedAs("admin", "admin")
                .contentType(ContentType.JSON)
                .log().ifValidationFails()
                .delete("/subject/"+subject.getId())
                .then()
                .log().ifValidationFails()
                .statusCode(204);

        assertThat(subjectRepository.findById(1L).orElse(null)).isNull();
    }

    @Test
    @DisplayName("PATCH /subject/<id> permits admins to change subject details")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void patchSubject_permitsAdmins() {
        // Given subject of id 1 exists
        var subject = subjectRepository.findById(1L).orElseThrow();

        givenAuthenticatedAs("admin", "admin")
                .contentType(ContentType.JSON)
                .body(new UpdateSubjectRequest("Dolar dolar", "MONEY"))
                .log().ifValidationFails()
                .patch("/subject/"+subject.getId())
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        subject = subjectRepository.findById(1L).orElseThrow();
        assertThat(subject.getName()).isEqualTo("Dolar dolar");
        assertThat(subject.getDescription()).isEqualTo("MONEY");
    }

    @Test
    @DisplayName("PATCH /subject/<id> forbids to change subject details")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void patchSubject_forbidsTeachersFromNameUpdate() {
        // Given subject of id 1 exists
        var subject = subjectRepository.findById(1L).orElseThrow();

        givenAuthenticatedAs("charlesangelica", "secret")
                .contentType(ContentType.JSON)
                .body(new UpdateSubjectRequest("Macroeconomics Rulez", null))
                .log().ifValidationFails()
                .patch("/subject/"+subject.getId())
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        var changedSubject = subjectRepository.findById(1L).orElseThrow();
        assertThat(changedSubject.getName()).isEqualTo("Macroeconomics Rulez");
        assertThat(changedSubject.getDescription()).isEqualTo(subject.getDescription());
    }

    @Test
    @DisplayName("POST /subject/<id>/account allows teachers to add participants")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void postSubjectAccount_teachersCanAddParticipants() {
        var subject = subjectRepository.findById(1L).orElseThrow();
        var targetParticipant = accountRepository.findById(2L).orElseThrow();

        givenAuthenticatedAs("charlesangelica", "secret")
                .contentType(ContentType.JSON)
                .body(List.of(targetParticipant.getId()))
                .log().ifValidationFails()
                .post("/subject/%d/account".formatted(subject.getId()))
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    @Test
    @DisplayName("DELETE /subject/<id>/account allows teachers to remove participants")
    @Sql("classpath:/fixtures/sql/mock_data.sql")
    void deleteSubjectAccount_teachersCanRemoveParticipants() {
        var subject = subjectRepository.findById(1L).orElseThrow();
        var targetParticipant = accountRepository.findById(1L).orElseThrow();

        givenAuthenticatedAs("charlesangelica", "secret")
                .contentType(ContentType.JSON)
                .log().ifValidationFails()
                .delete("/subject/%d/account/%d".formatted(subject.getId(), targetParticipant.getId()))
                .then()
                .log().ifValidationFails()
                .statusCode(204);
    }


}
