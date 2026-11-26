package com.psm.yagna.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "vanchan_booking", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vanchan_haribhakt_date", columnNames = {"haribhakt_id", "booking_date"}),
        @UniqueConstraint(name = "uk_vanchan_book_start_page", columnNames = {"book_id", "start_page"})
})
public class VanchanBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "haribhakt_id", nullable = false)
    private Haribhakt haribhakt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private VanchanBook book;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "start_page", nullable = false)
    private int startPage;

    @Column(name = "end_page", nullable = false)
    private int endPage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.BOOKED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    protected VanchanBooking() {
    }

    public VanchanBooking(Haribhakt haribhakt, VanchanBook book, LocalDate bookingDate, int startPage, int endPage) {
        this.haribhakt = haribhakt;
        this.book = book;
        this.bookingDate = bookingDate;
        this.startPage = startPage;
        this.endPage = endPage;
    }

    public void markCompleted() {
        this.status = BookingStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Haribhakt getHaribhakt() {
        return haribhakt;
    }

    public VanchanBook getBook() {
        return book;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public int getStartPage() {
        return startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
