package uk.ac.newcastle.enterprisemiddleware.booking;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerRestService;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelRestService;

import javax.inject.Inject;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestHTTPEndpoint(BookingRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class BookingRestServiceIntegrationTest {
    private static Booking booking;
    private static Customer customer;
    private static Hotel hotel;

    @Inject
    CustomerRestService customerRestService;

    @Inject
    HotelRestService hotelRestService;


    // Date and time (GMT): Sunday, 17 December 2023 00:00:00 in milliseconds
    private Date futureBookingDateEpoch = new Date(1702771200000L);
    // Date and time (GMT): Thursday, 17 December 2020 00:00:00
    private Date pastBookingDateEpoch = new Date(1608163200000L);

    @BeforeAll
    static void setup() {
    }

    @Test
    @Order(1)
    public void testCanCreateBooking() {
        customer = persistCustomer(createCustomerObject());

        // initialise Hotel
        hotel = persistHotel(createHotelObject());

        // initialise Booking
        booking = createBookingObject(customer, hotel, futureBookingDateEpoch);

        given().
                contentType(ContentType.JSON).
                body(booking).
                when()
                .post().
                then().
                statusCode(201);
    }

    @Test
    @Order(2)
    public void testCanGetBooking() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Booking[] result = response.body().as(Booking[].class);

        System.out.println(result[0]);

        assertEquals(1, result.length);
        assertEquals(booking.getBookingDate(),result[0].getBookingDate(), "Booking date is not equal");
        assertTrue(booking.getCustomer().getId().equals(result[0].getCustomer().getId()), "Customer Id are not equal");
        assertTrue(booking.getCustomer().getFirstName().equals(result[0].getCustomer().getFirstName()), "Customer FirstName are not equal");
        assertTrue(booking.getHotel().getName().equals(result[0].getHotel().getName()), "Hotel Name are not equal");
    }

    @Test
    @Order(3)
    public void testDuplicateDateCausesError() {

        given().
                contentType(ContentType.JSON).
                body(booking).
                when().
                post().
                then().
                statusCode(409)
                .body("error", containsString("Bad Request"))
                .body("reasons.booking", containsString("booking is already registered, please register on another date or hotel"));
    }

    @Test
    @Order(4)
    public void testCanDeleteBooking() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Booking[] result = response.body().as(Booking[].class);

        when().
                delete(result[0].getId().toString()).
                then().
                statusCode(204);
    }

    @Test
    @Order(5)
    public void testCanRemakeBooking() {
        given().
                contentType(ContentType.JSON).
                body(booking).
                when().
                post().
                then().
                statusCode(201);
    }

    @Test
    @Order(6)
    public void testPastDateCausesError() {
        Booking invalidBooking = createBookingObject(customer, hotel, pastBookingDateEpoch);
        given().
                contentType(ContentType.JSON).
                body(invalidBooking).
                when().
                post().
                then().
                statusCode(400).
                body("reasons.bookingDate", containsString("Booking date can not be in the past. Please choose one from the future"));
    }

    private Booking createBookingObject(Customer customer, Hotel hotel, Date bookingDate) {
        booking = new Booking();
        booking.setId(1L);
        booking.setHotel(hotel);
        booking.setCustomer(customer);
        booking.setBookingDate(bookingDate);
        return booking;
    }

    private Hotel createHotelObject() {
        Hotel hotel = new Hotel();
        hotel.setName("TestHotel");
        hotel.setPostCode("123456");
        hotel.setPhoneNumber("08866754321");
        return hotel;
    }

    private Customer createCustomerObject(){
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Account");
        customer.setEmail("test@email.com");
        customer.setPhoneNumber("08866754322");
        return customer;
    }

    private Customer persistCustomer(Customer customer){
        try {
            customerRestService.createCustomer(customer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return customer;
    }

    private Hotel persistHotel(Hotel hotel){
        try {
            hotelRestService.createHotel(hotel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hotel;
    }
}
