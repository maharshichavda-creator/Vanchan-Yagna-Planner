package com.psm.yagna.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One row per allocation sequence (VANCHAN, SHRAVAN). Locked with a pessimistic write lock while
 * handing out the next page range / audio track so concurrent bookings are serialized and can never
 * receive the same range or track.
 */
@Entity
@Table(name = "allocation_lock")
public class AllocationLock {

    public static final String VANCHAN = "VANCHAN";
    public static final String SHRAVAN = "SHRAVAN";

    @Id
    @Column(length = 30)
    private String name;

    protected AllocationLock() {
    }

    public AllocationLock(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
