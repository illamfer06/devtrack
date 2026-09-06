package com.devtrack.backend.controller;

import com.devtrack.backend.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Health",
        description = "Operations for checking the application health status"
)
@RestController
public class HealthController {

    @Operation(
            summary = "Check application health",
            description = "Returns the current health status of the DevTrack application."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Application is running"
            )
    })
    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP","DevTrack");
    }
}
