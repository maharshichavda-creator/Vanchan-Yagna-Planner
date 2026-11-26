package com.psm.yagna.dto;

import com.psm.yagna.domain.BookingStatus;
import com.psm.yagna.domain.ShravanBooking;
import java.time.LocalDate;

public record ShravanBookingDto(Long id, LocalDate date, ShravanTrackDto track, BookingStatus status) {

    public static ShravanBookingDto from(ShravanBooking b) {
        return new ShravanBookingDto(b.getId(), b.getBookingDate(), ShravanTrackDto.from(b.getTrack()), b.getStatus());
    }
}