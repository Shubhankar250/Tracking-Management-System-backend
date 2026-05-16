package com.trackingpath.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleSecurityException(Exception exception) {
        ProblemDetail errorDetail = null;

        // TODO send this stack trace to an observability tool
        exception.printStackTrace();

        if (exception instanceof BadCredentialsException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
            errorDetail.setProperty("description", "The username or password is incorrect");

            return problem(HttpStatus.UNAUTHORIZED, errorDetail);
        }

        if (exception instanceof AccountStatusException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "The account is locked");
            return problem(HttpStatus.FORBIDDEN, errorDetail);
        }

        if (exception instanceof AccessDeniedException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "You are not authorized to access this resource");
            return problem(HttpStatus.FORBIDDEN, errorDetail);
        }

        if (exception instanceof SignatureException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "The JWT signature is invalid");
            return problem(HttpStatus.FORBIDDEN, errorDetail);
        }

        if (exception instanceof ExpiredJwtException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "The JWT token has expired");
            return problem(HttpStatus.FORBIDDEN, errorDetail);
        }

        if (errorDetail == null) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
            errorDetail.setProperty("description", "Unknown internal server error.");
        }

        return problem(HttpStatus.INTERNAL_SERVER_ERROR, errorDetail);
    }
 // ===== New handler for your POI validation =====
    @ExceptionHandler(IncorrectArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIncorrectArgumentException(IncorrectArgumentException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), ex.getMessage());
        errorDetail.setProperty("description", "Invalid request data: missing or incorrect duration");
        return problem(HttpStatus.BAD_REQUEST, errorDetail);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxSizeException(MaxUploadSizeExceededException ex) {

        ProblemDetail errorDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE,
                        "Uploaded file exceeds maximum allowed size");

        errorDetail.setProperty("description",
                "Please upload files within allowed size limit");

        return problem(HttpStatus.PAYLOAD_TOO_LARGE, errorDetail);
    }
    
    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileSizeExceeded(FileSizeExceededException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("status", 400);
        error.put("field", ex.getFieldName());
        error.put("message", ex.getMessage());

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, ProblemDetail errorDetail) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(errorDetail);
    }
}

