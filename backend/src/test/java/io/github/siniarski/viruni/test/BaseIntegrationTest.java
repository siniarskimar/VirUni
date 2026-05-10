package io.github.siniarski.viruni.test;

import io.github.siniarski.viruni.dto.request.SignInRequest;
import io.github.siniarski.viruni.dto.response.SignInResponse;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groups common functions used for integration tests.
 *
 * All integration tests are expected to extend this class
 */
public abstract class BaseIntegrationTest {
    protected static final Map<String, SignInResponse> AUTH_RESPONSES = new ConcurrentHashMap<>();

    protected RequestSpecification givenAuthenticatedAs(String username, String password) {
        return RestAssured.given().spec(authSpec(username, password))
                .log().ifValidationFails();
    }

    protected static RequestSpecification authSpec(String username, String password) {
        return new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + fetchSignInResponse(username, password).getToken())
                .build();
    }

    protected static SignInResponse fetchSignInResponse(String username, String password) {
        return AUTH_RESPONSES.computeIfAbsent(
                username,
                k -> RestAssured.given()
                        .filters(Collections.emptyList())
                        .contentType(ContentType.JSON)
                        .body(new SignInRequest(k, password))
                        .post("/signin")
                        .then()
                        .log().ifValidationFails()
                        .statusCode(200)
                        .extract()
                        .as(SignInResponse.class)
        );
    }
}
