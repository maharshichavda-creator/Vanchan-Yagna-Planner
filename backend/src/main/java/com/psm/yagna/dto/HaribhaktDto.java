package com.psm.yagna.dto;

import com.psm.yagna.domain.Haribhakt;

public record HaribhaktDto(Long id, String name, String mandal, String bapsId) {

    public static HaribhaktDto from(Haribhakt h) {
        return new HaribhaktDto(h.getId(), h.getName(), h.getMandal(), h.getBapsId());
    }
}