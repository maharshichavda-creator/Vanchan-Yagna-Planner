package com.psm.yagna.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Pages read by each haribhakt of the group, in the same order as the group's members. */
public record GroupProgressRequest(
        @NotEmpty(message = "Pages read are required") List<@NotNull(message = "Enter pages read for every haribhakt")
                @Min(value = 0, message = "Pages read cannot be negative") Integer> pagesRead) {
}
