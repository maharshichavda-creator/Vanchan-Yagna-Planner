package com.psm.yagna.dto;

import com.psm.yagna.domain.BookingStatus;
import com.psm.yagna.domain.GroupVanchan;
import com.psm.yagna.domain.GroupVanchanMember;
import java.time.LocalDate;
import java.util.List;

/**
 * A group vanchan with its reading progress. {@code memberPages} lines up with {@code members};
 * {@code percentComplete} is the pages read by all members out of {@code totalPages} (all books together).
 */
public record GroupVanchanDto(Long id, Long groupNameId, String groupName, LocalDate date, List<String> members,
                              List<Integer> memberPages, int pagesRead, int totalPages, double percentComplete,
                              BookingStatus status) {

    public static GroupVanchanDto from(GroupVanchan g, int totalPages) {
        int pagesRead = g.totalPagesRead();
        return new GroupVanchanDto(g.getId(), g.getGroupName().getId(), g.getGroupName().getName(), g.getVanchanDate(),
                g.getMembers().stream().map(GroupVanchanMember::getName).toList(),
                g.getMembers().stream().map(GroupVanchanMember::getPagesRead).toList(),
                pagesRead, totalPages, percent(pagesRead, totalPages), g.getStatus());
    }

    /** Percentage with one decimal place; 0 when there are no pages at all. */
    public static double percent(int pagesRead, int totalPages) {
        return totalPages <= 0 ? 0 : Math.round(pagesRead * 1000.0 / totalPages) / 10.0;
    }
}