package com.psm.yagna.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** A group is created by naming the haribhakt who reads each book. */
public record GroupVanchanRequest(
        @NotNull(message = "Group name is required") Long groupNameId,
        LocalDate date,
        @NotEmpty(message = "Enter a haribhakt name against every book") @Valid List<Assignment> assignments) {

    public record Assignment(
            @NotNull(message = "Book is required") Long bookId,
            @NotBlank(message = "Enter a haribhakt name against every book")
            @Size(max = 120, message = "Haribhakt name is too long") String haribhaktName) {
    }
}