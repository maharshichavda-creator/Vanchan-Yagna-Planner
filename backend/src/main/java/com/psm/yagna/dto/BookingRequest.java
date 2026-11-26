package com.psm.yagna.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookingRequest(
        @NotNull(message = "Haribhakt is required") Long haribhaktId,
        @NotNull(message = "Date is required") LocalDate date) {
}