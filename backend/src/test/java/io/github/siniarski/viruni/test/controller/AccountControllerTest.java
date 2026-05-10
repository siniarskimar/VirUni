package io.github.siniarski.viruni.test.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import io.github.siniarski.viruni.dto.request.UpdateAccountRequest;
import io.github.siniarski.viruni.dto.response.AccountResponse;
import io.github.siniarski.viruni.security.permission.AccountPermission;
import io.github.siniarski.viruni.test.BaseIntegrationTest;
import io.github.siniarski.viruni.test.ContainerizedConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ContainerizedConfiguration.class)
public class AccountControllerTest extends BaseIntegrationTest {

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

    @AfterEach
    void afterEach() {
        AUTH_RESPONSES.clear();
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

    @Test
    @DisplayName("GET /account/<id> returns own account details")
    public void getAccount_returnsOwnAccount() {
        var auth = fetchSignInResponse("aliciaprice", "magics");

        var resp = givenAuthenticatedAs("aliciaprice", "magics")
                .contentType(ContentType.JSON)
                .when()
                .log().ifValidationFails(LogDetail.ALL)
                .get("/account/" + auth.getAccountId())
                .then()
                .log().ifValidationFails(LogDetail.BODY)
                .statusCode(200)
                .extract()
                .as(AccountResponse.class);

        assertThat(resp).usingRecursiveComparison()
                .isEqualTo(new AccountResponse(
                        auth.getAccountId(),
                        "aliciaprice",
                        "Alicia",
                        "Price",
                        Set.of(
                                AccountPermission.DELETE,
                                AccountPermission.VIEW,
                                AccountPermission.EDIT,
                                AccountPermission.EDIT_CREDENTIALS
                        )
                ));
    }

    @Test
    @DisplayName("PATCH /account/<id> saves own account details to repository")
    public void patchAccount_savesOwnAccountDetailsToRepository() {
        var auth = fetchSignInResponse("ramirezangela", "magics");
        var updateRequest = new UpdateAccountRequest("Ramira", "Jolie", "supersecret123");

        var originalAccount = accountRepository.findByUsername(auth.getUsername()).orElseThrow();
        givenAuthenticatedAs("ramirezangela", "magics")
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .patch("/account/"+auth.getAccountId())
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        var changed = accountRepository.findByUsername(auth.getUsername()).orElseThrow();

        assertThat(changed.getFirstname()).isEqualTo(updateRequest.firstname());
        assertThat(changed.getLastname()).isEqualTo(updateRequest.lastname());
        assertThat(changed.getPassword()).isNotEqualTo(originalAccount.getPassword());
    }
}
