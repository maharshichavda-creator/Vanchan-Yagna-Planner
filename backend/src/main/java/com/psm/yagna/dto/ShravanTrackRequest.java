package com.psm.yagna.dto;

import jakarta.validation.constraints.NotBlank;

public record ShravanTrackRequest(
        @NotBlank(message = "Track title is required") String title,
        String audioUrl) {
}