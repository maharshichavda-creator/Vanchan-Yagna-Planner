package com.psm.yagna.repository;

import com.psm.yagna.domain.AllocationLock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AllocationLockRepository extends JpaRepository<AllocationLock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from AllocationLock l where l.name = :name")
    Optional<AllocationLock> lockByName(@Param("name") String name);
}
