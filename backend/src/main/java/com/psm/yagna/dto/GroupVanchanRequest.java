package com.psm.yagna.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record GroupVanchanRequest(
        @NotNull(message = "Group name is required") Long groupNameId,
        LocalDate date,
        @NotEmpty(message = "Add at least one haribhakt name") List<String> members) {
}