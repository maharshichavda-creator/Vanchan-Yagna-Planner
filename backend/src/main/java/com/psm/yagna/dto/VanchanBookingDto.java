package com.psm.yagna.dto;

import com.psm.yagna.domain.BookingStatus;
import com.psm.yagna.domain.VanchanBooking;
import java.time.LocalDate;

public record VanchanBookingDto(Long id, LocalDate date, Long bookId, String bookTitle, int startPage, int endPage,
                                int pageCount, BookingStatus status, long pushpNumber) {

    public static VanchanBookingDto from(VanchanBooking b, long pushpNumber) {
        return new VanchanBookingDto(b.getId(), b.getBookingDate(), b.getBook().getId(), b.getBook().getTitle(),
                b.getStartPage(), b.getEndPage(), b.getEndPage() - b.getStartPage() + 1, b.getStatus(), pushpNumber);
    }
}