package com.psm.yagna.repository;

import com.psm.yagna.domain.VanchanBooking;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VanchanBookingRepository extends JpaRepository<VanchanBooking, Long> {

    boolean existsByHaribhaktIdAndBookingDate(Long haribhaktId, LocalDate bookingDate);

    @EntityGraph(attributePaths = "book")
    List<VanchanBooking> findByHaribhaktIdOrderByBookingDateDescIdDesc(Long haribhaktId);

    /** The booking that currently marks the end of everything handed out so far. */
    @EntityGraph(attributePaths = "book")
    Optional<VanchanBooking> findFirstByOrderByBookSequenceNoDescEndPageDesc();
}
