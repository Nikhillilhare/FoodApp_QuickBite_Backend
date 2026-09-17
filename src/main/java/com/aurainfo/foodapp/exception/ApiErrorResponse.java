package com.aurainfo.foodapp.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The single, consistent shape every error response takes across the whole
 * API. Whether the error comes from the Auth module, Product module, Order
 * module, or anywhere else, the frontend always receives exactly this shape
 * — so it only ever needs to write ONE piece of code to read `message` and
 * show it in a toast/snackbar, regardless of which endpoint failed.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    // Always false for an error response — lets the frontend check
    // `response.success` to decide "was this a success or an error"
    // without needing to inspect the HTTP status code separately.
    private boolean success;

    // The human-readable message — THIS is what the frontend shows the user.
    private String message;

    // The HTTP status code, repeated here as a plain number so the frontend
    // can branch on it easily (e.g. 404 vs 409) without re-parsing headers.
    private int status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Which endpoint the error happened on — very useful when debugging a
    // bug report from the client ("it broke on checkout" vs actually knowing
    // it was POST /api/customer/orders/checkout that failed).
    private String path;
}
