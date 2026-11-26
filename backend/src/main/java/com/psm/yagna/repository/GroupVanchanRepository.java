package com.psm.yagna.repository;

import com.psm.yagna.domain.GroupVanchan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupVanchanRepository extends JpaRepository<GroupVanchan, Long> {

    boolean existsByGroupNameId(Long groupNameId);

    @Query("select distinct g from GroupVanchan g join fetch g.groupName left join fetch g.members order by g.vanchanDate desc, g.id desc")
    List<GroupVanchan> findAllWithDetails();
}
