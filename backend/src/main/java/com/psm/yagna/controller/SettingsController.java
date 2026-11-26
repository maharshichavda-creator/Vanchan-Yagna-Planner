package com.psm.yagna.controller;

import com.psm.yagna.domain.AppSetting;
import com.psm.yagna.dto.SettingsDto;
import com.psm.yagna.repository.AppSettingRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final AppSettingRepository settingRepository;

    public SettingsController(AppSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public SettingsDto settings() {
        String ebookUrl = settingRepository.findById(AppSetting.EBOOK_URL)
                .map(AppSetting::getValue)
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .orElse(null);
        return new SettingsDto(ebookUrl);
    }
}
