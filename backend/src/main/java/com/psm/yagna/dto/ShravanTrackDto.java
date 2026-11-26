package com.psm.yagna.dto;

import com.psm.yagna.domain.ShravanTrack;

public record ShravanTrackDto(Long id, int sequenceNo, String title, String audioUrl) {

    public static ShravanTrackDto from(ShravanTrack t) {
        return new ShravanTrackDto(t.getId(), t.getSequenceNo(), t.getTitle(), t.getAudioUrl());
    }
}