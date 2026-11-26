package com.psm.yagna.repository;

import com.psm.yagna.domain.VanchanBooking;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VanchanBookingRepository extends JpaRepository<VanchanBooking, Long> {

    boolean existsByHaribhaktIdAndBookingDate(Long haribhaktId, LocalDate bookingDate);

    @EntityGraph(attributePaths = "book")
    List<VanchanBooking> findByHaribhaktIdOrderByBookingDateDescIdDesc(Long haribhaktId);

    @Query("select b from VanchanBooking b join fetch b.haribhakt join fetch b.book order by b.bookingDate desc, b.id desc")
    List<VanchanBooking> findAllWithDetails();

    /** The booking that currently marks the end of everything handed out so far. */
    @EntityGraph(attributePaths = "book")
    Optional<VanchanBooking> findFirstByOrderByBookSequenceNoDescEndPageDesc();
}
