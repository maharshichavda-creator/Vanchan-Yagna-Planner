package com.psm.yagna.repository;

import com.psm.yagna.domain.ShravanBooking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShravanBookingRepository extends JpaRepository<ShravanBooking, Long> {

    @Query("select b from ShravanBooking b join fetch b.track where b.haribhakt.id = :haribhaktId order by b.bookingDate desc, b.id desc")
    List<ShravanBooking> findByHaribhakt(Long haribhaktId);

    @Query("select b from ShravanBooking b join fetch b.haribhakt join fetch b.track order by b.bookingDate desc, b.id desc")
    List<ShravanBooking> findAllWithDetails();
}
