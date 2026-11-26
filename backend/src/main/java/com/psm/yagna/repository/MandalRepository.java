package com.psm.yagna.repository;

import com.psm.yagna.domain.Mandal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MandalRepository extends JpaRepository<Mandal, Long> {

    boolean existsByName(String name);

    List<Mandal> findAllByOrderBySequenceNoAsc();
}
