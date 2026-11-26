package com.psm.yagna.service;

import com.psm.yagna.domain.GroupName;
import com.psm.yagna.domain.GroupVanchan;
import com.psm.yagna.dto.GroupNameDto;
import com.psm.yagna.dto.GroupProgressRequest;
import com.psm.yagna.dto.GroupVanchanDto;
import com.psm.yagna.dto.GroupVanchanRequest;
import com.psm.yagna.exception.ConflictException;
import com.psm.yagna.exception.ResourceNotFoundException;
import com.psm.yagna.repository.GroupNameRepository;
import com.psm.yagna.repository.GroupVanchanRepository;
import com.psm.yagna.repository.VanchanBookRepository;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupVanchanService {

    private final GroupNameRepository groupNameRepository;
    private final GroupVanchanRepository groupVanchanRepository;
    private final VanchanBookRepository bookRepository;

    public GroupVanchanService(GroupNameRepository groupNameRepository, GroupVanchanRepository groupVanchanRepository,
                               VanchanBookRepository bookRepository) {
        this.groupNameRepository = groupNameRepository;
        this.groupVanchanRepository = groupVanchanRepository;
        this.bookRepository = bookRepository;
    }

    /** Total pages of all books together; a group is 100% complete once this many pages have been read. */
    public int totalPages() {
        return (int) bookRepository.sumTotalPages();
    }

    /** Only group names that have not yet been used to create a group. */
    @Transactional(readOnly = true)
    public List<GroupNameDto> groupNames() {
        return groupNameRepository.findAvailable().stream().map(GroupNameDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<GroupVanchanDto> list() {
        int totalPages = totalPages();
        return groupVanchanRepository.findAllWithDetails().stream().map(g -> GroupVanchanDto.from(g, totalPages)).toList();
    }

    @Transactional
    public GroupVanchanDto create(GroupVanchanRequest request) {
        GroupName groupName = groupNameRepository.findById(request.groupNameId())
                .orElseThrow(() -> new ResourceNotFoundException("Group name not found"));
        if (groupVanchanRepository.existsByGroupNameId(groupName.getId())) {
            throw new ConflictException("A vanchan group has already been created with the name \"" + groupName.getName() + "\"");
        }
        List<String> members = new LinkedHashSet<>(request.members().stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList()).stream().toList();
        if (members.isEmpty()) {
            throw new IllegalArgumentException("Add at least one haribhakt name");
        }
        LocalDate date = request.date() != null ? request.date() : LocalDate.now();
        return GroupVanchanDto.from(groupVanchanRepository.save(new GroupVanchan(groupName, date, members)), totalPages());
    }

    @Transactional
    public GroupVanchanDto updateProgress(Long id, GroupProgressRequest request) {
        GroupVanchan group = groupVanchanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group vanchan not found"));
        List<Integer> pagesRead = request.pagesRead();
        if (pagesRead.size() != group.getMembers().size()) {
            throw new IllegalArgumentException("Enter pages read for every haribhakt of the group");
        }
        int total = totalPages();
        long sum = pagesRead.stream().mapToLong(Integer::longValue).sum();
        if (sum > total) {
            throw new IllegalArgumentException("Total pages read (" + sum + ") cannot be more than " + total);
        }
        group.updatePagesRead(pagesRead);
        return GroupVanchanDto.from(group, total);
    }

    @Transactional
    public GroupVanchanDto complete(Long id) {
        GroupVanchan group = groupVanchanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group vanchan not found"));
        group.markCompleted();
        return GroupVanchanDto.from(group, totalPages());
    }
}
