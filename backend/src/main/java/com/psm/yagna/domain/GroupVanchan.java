package com.psm.yagna.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_vanchan", uniqueConstraints = @UniqueConstraint(name = "uk_group_vanchan_group_name", columnNames = "group_name_id"))
public class GroupVanchan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_name_id", nullable = false)
    private GroupName groupName;

    @Column(name = "vanchan_date", nullable = false)
    private LocalDate vanchanDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "group_vanchan_member", joinColumns = @JoinColumn(name = "group_vanchan_id"))
    @OrderColumn(name = "member_order")
    private List<GroupVanchanMember> members = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.BOOKED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    protected GroupVanchan() {
    }

    public GroupVanchan(GroupName groupName, LocalDate vanchanDate, List<GroupVanchanMember> members) {
        this.groupName = groupName;
        this.vanchanDate = vanchanDate;
        this.members = new ArrayList<>(members);
    }

    /** Sets the pages read for each member, in member order. */
    public void updatePagesRead(List<Integer> pagesRead) {
        for (int i = 0; i < members.size(); i++) {
            members.get(i).setPagesRead(pagesRead.get(i));
        }
    }

    public int totalPagesRead() {
        return members.stream().mapToInt(GroupVanchanMember::getPagesRead).sum();
    }

    /**
     * A group is completed automatically once all pages of all books have been read; lowering the numbers
     * again (e.g. to correct a mistake) puts it back in progress.
     */
    public void updateStatus(int totalPages) {
        boolean done = totalPages > 0 && totalPagesRead() >= totalPages;
        if (done && status != BookingStatus.COMPLETED) {
            this.status = BookingStatus.COMPLETED;
            this.completedAt = Instant.now();
        } else if (!done && status == BookingStatus.COMPLETED) {
            this.status = BookingStatus.BOOKED;
            this.completedAt = null;
        }
    }

    public Long getId() {
        return id;
    }

    public GroupName getGroupName() {
        return groupName;
    }

    public LocalDate getVanchanDate() {
        return vanchanDate;
    }

    public List<GroupVanchanMember> getMembers() {
        return members;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
