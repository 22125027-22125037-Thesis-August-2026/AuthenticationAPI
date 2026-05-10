package com.mhsa.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Generic API Response Wrapper Used to standardize all API responses across the
 * application
 *
 * @param <T> The type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates whether the request was successful
     */
    private boolean success;

    /**
     * The actual response data
     */
    private T data;

    /**
     * Error information if the request failed
     */
    private String error;

    /**
     * Timestamp when the response was generated
     */
    private Instant timestamp;

    /**
     * Factory method to create a successful response
     *
     * @param data The response data
     * @param <T> The type of data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Factory method to create an error response
     *
     * @param errorMessage The error message
     * @param <T> The type of data
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String errorMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .error(errorMessage)
                .timestamp(Instant.now())
                .build();
    }
}
