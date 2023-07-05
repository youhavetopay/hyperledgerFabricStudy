package com.example.HyperledgerSpring;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler({
            HttpServerErrorException.class
    })
    public ResponseEntity<Object> handleHttpServerErrorException(final HttpServerErrorException exception){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", exception.getStatusCode());
        errorResponse.put("message", exception.getMessage());

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(errorResponse);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<Object> handleBadRequestException(final Exception exception){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("message", "필수 인자가 없습니다.");
        errorResponse.put("server error message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

}
