package com.psm.yagna.controller;

import com.psm.yagna.dto.MandalDto;
import com.psm.yagna.repository.MandalRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mandals")
public class MandalController {

    private final MandalRepository mandalRepository;

    public MandalController(MandalRepository mandalRepository) {
        this.mandalRepository = mandalRepository;
    }

    @GetMapping
    public List<MandalDto> list() {
        return mandalRepository.findAllByOrderBySequenceNoAsc().stream().map(MandalDto::from).toList();
    }
}
