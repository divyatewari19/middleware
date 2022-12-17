package uk.ac.newcastle.enterprisemiddleware.guestbooking;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelRestService;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;


import javax.inject.Inject;
import java.net.URL;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestHTTPEndpoint(GuestBookingRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class GuestBookingRestServiceIntegrationTest {
    private static Customer customer;
    private static Hotel hotel;
    private static GuestBooking booking;
    @Inject
    HotelRestService hotelRestService;

    @Inject
    HotelService hotelService;
    @TestHTTPResource("/hotels")
    URL hotelEndpoint;

    @TestHTTPResource("/bookings")
    URL bookingEndpoint;

    // Date and time (GMT): Sunday, 17 December 2023 00:00:00 in milliseconds
    private Date futureBookingDateEpoch = new Date(1702771200000L);
    // Date and time (GMT): Thursday, 17 December 2020 00:00:00
    private Date pastBookingDateEpoch = new Date(1608163200000L);

    @BeforeAll
    static void setup() {
        // create a hotel Object
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("TestHotel");
        hotel.setPostCode("123456");
        hotel.setPhoneNumber("08866754322");

        // create Customer Object
        customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Account");
        customer.setEmail("test@email.com");
        customer.setPhoneNumber("08866754322");
    }

    @Test
    @Order(1)
    public void testCanCreateGuestBooking() {
        Response response = given().
                contentType(ContentType.JSON).
                body(hotel).
                when().
                post(hotelEndpoint).
                then().
                statusCode(201)
                .extract().response();

        System.out.println("POST hotel response " + response.toString()); // convert to JSON OBJECT?

        booking = createGuestBookingObject(customer, hotel.getId(), futureBookingDateEpoch);

        given().
                contentType(ContentType.JSON).
                body(booking).
                when().
                post().
                then().
                statusCode(201);
    }

    @Test
    @Order(2)
    public void testCanGetBooking() {
        Response response = when().
                get(bookingEndpoint).
                then().
                statusCode(200).
                extract().response();

        Booking[] result = response.body().as(Booking[].class);

        System.out.println("RESPONSE: " + result[0]);

        assertEquals(1, result.length);
        assertEquals(booking.getBookingDate(),result[0].getBookingDate(), "Booking date is not equal");
        assertTrue(booking.getCustomer().getFirstName().equals(result[0].getCustomer().getFirstName()), "Customer FirstName are not equal");
    }

    @Test
    @Order(3)
    public void testInvalidBooking() {
        // test with invalid hotel id
        booking = createGuestBookingObject(customer, 7L, futureBookingDateEpoch);

        try {
            given().
                    contentType(ContentType.JSON).
                    body(booking).
                    when().
                    post().
                    then().
                    statusCode(400);
        } catch (Exception e) {
            // Exception is expected
        }
    }

    private GuestBooking createGuestBookingObject(Customer customer, Long hotelId, Date bookingDate) {
        GuestBooking booking = new GuestBooking();
        booking.setId(1L);
        booking.setHotelId(hotelId);
        booking.setCustomer(customer);
        booking.setBookingDate(bookingDate);
        return booking;
    }
}
