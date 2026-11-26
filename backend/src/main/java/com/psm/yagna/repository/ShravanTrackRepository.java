package com.psm.yagna.repository;

import com.psm.yagna.domain.ShravanTrack;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShravanTrackRepository extends JpaRepository<ShravanTrack, Long> {

    @Query("""
            select t from ShravanTrack t
            where not exists (select 1 from ShravanBooking b where b.track = t)
            order by t.sequenceNo
            """)
    List<ShravanTrack> findUnallocated(Pageable pageable);

    @Query("select count(t) from ShravanTrack t where not exists (select 1 from ShravanBooking b where b.track = t)")
    long countUnallocated();

    @Query("select coalesce(max(t.sequenceNo), 0) from ShravanTrack t")
    int findMaxSequenceNo();
}
