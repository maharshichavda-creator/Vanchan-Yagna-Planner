package com.psm.yagna.service;

import com.psm.yagna.domain.GroupName;
import com.psm.yagna.domain.GroupVanchan;
import com.psm.yagna.domain.GroupVanchanMember;
import com.psm.yagna.domain.VanchanBook;
import com.psm.yagna.dto.GroupNameDto;
import com.psm.yagna.dto.GroupProgressRequest;
import com.psm.yagna.dto.GroupVanchanDto;
import com.psm.yagna.dto.GroupVanchanRequest;
import com.psm.yagna.dto.VanchanBookDto;
import com.psm.yagna.exception.ConflictException;
import com.psm.yagna.exception.ResourceNotFoundException;
import com.psm.yagna.repository.GroupNameRepository;
import com.psm.yagna.repository.GroupVanchanRepository;
import com.psm.yagna.repository.VanchanBookRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    public List<VanchanBookDto> books() {
        return bookRepository.findAllByOrderBySequenceNoAsc().stream().map(VanchanBookDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<GroupVanchanDto> list() {
        int totalPages = totalPages();
        Map<Long, VanchanBook> books = booksById();
        return groupVanchanRepository.findAllWithDetails().stream().map(g -> GroupVanchanDto.from(g, totalPages, books)).toList();
    }

    private Map<Long, VanchanBook> booksById() {
        return bookRepository.findAllByOrderBySequenceNoAsc().stream()
                .collect(Collectors.toMap(VanchanBook::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    @Transactional
    public GroupVanchanDto create(GroupVanchanRequest request) {
        GroupName groupName = groupNameRepository.findById(request.groupNameId())
                .orElseThrow(() -> new ResourceNotFoundException("Group name not found"));
        if (groupVanchanRepository.existsByGroupNameId(groupName.getId())) {
            throw new ConflictException("A vanchan group has already been created with the name \"" + groupName.getName() + "\"");
        }
        // Every book needs exactly one haribhakt; members are stored in book order.
        Map<Long, VanchanBook> books = booksById();
        Map<Long, String> nameByBook = new HashMap<>();
        for (GroupVanchanRequest.Assignment a : request.assignments()) {
            if (!books.containsKey(a.bookId())) {
                throw new IllegalArgumentException("Unknown book");
            }
            if (nameByBook.put(a.bookId(), a.haribhaktName().trim()) != null) {
                throw new IllegalArgumentException("A book can have only one haribhakt");
            }
        }
        List<GroupVanchanMember> members = new ArrayList<>();
        for (VanchanBook book : books.values()) {
            String name = nameByBook.get(book.getId());
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Enter a haribhakt name against every book");
            }
            members.add(new GroupVanchanMember(name, book.getId()));
        }
        LocalDate date = request.date() != null ? request.date() : LocalDate.now();
        return GroupVanchanDto.from(groupVanchanRepository.save(new GroupVanchan(groupName, date, members)),
                totalPages(), books);
    }

    @Transactional
    public GroupVanchanDto updateProgress(Long id, GroupProgressRequest request) {
        GroupVanchan group = groupVanchanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group vanchan not found"));
        List<Integer> pagesRead = request.pagesRead();
        if (pagesRead.size() != group.getMembers().size()) {
            throw new IllegalArgumentException("Enter pages read for every haribhakt of the group");
        }
        Map<Long, VanchanBook> books = booksById();
        for (int i = 0; i < pagesRead.size(); i++) {
            GroupVanchanMember member = group.getMembers().get(i);
            VanchanBook book = member.getBookId() == null ? null : books.get(member.getBookId());
            if (book != null && pagesRead.get(i) > book.getTotalPages()) {
                throw new IllegalArgumentException("Pages read for " + book.getTitle() + " cannot be more than "
                        + book.getTotalPages());
            }
        }
        int total = totalPages();
        long sum = pagesRead.stream().mapToLong(Integer::longValue).sum();
        if (sum > total) {
            throw new IllegalArgumentException("Total pages read (" + sum + ") cannot be more than " + total);
        }
        group.updatePagesRead(pagesRead);
        group.updateStatus(total);
        return GroupVanchanDto.from(group, total, books);
    }
}
