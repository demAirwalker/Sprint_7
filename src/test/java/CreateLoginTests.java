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

@Epic("Demo")
@Feature("Allure Setup")
public class CreateLoginTests {

    private Integer courierId = null;
    private String createBody;
    private String loginBody;
    private String loginBodyInvalid;
    private String loginMissingField;

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
        loginBodyInvalid = readResourceFile("courier_login_invalid_user.json");
        loginMissingField = readResourceFile("courier_login_missingLogin.json");
    }

    @Step("Создание курьера, логин + проверка статус кода/сообщения, удаление по id")
    @Test
    public void testLoginPositive() {
        // Создание курьера
        given()
                .header("Content-type", "application/json")
                .body(createBody)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", is(true));

        // Логин курьера
        Response loginResponse = given()
                .header("Content-type", "application/json")
                .body(loginBody)
                .when()
                .post("/api/v1/courier/login");

        loginResponse.then()
                .statusCode(200)
                .body("id", notNullValue());

        //Сохранение id для удаления в @After
        courierId = loginResponse.then().extract().path("id");
    }

    @Step("Попытка логина несуществующего пользователя")
    @Test
    public void testLoginInvalidUser() {
        Response loginResponse = given()
                .header("Content-type", "application/json")
                .body(loginBodyInvalid)
                .when()
                .post("/api/v1/courier/login");
        loginResponse.then()
                .statusCode(404);
    }

    @Step("Попытка логина без поля login")
    @Test
    public void testLoginMissingField() {
        Response loginResponse = given()
                .header("Content-type", "application/json")
                .body(loginMissingField)
                .when()
                .post("/api/v1/courier/login");
        loginResponse.then()
                .statusCode(400);
    }


    @After
    // Удаление курьера если courierId != 0
    public void deleteCourier() {
        if (courierId != null) {
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
