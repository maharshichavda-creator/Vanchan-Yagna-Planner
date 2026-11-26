package com.psm.yagna.controller;

import com.psm.yagna.dto.HaribhaktDto;
import com.psm.yagna.dto.HaribhaktLoginRequest;
import com.psm.yagna.service.HaribhaktService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/haribhakt")
public class HaribhaktController {

    private final HaribhaktService haribhaktService;

    public HaribhaktController(HaribhaktService haribhaktService) {
        this.haribhaktService = haribhaktService;
    }

    @PostMapping("/sign-in")
    public HaribhaktDto signIn(@Valid @RequestBody HaribhaktLoginRequest request) {
        return haribhaktService.signIn(request);
    }
}
