package com.orange.rest_vacinas.rest.error_handling;

public class InvalidFormException extends RuntimeException{
    private final String invalidValue;

    public InvalidFormException(String message, String invalidValue) {
        super(message);
        this.invalidValue = invalidValue;
    }

    public InvalidFormException(String message, Throwable cause, String invalidValue) {
        super(message, cause);
        this.invalidValue = invalidValue;
    }

    public InvalidFormException(Throwable cause, String invalidValue) {
        super(cause);
        this.invalidValue = invalidValue;
    }

    public String getInvalidValue() {
        return invalidValue;
    }
}
