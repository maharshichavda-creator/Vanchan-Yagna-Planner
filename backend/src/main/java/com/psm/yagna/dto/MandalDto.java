package com.psm.yagna.dto;

import com.psm.yagna.domain.Mandal;

public record MandalDto(Long id, String name) {

    public static MandalDto from(Mandal m) {
        return new MandalDto(m.getId(), m.getName());
    }
}
