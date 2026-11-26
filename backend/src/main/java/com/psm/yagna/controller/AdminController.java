package com.psm.yagna.controller;

import com.psm.yagna.dto.StatusReportDto;
import com.psm.yagna.service.StatusReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StatusReportService statusReportService;

    public AdminController(StatusReportService statusReportService) {
        this.statusReportService = statusReportService;
    }

    @GetMapping("/status-report")
    public StatusReportDto statusReport(@RequestParam Long haribhaktId) {
        return statusReportService.report(haribhaktId);
    }
}
