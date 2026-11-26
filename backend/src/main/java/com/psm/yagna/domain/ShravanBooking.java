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
@Table(name = "shravan_booking", uniqueConstraints = {
        @UniqueConstraint(name = "uk_shravan_haribhakt_date", columnNames = {"haribhakt_id", "booking_date"}),
        @UniqueConstraint(name = "uk_shravan_track", columnNames = "track_id")
})
public class ShravanBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "haribhakt_id", nullable = false)
    private Haribhakt haribhakt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private ShravanTrack track;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.BOOKED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ShravanBooking() {
    }

    public ShravanBooking(Haribhakt haribhakt, ShravanTrack track, LocalDate bookingDate) {
        this.haribhakt = haribhakt;
        this.track = track;
        this.bookingDate = bookingDate;
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

    public ShravanTrack getTrack() {
        return track;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
