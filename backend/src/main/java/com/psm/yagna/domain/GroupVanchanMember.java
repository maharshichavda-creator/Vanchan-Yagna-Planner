package com.psm.yagna.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** A haribhakt taking part in a group vanchan, with the number of pages they have read so far. */
@Embeddable
public class GroupVanchanMember {

    @Column(name = "haribhakt_name", nullable = false, length = 120)
    private String name;

    // Nullable so the column can be added to a table that already has rows; null means nothing read yet.
    @Column(name = "pages_read")
    private Integer pagesRead;

    protected GroupVanchanMember() {
    }

    public GroupVanchanMember(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPagesRead() {
        return pagesRead == null ? 0 : pagesRead;
    }

    public void setPagesRead(int pagesRead) {
        this.pagesRead = pagesRead;
    }
}
