package com.psm.yagna.repository;

import com.psm.yagna.domain.Haribhakt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HaribhaktRepository extends JpaRepository<Haribhakt, Long> {

    Optional<Haribhakt> findFirstByNameIgnoreCaseAndMandalOrderByIdAsc(String name, String mandal);
}
