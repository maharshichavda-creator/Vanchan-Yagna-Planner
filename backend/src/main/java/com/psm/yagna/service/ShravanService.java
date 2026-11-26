package com.psm.yagna.service;

import com.psm.yagna.domain.AllocationLock;
import com.psm.yagna.domain.BookingStatus;
import com.psm.yagna.domain.Haribhakt;
import com.psm.yagna.domain.ShravanBooking;
import com.psm.yagna.domain.ShravanTrack;
import com.psm.yagna.dto.ShravanBookingDto;
import com.psm.yagna.dto.ShravanSummaryDto;
import com.psm.yagna.dto.ShravanTrackDto;
import com.psm.yagna.dto.ShravanTrackRequest;
import com.psm.yagna.exception.ConflictException;
import com.psm.yagna.exception.ResourceNotFoundException;
import com.psm.yagna.repository.AllocationLockRepository;
import com.psm.yagna.repository.ShravanBookingRepository;
import com.psm.yagna.repository.ShravanTrackRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShravanService {

    private final ShravanBookingRepository bookingRepository;
    private final ShravanTrackRepository trackRepository;
    private final AllocationLockRepository lockRepository;
    private final HaribhaktService haribhaktService;

    public ShravanService(ShravanBookingRepository bookingRepository, ShravanTrackRepository trackRepository,
                          AllocationLockRepository lockRepository, HaribhaktService haribhaktService) {
        this.bookingRepository = bookingRepository;
        this.trackRepository = trackRepository;
        this.lockRepository = lockRepository;
        this.haribhaktService = haribhaktService;
    }

    @Transactional(readOnly = true)
    public ShravanSummaryDto summary() {
        return new ShravanSummaryDto(trackRepository.count(), trackRepository.countUnallocated());
    }

    @Transactional(readOnly = true)
    public List<ShravanBookingDto> list(Long haribhaktId) {
        return bookingRepository.findByHaribhakt(haribhaktId).stream().map(ShravanBookingDto::from).toList();
    }

    @Transactional
    public ShravanBookingDto book(Long haribhaktId) {
        Haribhakt haribhakt = haribhaktService.getOrThrow(haribhaktId);
        if (bookingRepository.existsByHaribhaktIdAndStatus(haribhaktId, BookingStatus.BOOKED)) {
            throw new ConflictException("Please mark your previous shravan slot as completed before booking a new one");
        }

        // Serialize allocation so two haribhakts can never be handed the same audio track.
        lockRepository.lockByName(AllocationLock.SHRAVAN)
                .orElseThrow(() -> new IllegalStateException("Shravan allocation lock missing"));
        ShravanTrack track = trackRepository.findUnallocated(PageRequest.of(0, 1)).stream().findFirst()
                .orElseThrow(() -> new ConflictException("All audio tracks have been allocated. No more shravan slots are available"));
        return ShravanBookingDto.from(bookingRepository.save(new ShravanBooking(haribhakt, track, LocalDate.now())));
    }

    @Transactional
    public ShravanBookingDto complete(Long bookingId, Long haribhaktId) {
        ShravanBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getHaribhakt().getId().equals(haribhaktId)) {
            throw new ResourceNotFoundException("Booking not found");
        }
        booking.markCompleted();
        return ShravanBookingDto.from(booking);
    }

    /** Appends a track to the end of the listening order. */
    @Transactional
    public ShravanTrackDto addTrack(ShravanTrackRequest request) {
        lockRepository.lockByName(AllocationLock.SHRAVAN)
                .orElseThrow(() -> new IllegalStateException("Shravan allocation lock missing"));
        String url = request.audioUrl() == null || request.audioUrl().isBlank() ? null : request.audioUrl().trim();
        ShravanTrack track = new ShravanTrack(trackRepository.findMaxSequenceNo() + 1, request.title().trim(), url);
        return ShravanTrackDto.from(trackRepository.save(track));
    }
}
