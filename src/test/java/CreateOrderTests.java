import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.response.Response;
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
public class CreateOrderTests {
    private String createOrderBlack;
    private String createOrderGrey;
    private String createOrderBlackGrey;
    private String createOrderNoColor;


    private String readResourceFile(String fileName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new RuntimeException("Файл не найден: " + fileName);
        Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";

        createOrderBlack = readResourceFile("order_create_black.json");
        createOrderGrey = readResourceFile("order_create_grey.json");
        createOrderBlackGrey = readResourceFile("order_create_black_grey.json");
        createOrderNoColor = readResourceFile("order_create_no_color.json");
    }

    @Step("Создание ордера BLACK + проверка статус кода/сообщения")
    @Test
    public void testCreateOrderBlack() {

        Response response = given()
                .header("Content-type", "application/json")
                .body(createOrderBlack)
                .when()
                .post("/api/v1/orders");

        response.then()
                .statusCode(201)
                .body("track", notNullValue());

        System.out.println("Track ID BLACK: " + response.jsonPath().getString("track"));
    }

    @Step("Создание ордера GREY + проверка статус кода/сообщения")
    @Test
    public void testCreateOrderGrey() {

        Response response = given()
                .header("Content-type", "application/json")
                .body(createOrderGrey)
                .when()
                .post("/api/v1/orders");

        response.then()
                .statusCode(201)
                .body("track", notNullValue());

        System.out.println("Track ID GREY: " + response.jsonPath().getString("track"));
    }

    @Step("Создание ордера BLACK/GREY + проверка статус кода/сообщения")
    @Test
    public void testCreateOrderBlackGrey() {

        Response response = given()
                .header("Content-type", "application/json")
                .body(createOrderBlackGrey)
                .when()
                .post("/api/v1/orders");

        response.then()
                .statusCode(201)
                .body("track", notNullValue());

        System.out.println("Track ID BLACK/GREY: " + response.jsonPath().getString("track"));
    }

    @Step("Создание ордера no color + проверка статус кода/сообщения")
    @Test
    public void testCreateOrderNoColor() {

        Response response = given()
                .header("Content-type", "application/json")
                .body(createOrderNoColor)
                .when()
                .post("/api/v1/orders");

        response.then()
                .statusCode(201)
                .body("track", notNullValue());

        System.out.println("Track ID no color: " + response.jsonPath().getString("track"));
    }
}
