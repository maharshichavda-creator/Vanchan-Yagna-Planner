package com.psm.yagna.dto;

import jakarta.validation.constraints.NotNull;

public record BookingRequest(@NotNull(message = "Haribhakt is required") Long haribhaktId) {
}
