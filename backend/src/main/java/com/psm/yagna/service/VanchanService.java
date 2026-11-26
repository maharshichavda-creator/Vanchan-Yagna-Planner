package com.psm.yagna.service;

import com.psm.yagna.config.YagnaProperties;
import com.psm.yagna.domain.AllocationLock;
import com.psm.yagna.domain.Haribhakt;
import com.psm.yagna.domain.VanchanBook;
import com.psm.yagna.domain.VanchanBooking;
import com.psm.yagna.dto.VanchanBookingDto;
import com.psm.yagna.dto.VanchanSummaryDto;
import com.psm.yagna.exception.ConflictException;
import com.psm.yagna.exception.ResourceNotFoundException;
import com.psm.yagna.repository.AllocationLockRepository;
import com.psm.yagna.repository.VanchanBookRepository;
import com.psm.yagna.repository.VanchanBookingRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VanchanService {

    /** The first page that has not been handed out yet. */
    private record NextPage(VanchanBook book, int page) {
    }

    private final VanchanBookingRepository bookingRepository;
    private final VanchanBookRepository bookRepository;
    private final AllocationLockRepository lockRepository;
    private final HaribhaktService haribhaktService;
    private final int pagesPerBooking;

    public VanchanService(VanchanBookingRepository bookingRepository, VanchanBookRepository bookRepository,
                          AllocationLockRepository lockRepository, HaribhaktService haribhaktService,
                          YagnaProperties properties) {
        this.bookingRepository = bookingRepository;
        this.bookRepository = bookRepository;
        this.lockRepository = lockRepository;
        this.haribhaktService = haribhaktService;
        this.pagesPerBooking = properties.vanchan().pagesPerBooking();
    }

    @Transactional(readOnly = true)
    public VanchanSummaryDto summary() {
        List<VanchanBook> books = bookRepository.findAllByOrderBySequenceNoAsc();
        int totalPages = books.stream().mapToInt(VanchanBook::getTotalPages).sum();
        int allocated = bookingRepository.findFirstByOrderByBookSequenceNoDescEndPageDesc()
                .map(last -> books.stream()
                        .filter(b -> b.getSequenceNo() < last.getBook().getSequenceNo())
                        .mapToInt(VanchanBook::getTotalPages).sum() + last.getEndPage())
                .orElse(0);
        Optional<NextPage> next = nextPage();
        return new VanchanSummaryDto(pagesPerBooking, books.size(), totalPages, allocated, totalPages - allocated,
                next.map(n -> n.book().getTitle()).orElse(null), next.map(NextPage::page).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<VanchanBookingDto> list(Long haribhaktId) {
        return bookingRepository.findByHaribhaktIdOrderByIdDesc(haribhaktId).stream()
                .map(VanchanBookingDto::from)
                .toList();
    }

    @Transactional
    public VanchanBookingDto book(Long haribhaktId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Please select today or a future date");
        }
        Haribhakt haribhakt = haribhaktService.getOrThrow(haribhaktId);

        // Serialize allocation so two haribhakts can never be handed the same page range.
        lockRepository.lockByName(AllocationLock.VANCHAN)
                .orElseThrow(() -> new IllegalStateException("Vanchan allocation lock missing"));
        NextPage next = nextPage()
                .orElseThrow(() -> new ConflictException("All pages have been allocated. No more vanchan slots are available"));
        // A booking never spans two books: the last booking of a book may be shorter than the usual size.
        int end = Math.min(next.page() + pagesPerBooking - 1, next.book().getTotalPages());
        return VanchanBookingDto.from(bookingRepository.save(new VanchanBooking(haribhakt, next.book(), date, next.page(), end)));
    }

    private Optional<NextPage> nextPage() {
        Optional<VanchanBooking> last = bookingRepository.findFirstByOrderByBookSequenceNoDescEndPageDesc();
        if (last.isEmpty()) {
            return bookRepository.findAllByOrderBySequenceNoAsc().stream().findFirst().map(b -> new NextPage(b, 1));
        }
        VanchanBook book = last.get().getBook();
        if (last.get().getEndPage() < book.getTotalPages()) {
            return Optional.of(new NextPage(book, last.get().getEndPage() + 1));
        }
        return bookRepository.findFirstBySequenceNoGreaterThanOrderBySequenceNoAsc(book.getSequenceNo())
                .map(b -> new NextPage(b, 1));
    }

    @Transactional
    public VanchanBookingDto complete(Long bookingId, Long haribhaktId) {
        VanchanBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getHaribhakt().getId().equals(haribhaktId)) {
            throw new ResourceNotFoundException("Booking not found");
        }
        booking.markCompleted();
        return VanchanBookingDto.from(booking);
    }
}
