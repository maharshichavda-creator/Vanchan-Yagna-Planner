package com.psm.yagna.dto;

import com.psm.yagna.domain.GroupName;

public record GroupNameDto(Long id, String name) {

    public static GroupNameDto from(GroupName g) {
        return new GroupNameDto(g.getId(), g.getName());
    }
}