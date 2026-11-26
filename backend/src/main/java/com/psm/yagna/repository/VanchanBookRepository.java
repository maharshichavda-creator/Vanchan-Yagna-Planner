package com.psm.yagna.repository;

import com.psm.yagna.domain.VanchanBook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VanchanBookRepository extends JpaRepository<VanchanBook, Long> {

    List<VanchanBook> findAllByOrderBySequenceNoAsc();

    Optional<VanchanBook> findFirstBySequenceNoGreaterThanOrderBySequenceNoAsc(int sequenceNo);
}
