package uk.ac.newcastle.enterprisemiddleware.hotel;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestHTTPEndpoint(HotelRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class HotelRestServiceIntegrationTest {
    private static Hotel hotel;

    @BeforeAll
    static void setup() {
        hotel = new Hotel();
        hotel.setName("TestHotel");
        hotel.setPostCode("123456");
        hotel.setPhoneNumber("08866754322");
    }

    @Test
    @Order(1)
    public void testCanCreateHotel() {
        given().
                contentType(ContentType.JSON).
                body(hotel).
                when()
                .post().
                then().
                statusCode(201);
    }

    @Test
    @Order(2)
    public void testCanGetHotels() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Hotel[] result = response.body().as(Hotel[].class);

        System.out.println(result[0]);

        assertEquals(1, result.length);
        assertTrue(hotel.getName().equals(result[0].getName()), "Name not equal");
        assertTrue(hotel.getPostCode().equals(result[0].getPostCode()), "Postcode is not equal");
        assertTrue(hotel.getPhoneNumber().equals(result[0].getPhoneNumber()), "Phone number not equal");
    }

    @Test
    @Order(3)
    public void testDuplicatePhoneNumberCausesError() {
        given().
                contentType(ContentType.JSON).
                body(hotel).
                when().
                post().
                then().
                statusCode(409).
                body("reasons.phoneNumber", containsString("That phone number is already used, please use a unique phone number"));
    }

    @Test
    @Order(4)
    public void testPhoneFormatCausesError() {
        Hotel hotel = new Hotel();
        hotel.setName("TestHotel");
        hotel.setPostCode("123456");
        hotel.setPhoneNumber("(201) 123-4567");

        given().
                contentType(ContentType.JSON).
                body(hotel).
                when().
                post().
                then().
                statusCode(400).
                body("reasons.phoneNumber", containsString("The phone number starts with a 0, contains only digits and is 11 characters in length."));
    }

    @Test
    @Order(4)
    public void testPostCodeFormatCausesError() {
        Hotel hotel = new Hotel();
        hotel.setName("TestHotel");
        hotel.setPostCode("12345697");
        hotel.setPhoneNumber("08866754322");

        given().
                contentType(ContentType.JSON).
                body(hotel).
                when().
                post().
                then().
                statusCode(400).
                body("reasons.postCode", containsString("Please use a non-empty alpha-numerical string which is 6 characters in length"));
    }

    @Test
    @Order(5)
    public void testCanDeleteHotel() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Hotel[] result = response.body().as(Hotel[].class);

        when().
                delete(result[0].getId().toString()).
                then().
                statusCode(204);
    }

}
