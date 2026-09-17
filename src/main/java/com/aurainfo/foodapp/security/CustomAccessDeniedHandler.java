package com.aurainfo.foodapp.security;

import com.aurainfo.foodapp.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public CustomAccessDeniedHandler(
            JsonMapper jsonMapper
    ) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        ApiErrorResponse errorResponse =
                ApiErrorResponse.builder()
                        .success(false)
                        .message(
                                "You do not have permission to access this resource"
                        )
                        .status(
                                HttpStatus.FORBIDDEN.value()
                        )
                        .timestamp(
                                LocalDateTime.now()
                        )
                        .path(
                                request.getRequestURI()
                        )
                        .build();

        response.setStatus(
                HttpStatus.FORBIDDEN.value()
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        jsonMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}