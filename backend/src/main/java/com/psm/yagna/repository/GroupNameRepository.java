package com.psm.yagna.repository;

import com.psm.yagna.domain.GroupName;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupNameRepository extends JpaRepository<GroupName, Long> {

    boolean existsByName(String name);

    @Query("""
            select g from GroupName g
            where not exists (select 1 from GroupVanchan v where v.groupName = g)
            order by g.name
            """)
    List<GroupName> findAvailable();
}
