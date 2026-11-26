package com.psm.yagna.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "shravan_track", uniqueConstraints = @UniqueConstraint(columnNames = "sequence_no"))
public class ShravanTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Position in the listening order; tracks are handed out by ascending sequence number. */
    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "audio_url", length = 1000)
    private String audioUrl;

    protected ShravanTrack() {
    }

    public ShravanTrack(int sequenceNo, String title, String audioUrl) {
        this.sequenceNo = sequenceNo;
        this.title = title;
        this.audioUrl = audioUrl;
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

    public String getAudioUrl() {
        return audioUrl;
    }
}
