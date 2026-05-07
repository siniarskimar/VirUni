package io.github.siniarski.viruni.test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import io.github.siniarski.viruni.dto.response.SignInResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.siniarski.viruni.dto.request.SignInRequest;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ContainerizedConfiguration.class)
public class AccountControllerTest {

    @LocalServerPort
    private Integer serverPort;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() {
        RestAssured.requestSpecification = null;
        RestAssured.baseURI = "http://localhost:" + serverPort;
        accountRepository.deleteAll();
    }

    void insertMockAccounts() {
        List<Account> mockAccounts = List.of(
                new Account(
                        "aliciaprice", passwordEncoder.encode("magics"),
                        "Alicia", "Price", AccountRole.USER),
                new Account(
                        "ramirezangela", passwordEncoder.encode("magics"),
                        "Angela", "Ramirez", AccountRole.USER),
                new Account(
                        "charlesangelica", passwordEncoder.encode("secret"),
                        "Angelica", "Charles", AccountRole.TEACHER));

        accountRepository.saveAll(mockAccounts);
    }

    SignInResponse authorizeAs(SignInRequest form) {
        var resp = given()
                .contentType(ContentType.JSON)
                .body(form)
                .post("/signin");

        resp.then().statusCode(200);
        var body = resp.as(SignInResponse.class);

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + body.getToken())
                .build();

        return body;
    }


    @Test
    public void shouldGetAccount() {
        insertMockAccounts();
        var authorization = authorizeAs(new SignInRequest("aliciaprice", "magics"));

        given()
                .contentType(ContentType.JSON)
                .when()
                .log().ifValidationFails(LogDetail.ALL)
                .get("/account/" + authorization.getAccountId())
                .then()
                .log().ifValidationFails(LogDetail.BODY)
                .statusCode(200)
                .body("id", equalTo((int) authorization.getAccountId()))
                .body("firstname", equalTo("Alicia"))
                .body("lastname", equalTo("Price"));
    }
}
