import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import io.qameta.allure.Step;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Epic("Demo")
@Feature("Allure Setup")
public class CreateCourierTests {
    private int courierId = 0;
    private String createBody;
    private String loginBody;
    private String createBodyError;

    private String readResourceFile(String fileName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new RuntimeException("Файл не найден: " + fileName);
        Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";

        createBody = readResourceFile("courier_create.json");
        loginBody = readResourceFile("courier_login.json");
        createBodyError = readResourceFile("courier_create_missingField.json");
    }

    @Step("Создание курьера + проверка статус кода/сообщения, удаление по id")
    @Test
    public void testCreateCourierPositive() {
        // Создание курьера
        Response createResponse = given()
                .header("Content-type", "application/json")
                .body(createBody)
                .when()
                .post("/api/v1/courier");

        assertEquals(201, createResponse.getStatusCode());
        //отличается от тз, в тз код 200
        boolean okValue = createResponse.jsonPath().getBoolean("ok");
        assertTrue("Expected 'ok' to be true", okValue);

        // Логин курьера
        Response loginResponse = given()
                .header("Content-type", "application/json")
                .body(loginBody)
                .when()
                .post("/api/v1/courier/login");

        loginResponse.then()
                .statusCode(200)
                .body("id", notNullValue());

        courierId = loginResponse.then().extract().path("id");
    }

    @Step("Создание курьера, попытка создать дубликат, удаление по id")
    @Test
    public void testCreateDuplicate() {
        given()
                .header("Content-type", "application/json")
                .body(createBody)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", is(true));

        Response loginResponse = given()
                .header("Content-type", "application/json")
                .body(loginBody)
                .when()
                .post("/api/v1/courier/login");

        loginResponse.then()
                .statusCode(200)
                .body("id", notNullValue());

        courierId = loginResponse.then().extract().path("id");

        given()
                .header("Content-type", "application/json")
                .body(createBody)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(409)
                .body("message", is("Этот логин уже используется. Попробуйте другой."));
                //отличается от тз, в тз текст ошибки "Этот логин уже используется"
    }

    @Step("Попытка создания курьера с отсутствующим полем")
    @Test
    public void testCreateCourierMissingField() {
        Response createResponse = given()
                .header("Content-type", "application/json")
                .body(createBodyError)
                .when()
                .post("/api/v1/courier");
        assertEquals(400, createResponse.getStatusCode());
    }

    @After
    // Удаление курьера если courierId != 0
    public void deleteCourier() {
        if (courierId != 0) {
            given()
                    .header("Content-type", "application/json")
                    .when()
                    .delete("/api/v1/courier/" + courierId)
                    .then()
                    .statusCode(200)
                    .body("ok", is(true));
        }
    }
}
