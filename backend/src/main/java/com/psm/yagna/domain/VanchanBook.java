package com.psm.yagna.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** A book that is read through Vanchan Yagna; its pages are numbered 1..totalPages. */
@Entity
@Table(name = "vanchan_book", uniqueConstraints = @UniqueConstraint(columnNames = "sequence_no"))
public class VanchanBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reading order: pages are handed out from the lowest sequence number first. */
    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "total_pages", nullable = false)
    private int totalPages;

    protected VanchanBook() {
    }

    public VanchanBook(int sequenceNo, String title, int totalPages) {
        this.sequenceNo = sequenceNo;
        this.title = title;
        this.totalPages = totalPages;
    }

    public Long getId() {
        return id;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
