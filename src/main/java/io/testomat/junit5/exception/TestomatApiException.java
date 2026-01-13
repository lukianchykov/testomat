package io.testomat.junit5.exception;

import lombok.Getter;

@Getter
public class TestomatApiException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public TestomatApiException(int statusCode, String responseBody) {
        super("Testomat API call failed with status " + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

}
