package com.offerflow.copilot.api;

import com.offerflow.copilot.security.local.LocalPermissionDeniedException;
import com.offerflow.copilot.security.local.PermissionDecision;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LocalPermissionExceptionHandler {

    @ExceptionHandler(LocalPermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public PermissionDecision permissionDenied(LocalPermissionDeniedException exception) {
        return exception.decision();
    }
}
