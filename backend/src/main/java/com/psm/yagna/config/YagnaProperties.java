package com.psm.yagna.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record YagnaProperties(
        @Valid @NotNull Vanchan vanchan,
        @Valid @NotNull Shravan shravan,
        @Valid @NotNull GroupVanchan groupVanchan,
        @Valid @NotNull Mandal mandal,
        @Valid @NotNull Ebook ebook) {

    public record Ebook(String url) {
    }

    public record Mandal(@NotBlank String file) {
    }

    public record Vanchan(@Min(1) int pagesPerBooking, @NotBlank String booksFile) {
    }

    public record Shravan(@NotBlank String tracksFile) {
    }

    public record GroupVanchan(@NotNull List<String> names) {
    }
}