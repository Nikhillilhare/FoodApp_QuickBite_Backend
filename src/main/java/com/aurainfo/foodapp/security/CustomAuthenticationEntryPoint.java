package com.aurainfo.foodapp.security;

import com.aurainfo.foodapp.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public CustomAuthenticationEntryPoint(
            JsonMapper jsonMapper
    ) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        ApiErrorResponse errorResponse =
                ApiErrorResponse.builder()
                        .success(false)
                        .message(
                                "Authentication is required to access this resource"
                        )
                        .status(
                                HttpStatus.UNAUTHORIZED.value()
                        )
                        .timestamp(
                                LocalDateTime.now()
                        )
                        .path(
                                request.getRequestURI()
                        )
                        .build();

        response.setStatus(
                HttpStatus.UNAUTHORIZED.value()
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