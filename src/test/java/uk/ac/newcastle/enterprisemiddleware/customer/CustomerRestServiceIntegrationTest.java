package uk.ac.newcastle.enterprisemiddleware.customer;

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
@TestHTTPEndpoint(CustomerRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class CustomerRestServiceIntegrationTest {
    private static Customer customer;

    @BeforeAll
    static void setup() {
        customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Account");
        customer.setEmail("test@email.com");
        customer.setPhoneNumber("08866754322");
    }

    @Test
    @Order(1)
    public void testCanCreateCustomer() {
        given().
                contentType(ContentType.JSON).
                body(customer).
                when()
                .post().
                then().
                statusCode(201);
    }

    @Test
    @Order(2)
    public void testCanGetCustomers() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Customer[] result = response.body().as(Customer[].class);

        System.out.println(result[0]);

        assertEquals(1, result.length);
        assertTrue(customer.getFirstName().equals(result[0].getFirstName()), "First name not equal");
        assertTrue(customer.getLastName().equals(result[0].getLastName()), "Last name not equal");
        assertTrue(customer.getEmail().equals(result[0].getEmail()), "Email not equal");
        assertTrue(customer.getPhoneNumber().equals(result[0].getPhoneNumber()), "Phone number not equal");
    }

    @Test
    @Order(3)
    public void testDuplicateEmailCausesError() {
        given().
                contentType(ContentType.JSON).
                body(customer).
                when().
                post().
                then().
                statusCode(409).
                body("reasons.email", containsString("email is already used"));
    }

    @Test
    @Order(4)
    public void testPhoneFormatCausesError() {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Account");
        customer.setEmail("test@email.com");
        customer.setPhoneNumber("(201) 123-4567");

        given().
                contentType(ContentType.JSON).
                body(customer).
                when().
                post().
                then().
                statusCode(400).
                body("reasons.phoneNumber", containsString("The phone number starts with a 0, contains only digits and is 11 characters in length."));
    }

    @Test
    @Order(5)
    public void testEmptyRequiredFieldCausesError() {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Account");
        customer.setEmail("");
        customer.setPhoneNumber("08866754322");

        given().
                contentType(ContentType.JSON).
                body(customer).
                when().
                post().
                then().
                statusCode(400).
                body("reasons.email", containsString("must not be empty"));
    }

    @Test
    @Order(6)
    public void testCanDeleteCustomer() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Customer[] result = response.body().as(Customer[].class);

        when().
                delete(result[0].getId().toString()).
                then().
                statusCode(204);
    }
}
