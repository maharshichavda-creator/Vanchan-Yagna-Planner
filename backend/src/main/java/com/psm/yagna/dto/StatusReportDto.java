package com.psm.yagna.dto;

import com.psm.yagna.domain.BookingStatus;
import java.time.LocalDate;
import java.util.List;

/** Admin status report: totals, per-mandal haribhakt participation and the group vanchan groups. */
public record StatusReportDto(
        Summary summary,
        List<MandalReport> mandals,
        List<GroupReport> groups) {

    public record Counts(int total, int pending, int completed) {
    }

    public record Summary(int mandals, int haribhakts, Counts vanchan, Counts shravan, Counts groupVanchan) {
    }

    public record MandalReport(String mandal, int haribhaktCount, Counts vanchan, Counts shravan,
                               List<HaribhaktReport> haribhakts) {
    }

    public record HaribhaktReport(Long id, String name, List<VanchanEntry> vanchan, List<ShravanEntry> shravan) {
    }

    public record VanchanEntry(LocalDate date, String bookTitle, int startPage, int endPage, BookingStatus status) {
    }

    public record ShravanEntry(LocalDate date, int trackNo, String trackTitle, BookingStatus status) {
    }

    /** Group members are stored by name only; mandal is filled in when the name matches one registered haribhakt. */
    public record GroupMember(String name, String mandal, String bookTitle, int pagesRead) {
    }

    public record GroupReport(Long id, String groupName, LocalDate date, List<GroupMember> members, int pagesRead,
                              int totalPages, double percentComplete, BookingStatus status) {
    }
}
