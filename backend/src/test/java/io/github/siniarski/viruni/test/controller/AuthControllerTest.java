package io.github.siniarski.viruni.test.controller;

import io.github.siniarski.viruni.dto.request.SignInRequest;
import io.github.siniarski.viruni.dto.request.SignUpRequest;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.test.BaseIntegrationTest;
import io.github.siniarski.viruni.test.ContainerizedConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ContainerizedConfiguration.class)
public class AuthControllerTest extends BaseIntegrationTest {

    @LocalServerPort
    private Integer serverPort;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost:" + serverPort;
        accountRepository.deleteAll();
        insertMockAccounts();
    }

    void insertMockAccounts() {
        List<Account> accounts = List.of(
                new Account(
                        "ramirezangela", passwordEncoder.encode("magics"),
                        "Angela", "Ramirez", AccountRole.USER),
                new Account(
                        "admin", passwordEncoder.encode("admin"),
                        "System", "admin", AccountRole.ADMIN)
        );

        accountRepository.saveAll(accounts);
    }

    static Stream<Arguments> bogusCredentialsCases() {
        return Stream.of(
                Arguments.of("nonsensical", "doesnotexist"),
                Arguments.of("admin", "notthistime"),
                Arguments.of("remirezangela", "RAMIREZ"),
                Arguments.of("johndoe", "magics"),
                Arguments.of("postgres", "postgres"),
                Arguments.of("phpmyadmin", "admin")
        );
    }

    static Stream<Arguments> validCredentialsCases() {
        return Stream.of(
                Arguments.of("admin", "admin"),
                Arguments.of("ramirezangela", "magics")
        );
    }


    @ParameterizedTest
    @MethodSource("bogusCredentialsCases")
    @DisplayName("POST /signin rejects invalid credentials")
    void postSignin_rejectsInvalidCredentials(String bogusUsername, String bogusPassword) {
        given()
                .contentType(ContentType.JSON)
                .body(new SignInRequest(bogusUsername, bogusPassword))
                .log().ifValidationFails()
                .post("/signin")
                .then()
                .log().ifValidationFails()
                .statusCode(401);
    }

    @ParameterizedTest
    @MethodSource("validCredentialsCases")
    @DisplayName("POST /signin succeeds on valid credentials")
    void postSignin_succeedsOnValidCredentials(String username, String password) {
        given()
                .contentType(ContentType.JSON)
                .body(new SignInRequest(username, password))
                .log().ifValidationFails()
                .post("/signin")
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /signup forbids duplicate usernames")
    void postSignup_forbidsDuplicateUsernames() {
        given()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest())
                .log().ifValidationFails()
                .post("/signup")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /signup forbids regristration of administrators")
    void postSignup_forbidsRegristrationOfAdministrators() {
        var req = new SignUpRequest();

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest())
                .log().ifValidationFails()
                .post("/signup")
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }

}
