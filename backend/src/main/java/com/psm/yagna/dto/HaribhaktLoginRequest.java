package com.psm.yagna.dto;

import jakarta.validation.constraints.NotBlank;

public record HaribhaktLoginRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Mandal is required") String mandal) {
}