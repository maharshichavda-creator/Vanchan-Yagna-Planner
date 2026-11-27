package com.psm.yagna.dto;

import com.psm.yagna.domain.BookingStatus;
import com.psm.yagna.domain.GroupVanchan;
import com.psm.yagna.domain.GroupVanchanMember;
import com.psm.yagna.domain.VanchanBook;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A group vanchan with its reading progress. Each member is a haribhakt reading one book; {@code percentComplete}
 * is the pages read by all members out of {@code totalPages} (all books together).
 */
public record GroupVanchanDto(Long id, Long groupNameId, String groupName, LocalDate date, List<MemberDto> members,
                              int pagesRead, int totalPages, double percentComplete, BookingStatus status,
                              Long createdById) {

    /** {@code bookTitle} and {@code bookPages} are null for groups created before books were assigned. */
    public record MemberDto(String name, String bookTitle, Integer bookPages, int pagesRead) {

        static MemberDto from(GroupVanchanMember m, Map<Long, VanchanBook> books) {
            VanchanBook book = m.getBookId() == null ? null : books.get(m.getBookId());
            return new MemberDto(m.getName(), book == null ? null : book.getTitle(),
                    book == null ? null : book.getTotalPages(), m.getPagesRead());
        }
    }

    public static GroupVanchanDto from(GroupVanchan g, int totalPages, Map<Long, VanchanBook> books) {
        int pagesRead = g.totalPagesRead();
        return new GroupVanchanDto(g.getId(), g.getGroupName().getId(), g.getGroupName().getName(), g.getVanchanDate(),
                g.getMembers().stream().map(m -> MemberDto.from(m, books)).toList(),
                pagesRead, totalPages, percent(pagesRead, totalPages), g.getStatus(),
                g.getCreatedById());
    }

    /** Percentage with one decimal place; 0 when there are no pages at all. */
    public static double percent(int pagesRead, int totalPages) {
        return totalPages <= 0 ? 0 : Math.round(pagesRead * 1000.0 / totalPages) / 10.0;
    }
}