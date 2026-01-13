package io.testomat.junit5;

import io.testomat.junit5.annotations.TestId;
import io.testomat.junit5.annotations.Title;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TestomatReporterExtension.class)
@Title("My Awesome Junit 5 Test Run")
public class TestomatExampleTest {

    @Test
    @TestId("T12345")
    @Title("Verify basic addition operation")
    void testAddition() {
        int result = 2 + 2;
        assertEquals(4, result, "Addition should work correctly");
    }

    @Test
    @TestId("T12346")
    @Title("Verify subtraction with negative result")
    void testSubtraction() {
        int result = 5 - 10;
        assertEquals(-5, result, "Subtraction should handle negative results");
    }

    @Test
    @TestId("T12347")
    @Title("Verify multiplication by zero")
    void testMultiplicationByZero() {
        int result = 100 * 0;
        assertEquals(0, result, "Multiplication by zero should equal zero");
    }

    @Test
    @TestId("T12348")
    @Title("Verify division throws exception on divide by zero")
    void testDivisionByZero() {
        assertThrows(ArithmeticException.class, () -> {
            int result = 10 / 0;
        }, "Division by zero should throw ArithmeticException");
    }

    @Test
    @Title("Test without TestId annotation")
    void testWithoutTestId() {
        String message = "Hello, Testomat.io!";
        assertTrue(message.contains("Testomat"), "Message should contain 'Testomat'");
    }

    @Test
    @Disabled("Skipped for demonstration purposes")
    @TestId("T12350")
    @Title("Skipped test example")
    void testSkipped() {
        fail("This test should be skipped and never executed");
    }

//    @Test
//    @TestId("T12349")
//    @Title("Intentionally failing test")
//    void testFailure() {
//        int result = 2 + 2;
//        assertEquals(5, result, "This test is expected to fail");
//    }
}