package com.orange.rest_vacinas.rest.error_handling;

import com.orange.rest_vacinas.rest.RestResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class VaccineApiErrorHandler {
    @ExceptionHandler
    public ResponseEntity<RestResponse> invalidFormHandler(InvalidFormException e) {
        e.printStackTrace();
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.BAD_REQUEST.value(),
                        "Dados de cadastro inválidos: " + e.getInvalidValue()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<RestResponse> duplicateCpfHandler(DuplicateKeyException e) {
        e.printStackTrace();
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.BAD_REQUEST.value(),
                        e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler ResponseEntity<RestResponse> userNotFoundHandler(UserNotFoundException e) {
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.BAD_REQUEST.value(),
                        e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<RestResponse> badRequestHandler(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(
                new RestResponse(HttpStatus.BAD_REQUEST.value(), "Dados de cadastro inválidos"),
                HttpStatus.BAD_REQUEST
        );
    }
}
