import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Step;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Demo")
@Feature("Allure Setup")
public class GetOrderListTests {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    @Step("Получение списка из 10 доступных заказов и проверка что не пустое тело ответа")
    @Test
    public void getOrdersListNotEmpty() {
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .get("/api/v1/orders?limit=10");

        response.then()
                .statusCode(200)
                .body("orders", not(empty()))
                .body("orders", notNullValue());
    }
}
