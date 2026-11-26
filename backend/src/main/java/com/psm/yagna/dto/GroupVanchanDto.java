package com.psm.yagna.dto;

import com.psm.yagna.domain.BookingStatus;
import com.psm.yagna.domain.GroupVanchan;
import java.time.LocalDate;
import java.util.List;

public record GroupVanchanDto(Long id, Long groupNameId, String groupName, LocalDate date, List<String> members, BookingStatus status) {

    public static GroupVanchanDto from(GroupVanchan g) {
        return new GroupVanchanDto(g.getId(), g.getGroupName().getId(), g.getGroupName().getName(), g.getVanchanDate(),
                List.copyOf(g.getMembers()), g.getStatus());
    }
}