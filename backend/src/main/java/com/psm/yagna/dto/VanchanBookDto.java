package com.psm.yagna.dto;

import com.psm.yagna.domain.VanchanBook;

public record VanchanBookDto(Long id, int sequenceNo, String title, int totalPages) {

    public static VanchanBookDto from(VanchanBook b) {
        return new VanchanBookDto(b.getId(), b.getSequenceNo(), b.getTitle(), b.getTotalPages());
    }
}
